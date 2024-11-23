package com.ec337.facescanpayment.core.embeddings;

import android.content.Context;
import android.graphics.Bitmap;

import java.nio.ByteBuffer;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.gpu.GpuDelegate;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import org.tensorflow.lite.support.tensorbuffer.TensorBufferFloat;

public class FaceNet {

    private static final int IMG_SIZE = 160;
    private static final int EMBEDDING_DIM = 512;

    private Interpreter interpreter;
    private final ImageProcessor imageTensorProcessor;

    public FaceNet(Context context, boolean useGpu, boolean useXNNPack) {
        // Initialize the image processor
        imageTensorProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(IMG_SIZE, IMG_SIZE, ResizeOp.ResizeMethod.BILINEAR))
                .add(new StandardizeOp())
                .build();

        // Initialize TFLite Interpreter
        Interpreter.Options interpreterOptions = new Interpreter.Options();

        // Add the GPU Delegate if supported
        if (useGpu) {
            try (CompatibilityList compatibilityList = new CompatibilityList()) {
                if (compatibilityList.isDelegateSupportedOnThisDevice()) {
                    interpreterOptions.addDelegate(new GpuDelegate(compatibilityList.getBestOptionsForThisDevice()));
                }
            }
        } else {
            // Set the number of threads for computation
            interpreterOptions.setNumThreads(4);
        }

        interpreterOptions.setUseXNNPACK(useXNNPack);
        interpreterOptions.setUseNNAPI(true);

        // Load the model
        try {
            interpreter = new Interpreter(FileUtil.loadMappedFile(context, "facenet_512.tflite"), interpreterOptions);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    // Get face embedding using FaceNet
    public float[] getFaceEmbedding(Bitmap image) {
        ByteBuffer inputBuffer = convertBitmapToBuffer(image);
        return runFaceNet(inputBuffer)[0];
    }

    // Run the FaceNet model
    private float[][] runFaceNet(Object inputs) {
        float[][] faceNetModelOutputs = new float[1][EMBEDDING_DIM];
        interpreter.run(inputs, faceNetModelOutputs);
        return faceNetModelOutputs;
    }

    // Resize the given bitmap and convert it to a ByteBuffer
    private ByteBuffer convertBitmapToBuffer(Bitmap image) {
        return imageTensorProcessor.process(TensorImage.fromBitmap(image)).getBuffer();
    }

    // Op to perform standardization
    // x' = ( x - mean ) / std_dev
    public static class StandardizeOp implements TensorOperator {

        @Override
        public TensorBuffer apply(TensorBuffer input) {
            float[] pixels = input.getFloatArray();

            if (pixels.length == 0) {
                throw new IllegalArgumentException("Input tensor is empty.");
            }

            float mean = 0.0f, std = 0.0f;
            // Calculate mean
            for (float pixel : pixels) {
                mean += pixel;
            }
            mean /= pixels.length;

            // Calculate standard deviation
            for (float pixel : pixels) {
                std += (float) Math.pow(pixel - mean, 2);
            }
            std = (float) Math.sqrt(std / pixels.length);

            // Ensure standard deviation is at least 1/sqrt(n) to avoid division by zero
            std = Math.max(std, 1.0f / (float) Math.sqrt(pixels.length));

            // Standardize the pixels
            for (int i = 0; i < pixels.length; i++) {
                pixels[i] = (pixels[i] - mean) / std;
            }

            // Create output tensor
            TensorBufferFloat output = (TensorBufferFloat) TensorBufferFloat.createFixedSize(input.getShape(), DataType.FLOAT32);
            output.loadArray(pixels);

            return output;
        }
    }
}

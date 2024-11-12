package com.ec337.facescanpayment.core.utils;

import android.content.Context;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import java.util.concurrent.Executor;

public class BiometricHelper {

    private final Executor executor;
    private final BiometricPrompt biometricPrompt;
    private final BiometricPrompt.PromptInfo promptInfo;

    public BiometricHelper(Context context, BiometricPrompt.AuthenticationCallback callback) {
        executor = ContextCompat.getMainExecutor(context);
        biometricPrompt = new BiometricPrompt((androidx.fragment.app.FragmentActivity) context, executor, callback);

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login")
                .setSubtitle("Log in using your fingerprint")
                .setDeviceCredentialAllowed(true)
                .build();
    }

    public void authenticate() {
        biometricPrompt.authenticate(promptInfo);
    }
}

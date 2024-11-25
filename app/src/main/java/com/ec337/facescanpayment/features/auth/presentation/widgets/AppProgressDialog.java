package com.ec337.facescanpayment.features.auth.presentation.widgets;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class AppProgressDialog {

    private static Dialog progressDialog;
    private static TextView progressDialogText;
    private static boolean isDialogVisible = false;

    public static void showProgressDialog(Context context) {
        if (!isDialogVisible) {
            progressDialog = new Dialog(context);
            progressDialog.setCancelable(false); // Non-cancellable progress dialog
            progressDialog.setContentView(createProgressDialogView(context));

            progressDialog.show();
            isDialogVisible = true;
        }
    }

    public static void hideProgressDialog() {
        if (isDialogVisible && progressDialog != null) {
            progressDialog.dismiss();
            isDialogVisible = false;
        }
    }

    public static void setProgressDialogText(String message) {
        if (progressDialogText != null) {
            progressDialogText.setText(message);
        }
    }

    private static View createProgressDialogView(Context context) {
        // Create a LinearLayout to hold the progress bar and the text
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(40, 40, 40, 40);

        // Create a ProgressBar
        ProgressBar progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true); // Set as indeterminate (spinner-like)

        // Create a TextView for the message
        progressDialogText = new TextView(context);
        progressDialogText.setText("");
        progressDialogText.setTextColor(Color.BLACK);
        progressDialogText.setTextSize(16);
        progressDialogText.setGravity(Gravity.CENTER);
        progressDialogText.setPadding(16, 8, 16, 8);

        // Add the ProgressBar and TextView to the layout
        layout.addView(progressBar);
        layout.addView(progressDialogText);

        return layout;
    }
}

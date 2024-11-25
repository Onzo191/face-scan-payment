package com.ec337.facescanpayment.features.auth.presentation.widgets;

import android.content.Context;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;

public class AppAlertDialog {

    private static String title = "";
    private static String text = "";
    private static String positiveButtonText = "";
    private static String negativeButtonText = "";
    private static DialogInterface.OnClickListener positiveButtonOnClick;
    private static DialogInterface.OnClickListener negativeButtonOnClick;
    private static boolean alertDialogShowStatus = false;

    public static void showAlertDialog(Context context) {
        if (alertDialogShowStatus) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(title)
                    .setMessage(text)
                    .setPositiveButton(positiveButtonText, positiveButtonOnClick)
                    .setNegativeButton(negativeButtonText, negativeButtonOnClick)
                    .setCancelable(false);  // Non-cancellable alert dialog

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    public static void createAlertDialog(
            String dialogTitle,
            String dialogText,
            String dialogPositiveButtonText,
            String dialogNegativeButtonText,
            DialogInterface.OnClickListener onPositiveButtonClick,
            DialogInterface.OnClickListener onNegativeButtonClick) {

        title = dialogTitle;
        text = dialogText;
        positiveButtonText = dialogPositiveButtonText;
        negativeButtonText = dialogNegativeButtonText;
        positiveButtonOnClick = onPositiveButtonClick;
        negativeButtonOnClick = onNegativeButtonClick;
        alertDialogShowStatus = true;
    }
}

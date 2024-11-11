package com.ec337.facescanpayment.core.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class NavigationUtils {

    private static final String TAG = NavigationUtils.class.getSimpleName();

    /**
     * Navigate to any activity with an option to finish the current activity.
     *
     * @param context               The current activity context.
     * @param targetActivity        The target activity to navigate to.
     * @param finishCurrentActivity Whether to finish the current activity.
     */
    public static void navigateTo(Context context, Class<?> targetActivity, boolean finishCurrentActivity) {
        Log.i(TAG, "Navigating to " + targetActivity.getSimpleName());

        Intent intent = new Intent(context, targetActivity);
        context.startActivity(intent);

        if (finishCurrentActivity && context instanceof AppCompatActivity) {
            ((AppCompatActivity) context).finish();
        }
    }

    public static void navigateTo(Context context, Class<?> targetActivity) {
        navigateTo(context, targetActivity, true);
    }
}
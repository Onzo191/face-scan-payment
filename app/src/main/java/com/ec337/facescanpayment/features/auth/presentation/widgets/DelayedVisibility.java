package com.ec337.facescanpayment.features.auth.presentation.widgets;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

public class DelayedVisibility {

    private static final int ANIMATION_DURATION = 1000;  // Duration for fade-in and fade-out animations

    public static void setVisibilityWithAnimation(final View view, boolean visible) {
        if (view == null) {
            return;
        }

        // If we want to show the view
        if (visible) {
            // Make sure the view is visible
            view.setVisibility(View.VISIBLE);

            // Create the fade-in animation
            Animation fadeIn = new AlphaAnimation(0f, 1f);
            fadeIn.setDuration(ANIMATION_DURATION);
            fadeIn.setStartOffset(0);
            view.startAnimation(fadeIn);
        }
        // If we want to hide the view
        else {
            Animation fadeOut = new AlphaAnimation(1f, 0f);
            fadeOut.setDuration(ANIMATION_DURATION);
            fadeOut.setStartOffset(0);

            // When the animation ends, set visibility to GONE so the view does not occupy space
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationRepeat(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    view.setVisibility(View.GONE);
                }
            });

            view.startAnimation(fadeOut);
        }
    }
}

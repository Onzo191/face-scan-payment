package com.ec337.facescanpayment;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ec337.facescanpayment.core.utils.NavigationUtils;
import com.ec337.facescanpayment.features.auth.presentation.LoginPage;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainEntryPoint extends AppCompatActivity {
    private static final String TAG = MainEntryPoint.class.getSimpleName();
    public static final long SPLASH_DELAY = 2000;
    public static final long FADE_IN_DURATION = 1500;
    public static final long FADE_LOADING_DURATION = 800;
    public static final long MAX_LOAD_TIME = 30000;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.app__main_entry_point);
        setupInsets();
    }

    private void setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        animateLogo();
        startSplashDelay();
        startTimeoutHandler();
    }

    private void animateLogo() {
        ImageView logo = findViewById(R.id.ivLogo);
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(FADE_IN_DURATION);
        logo.startAnimation(fadeIn);
        loadingLogo(logo);
    }

    private void loadingLogo(ImageView logo) {
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(logo, "alpha", 0.2f, 1f);
        fadeIn.setDuration(FADE_LOADING_DURATION);

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(logo, "alpha", 1f, 0.2f);
        fadeOut.setDuration(FADE_LOADING_DURATION);

        fadeIn.setRepeatMode(ObjectAnimator.REVERSE);
        fadeIn.setRepeatCount(ObjectAnimator.INFINITE);

        fadeIn.start();
    }

    private void startSplashDelay() {
        Log.d(TAG, "startSplashDelay: Splash delay started.");
        handler.postDelayed(this::fetchCredential, SPLASH_DELAY);
    }

    private void startTimeoutHandler() {
        Log.d(TAG, "startTimeoutHandler: Timeout handler started.");
        handler.postDelayed(this::showNetworkErrorSnackbar, MAX_LOAD_TIME);
    }

    private void showNetworkErrorSnackbar() {
        Log.e(TAG, "startTimeoutHandler: Timeout reached, showing network error.");
        Snackbar.make(findViewById(R.id.main), "Kiểm tra lại kết nối mạng và thử lại!", Snackbar.LENGTH_INDEFINITE)
                .setAction("Thử lại", v -> retrySplashDelay())
                .show();
    }

    private void retrySplashDelay() {
        startSplashDelay();
        handler.removeCallbacksAndMessages(null);
        startTimeoutHandler();
    }

    private void fetchCredential() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        boolean isLogin = currentUser != null;
        Class<?> targetActivity = isLogin ? MainActivity.class : LoginPage.class;
        NavigationUtils.navigateTo(MainEntryPoint.this, targetActivity);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}

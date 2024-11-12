package com.ec337.facescanpayment.core.utils;

import android.view.View;
import android.widget.ProgressBar;

public class UIManager {

    private final ProgressBar loadingIndicator;

    public UIManager(ProgressBar loadingIndicator) {
        this.loadingIndicator = loadingIndicator;
    }

    public void showLoading() {
        loadingIndicator.setVisibility(View.VISIBLE);
    }

    public void hideLoading() {
        loadingIndicator.setVisibility(View.GONE);
    }
}

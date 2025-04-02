package com.example.vehiclerentalapp.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.example.vehiclerentalapp.R;

public class LoadingScreen {
    private Dialog dialog;
    private TextView loadingText;

    public LoadingScreen(Context context) {
        dialog = new Dialog(context, R.style.CustomDialogTheme);
        dialog.setContentView(R.layout.loading_screen);
        dialog.setCancelable(false); // Prevents the user from closing it manually

        // Center the dialog on the screen
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.CENTER; // Center the dialog
            window.setAttributes(params);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Initialize the loading text
        loadingText = dialog.findViewById(R.id.loadingText);
    }

    // Show loading dialog with default or custom text and fade-in animation
    public void show(String message) {
        if (dialog != null && loadingText != null) {
            loadingText.setText(message);

            // Apply fade-in animation
            Animation fadeIn = AnimationUtils.loadAnimation(dialog.getContext(), android.R.anim.fade_in);
            fadeIn.setDuration(300); // Fade in over 300ms
            dialog.getWindow().getDecorView().startAnimation(fadeIn);

            dialog.show();
        }
    }

    // Dismiss loading dialog
    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    // Optional: Check if dialog is showing
    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }
}
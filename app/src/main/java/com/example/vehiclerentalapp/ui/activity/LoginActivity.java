package com.example.vehiclerentalapp.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.vehiclerentalapp.R;
import com.example.vehiclerentalapp.ui.fragment.LoginFragment;

public class LoginActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        // Check if user is already logged in
        if (isUserLoggedIn()) {
            // Redirect to MainActivity and finish LoginActivity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return; // Exit onCreate early
        }

        // If not logged in, proceed with setting up the LoginActivity
        setContentView(R.layout.activity_login_signup);

        // Configure the window (status bar, etc.)
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorTertiary));

        // Light status bar (API 23+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = getWindow().getDecorView();
            int flags = decorView.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            decorView.setSystemUiVisibility(flags);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor(this, android.R.color.white));
        }

        // Load the LoginFragment into the FrameLayout
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentContainer, new LoginFragment());
            transaction.commit();
        }
    }

    // Helper method to check if user data exists in SharedPreferences
    private boolean isUserLoggedIn() {
        int userId = sharedPreferences.getInt("user_id", -1);
        int accountId = sharedPreferences.getInt("account_id", -1);
        String accountCode = sharedPreferences.getString("account_code", "N/A");
        String name = sharedPreferences.getString("name", "N/A");
        String imageUrl = sharedPreferences.getString("image_url", null);

        // Define conditions for a "logged-in" state
        // Example: userId and accountId must not be default (-1), and name must not be "N/A"
        return userId != -1 && accountId != -1 && !name.equals("N/A");
    }
}
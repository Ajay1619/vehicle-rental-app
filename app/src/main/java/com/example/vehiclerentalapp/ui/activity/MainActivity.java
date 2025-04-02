package com.example.vehiclerentalapp.ui.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vehiclerentalapp.R;
import com.example.vehiclerentalapp.utils.BottomNavigation;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Use MainActivity's layout

        // Initialize BottomNavigation logic
        BottomNavigation bottomNavigation = new BottomNavigation(this);
        bottomNavigation.setupBottomNavigation(savedInstanceState); // Let BottomNavigation handle its logic
    }
}

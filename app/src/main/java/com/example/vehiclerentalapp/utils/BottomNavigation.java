package com.example.vehiclerentalapp.utils;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.vehiclerentalapp.R;
import com.example.vehiclerentalapp.ui.fragment.CustomerMainDashboardFragment;
import com.example.vehiclerentalapp.ui.fragment.CustomerMainMainMenu;
import com.example.vehiclerentalapp.ui.fragment.CustomerMainNotificationFragment;
import com.example.vehiclerentalapp.ui.fragment.CustomerMainSettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavigation {

    private final Context context;

    public BottomNavigation(Context context) {
        this.context = context;
    }

    /**
     * Setup and configure BottomNavigationView
     */
    public void setupBottomNavigation(Bundle savedInstanceState) {
        BottomNavigationView bottomNavigationView = ((AppCompatActivity) context).findViewById(R.id.bottom_navigation_view);
        FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();

        if (savedInstanceState == null) {
            // Load default fragment only when activity is created for the first time
            fragmentManager.beginTransaction()
                    .replace(R.id.mainFragmentContainer, new CustomerMainDashboardFragment())
                    .commit();
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            handleNavigationItemClick(item);
            return true;
        });
    }

    /**
     * Handle navigation item clicks and load respective fragments
     */
    private void handleNavigationItemClick(@NonNull MenuItem item) {
        Fragment selectedFragment = null;

        if (item.getItemId() == R.id.nav_home) {
            selectedFragment = new CustomerMainDashboardFragment();
        } else if (item.getItemId() == R.id.nav_menu) {
            selectedFragment = new CustomerMainMainMenu();
        } else if (item.getItemId() == R.id.nav_notifications) {
            selectedFragment = new CustomerMainNotificationFragment();
        } else if (item.getItemId() == R.id.nav_settings) {
            selectedFragment = new CustomerMainSettingsFragment();
        }

        if (selectedFragment != null) {
            loadFragment(selectedFragment);
        }
    }

    /**
     * Load a fragment into the main fragment container
     */
    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.mainFragmentContainer, fragment);
        transaction.commit();
    }
}

package com.example.vehiclerentalapp.ui.activity;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.vehiclerentalapp.R;
import com.example.vehiclerentalapp.ui.fragment.CustomerVehiclesViewFragment;

public class CustomerVehiclesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_vehicles);

        TextView activityNameTextView = findViewById(R.id.activityName);
        activityNameTextView.setText("Vehicles");

        // Load fragment only if not restored from a previous state
        if (savedInstanceState == null) {
            loadCustomerVehiclesFragment();
        }
    }

    private void loadCustomerVehiclesFragment() {
        Fragment fragment = new CustomerVehiclesViewFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.customer_vehicles_fragment_container, fragment);
        fragmentTransaction.commit();
    }
}

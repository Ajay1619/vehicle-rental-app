package com.example.vehiclerentalapp.ui.activity;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.example.vehiclerentalapp.R;
import com.example.vehiclerentalapp.ui.fragment.CustomerBusinessViewFragment;

public class CustomerBusinessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_business);

        TextView activityNameTextView = findViewById(R.id.activityName);
        activityNameTextView.setText("Your Business");

        // Load the fragment only if activity is newly created
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.customer_business_fragment_container, new CustomerBusinessViewFragment())
                    .commit();
        }
    }

}

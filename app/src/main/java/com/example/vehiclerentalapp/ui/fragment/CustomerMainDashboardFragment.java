package com.example.vehiclerentalapp.ui.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.vehiclerentalapp.R;

public class CustomerMainDashboardFragment extends Fragment {

    private TextView tvUserId, tvAccountId, tvAccountCode, tvName, tvImageUrl;
    private ImageView ivProfileImage;
    private SharedPreferences sharedPreferences;

    public CustomerMainDashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", requireActivity().MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_customer_main_dashboard, container, false);

        // Initialize UI elements
        tvUserId = rootView.findViewById(R.id.tvUserId);
        tvAccountId = rootView.findViewById(R.id.tvAccountId);
        tvAccountCode = rootView.findViewById(R.id.tvAccountCode);
        tvName = rootView.findViewById(R.id.tvName);
        tvImageUrl = rootView.findViewById(R.id.tvImageUrl);
        ivProfileImage = rootView.findViewById(R.id.ivProfileImage);

        // Load data from SharedPreferences
        int userId = sharedPreferences.getInt("user_id", -1);
        int accountId = sharedPreferences.getInt("account_id", -1);
        String accountCode = sharedPreferences.getString("account_code", "N/A");
        String name = sharedPreferences.getString("name", "N/A");
        String imageUrl = sharedPreferences.getString("image_url", null);

        // Set TextView values
        tvUserId.setText("User ID: " + (userId != -1 ? userId : "N/A"));
        tvAccountId.setText("Account ID: " + (accountId != -1 ? accountId : "N/A"));
        tvAccountCode.setText("Account Code: " + accountCode);
        tvName.setText("Name: " + name);
        tvImageUrl.setText("Image URL: " + (imageUrl != null ? imageUrl : "N/A"));

        // Load image with Glide from the cached URL
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(getString(R.string.api_url)+"/uploads/customer_logos/"+imageUrl)
                    .placeholder(R.drawable.placeholder_image) // Add a placeholder drawable
                    .error(R.drawable.error_image) // Add an error drawable
                    .circleCrop() // Optional: make it circular
                    .into(ivProfileImage);
        } else {
            // Set a default image if no URL is available
            ivProfileImage.setImageResource(R.drawable.placeholder_image);
        }

        return rootView;
    }
}
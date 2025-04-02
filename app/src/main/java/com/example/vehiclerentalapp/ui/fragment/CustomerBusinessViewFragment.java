package com.example.vehiclerentalapp.ui.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.vehiclerentalapp.R;
import com.example.vehiclerentalapp.networks.ApiClient;
import com.example.vehiclerentalapp.networks.ApiService;
import com.example.vehiclerentalapp.models.BaseResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerBusinessViewFragment extends Fragment {
    private static final String TAG = "CustomerBusinessView";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    // UI elements
    private TextView businessNameTextView;
    private TextView customerContactName;
    private TextView valueBusinessName;
    private TextView valueBusinessRegisterNumber;
    private TextView valueGSTIN;
    private TextView valuePersonalName;
    private TextView valuePersonalContactNumber;
    private TextView valuePersonalEmailId;
    private TextView valueAddressNo;
    private TextView valueAddressStreetName;
    private TextView valueAddressLocality;
    private TextView valueAddressCity;
    private TextView valueAddressDistrict;
    private TextView valueAddressState;
    private TextView valueAddressCountry;
    private TextView valueAddressPinCode;
    private TextView valueBankHolderName;
    private TextView valueBankName;
    private TextView valueBankBranchName;
    private TextView valueBankIFSCCode;
    private TextView valueBankUPINumber;
    private ImageView logoImageView; // Add this to your existing TextView declarations
    public CustomerBusinessViewFragment() {
        // Required empty public constructor
    }

    public static CustomerBusinessViewFragment newInstance(String param1, String param2) {
        CustomerBusinessViewFragment fragment = new CustomerBusinessViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_business_view, container, false);

        // Initialize all UI elements (update IDs as per your layout)
        initializeViews(view);

        // Fetch data
        fetchCustomerBusinessDetails();

        return view;
    }

    private void initializeViews(View view) {
        businessNameTextView = view.findViewById(R.id.businessName);
        valueBusinessRegisterNumber = view.findViewById(R.id.valueBusinessRegisterNumber); // Corrected ID
        valueGSTIN = view.findViewById(R.id.valueGSTIN);
        customerContactName = view.findViewById(R.id.customerContactName);
        valueBusinessName = view.findViewById(R.id.valueBusinessName);
        valuePersonalName = view.findViewById(R.id.valuePersonalName);
        valuePersonalContactNumber = view.findViewById(R.id.valuePersonalContactNumber);
        valuePersonalEmailId = view.findViewById(R.id.valuePersonalEmailId);
        valueAddressNo = view.findViewById(R.id.valueAddressNo);
        valueAddressStreetName = view.findViewById(R.id.valueAddressStreetName);
        valueAddressLocality = view.findViewById(R.id.valueAddressLocality);
        valueAddressCity = view.findViewById(R.id.valueAddressCity);
        valueAddressDistrict = view.findViewById(R.id.valueAddressDistrict);
        valueAddressState = view.findViewById(R.id.valueAddressState);
        valueAddressCountry = view.findViewById(R.id.valueAddressCountry);
        valueAddressPinCode = view.findViewById(R.id.valueAddressPinCode);
        valueBankHolderName = view.findViewById(R.id.valueBankHolderName);
        valueBankName = view.findViewById(R.id.valueBankName);
        valueBankBranchName = view.findViewById(R.id.valueBankBranchName);
        valueBankIFSCCode = view.findViewById(R.id.valueBankIFSCCode);
        valueBankUPINumber = view.findViewById(R.id.valueBankUPINumber);
        logoImageView = view.findViewById(R.id.logoImageView);

    }

    private void fetchCustomerBusinessDetails() {
        SharedPreferences loginPrefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int accountId = loginPrefs.getInt("account_id", -1);

        if (accountId == -1) {
            Toast.makeText(requireContext(), "Account ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        Call<BaseResponse> call = apiService.getCustomerDetails(accountId);

        call.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                if (!response.isSuccessful()) {
                    handleErrorResponse(response);
                    return;
                }

                BaseResponse baseResponse = response.body();
                if (baseResponse == null || baseResponse.getCode() != 200 || !"success".equals(baseResponse.getStatus())) {
                    String errorMsg = baseResponse != null ? baseResponse.getMessage() : "Unknown error";
                    Toast.makeText(requireContext(), "Failed to fetch details: " + errorMsg, Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> data = baseResponse.getData();
                if (data == null) {
                    Toast.makeText(requireContext(), "No data returned", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d(TAG, "Data: " + data.toString());
                Map<String, Object> contactInfo = baseResponse.getNestedMap("contactInfo");
                Map<String, Object> addressInfo = baseResponse.getNestedMap("addressInfo");
                Map<String, Object> bankInfo = baseResponse.getNestedMap("bankInfo"); // Corrected key
                Map<String, Object> businessInfo = baseResponse.getNestedMap("businessInfo");

                if (businessInfo != null && !businessInfo.isEmpty()) {
                    populateFields(contactInfo, addressInfo, bankInfo, businessInfo);
                   } else {
                    Toast.makeText(requireContext(), "No business info available", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleErrorResponse(Response<BaseResponse> response) {
        try {
            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown server error";
            Toast.makeText(requireContext(), "Server error: " + errorBody, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
        }
    }

    private void populateFields(Map<String, Object> contactInfo, Map<String, Object> addressInfo,
                                Map<String, Object> bankInfo, Map<String, Object> businessInfo) {
        setText(businessNameTextView, businessInfo, "businessName");
        setText(valueBusinessName, businessInfo, "businessName");
        setText(valueBusinessRegisterNumber, businessInfo, "registerNumber");
        setText(valueGSTIN, businessInfo, "gstin", true); // Handle null explicitly
        setText(customerContactName, contactInfo, "contactName");
        setText(valuePersonalName, contactInfo, "contactName");
        setText(valuePersonalContactNumber, contactInfo, "mobileNumber");
        setText(valuePersonalEmailId, contactInfo, "emailId");
        setText(valueAddressNo, addressInfo, "doorNo");
        setText(valueAddressStreetName, addressInfo, "streetName");
        setText(valueAddressLocality, addressInfo, "locality");
        setText(valueAddressCity, addressInfo, "city");
        setText(valueAddressDistrict, addressInfo, "district");
        setText(valueAddressState, addressInfo, "state");
        setText(valueAddressCountry, addressInfo, "country");
        setText(valueAddressPinCode, addressInfo, "postalCode");
        setText(valueBankHolderName, bankInfo, "accountHolderName");
        setText(valueBankName, bankInfo, "bankName");
        setText(valueBankBranchName, bankInfo, "branchName");
        setText(valueBankIFSCCode, bankInfo, "ifscCode");
        setText(valueBankUPINumber, bankInfo, "upiNumber");

        // Load logo image
        if (logoImageView != null && businessInfo != null) {
            String imageUrl = String.valueOf(businessInfo.getOrDefault("logoPath", ""));
            Log.d("checking - ", getString(R.string.api_url)+"/uploads/customer_logos/"+imageUrl);
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this)
                        .load(getString(R.string.api_url)+"/uploads/customer_logos/"+imageUrl) // Assuming logoPath is a URL or file path
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .circleCrop() // Optional: remove if you don’t want circular cropping
                        .into(logoImageView);
            } else {
                logoImageView.setImageResource(R.drawable.placeholder_image);
            }
        }
    }

    private void setText(TextView textView, Map<String, Object> map, String key) {
        setText(textView, map, key, false);
    }

    private void setText(TextView textView, Map<String, Object> map, String key, boolean handleNull) {
        if (textView != null && map != null) {
            Object value = map.getOrDefault(key, "");
            textView.setText(handleNull && value == null ? "" : String.valueOf(value));
        }
    }
}
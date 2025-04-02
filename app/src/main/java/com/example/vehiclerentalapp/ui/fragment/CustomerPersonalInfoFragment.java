package com.example.vehiclerentalapp.ui.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.vehiclerentalapp.R;
import com.example.vehiclerentalapp.models.BaseResponse;
import com.example.vehiclerentalapp.models.CustomerRegistration;
import com.example.vehiclerentalapp.networks.ApiClient;
import com.example.vehiclerentalapp.networks.ApiService;
import com.example.vehiclerentalapp.utils.LoadingScreen;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerPersonalInfoFragment extends Fragment {

    private TextInputEditText customerContactNameTextField, customerUsernameTextField, customerEmailTextField;
    private LoadingScreen loadingScreen;

    public CustomerPersonalInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_personal_info, container, false);

        // Initialize LoadingScreen
        loadingScreen = new LoadingScreen(requireContext());

        // Initialize UI elements
        customerContactNameTextField = view.findViewById(R.id.customer_contact_name_textfield);
        customerUsernameTextField = view.findViewById(R.id.customer_username_textfield); // Used for contact number
        customerEmailTextField = view.findViewById(R.id.customer_email_textfield);
        MaterialButton btnSubmit = view.findViewById(R.id.CustomerPersonalBtnSubmit);

        // Fetch customer details when the fragment loads
        fetchCustomerDetails();

        // Set click listener for the submit button
        btnSubmit.setOnClickListener(v -> customerPersonalInfoSubmitForm());

        return view;
    }

    private void fetchCustomerDetails() {
        SharedPreferences registrationPrefs = requireActivity().getSharedPreferences("RegistrationPrefs", Context.MODE_PRIVATE);
        int accountId = registrationPrefs.getInt("account_id", -1);

        if (accountId == -1) {
            Toast.makeText(getContext(), "Account ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingScreen.show("Fetching Details...");

        ApiService apiService = ApiClient.getApiService();
        Call<BaseResponse> call = apiService.getCustomerDetails(accountId);

        call.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {

                if (response.isSuccessful()) {
                    BaseResponse baseResponse = response.body();
                    if (baseResponse != null && baseResponse.getCode() == 200 && "success".equals(baseResponse.getStatus())) {
                        Map<String, Object> data = baseResponse.getData();
                        if (data != null) {
                            Map<String, Object> contactInfo = baseResponse.getNestedMap("contactInfo");
                            if (contactInfo != null) {
                                populateFields(contactInfo);
                            }
                        }

                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Toast.makeText(getContext(), "Server error: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                }

                loadingScreen.dismiss();

            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                loadingScreen.dismiss();
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateFields(Map<String, Object> contactInfo) {
        Object contactName = contactInfo.get("contactName");
        Object mobileNumber = contactInfo.get("mobileNumber");
        Object emailId = contactInfo.get("emailId");

        if (contactName instanceof String) {
            customerContactNameTextField.setText((String) contactName);
        }
        if (mobileNumber instanceof String) {
            customerUsernameTextField.setText((String) mobileNumber);
        }
        if (emailId instanceof String) {
            customerEmailTextField.setText((String) emailId);
        }
    }

    // Validation method for phone number
    private String isPhoneNumber(String phone) {
        // Check if the phone number starts with 6-9 and is exactly 10 digits long
        if (phone.matches("^[6-9]\\d{9}$")) {
            return "true";
        } else {
            return "Please enter a valid Mobile number.";
        }
    }

    // Validation method for email
    private String isEmail(String email) {
        // Check if the email follows a basic pattern: non-empty string@domain.tld
        if (email.matches("^\\S+@[\\w\\d.-]{2,}\\.[\\w]{2,6}$")) {
            return "true";
        } else {
            return "Please enter a valid email address.";
        }
    }
    private void customerPersonalInfoSubmitForm() {
        // Fetch values from TextInputEditText fields
        String contactName = customerContactNameTextField.getText() != null ? customerContactNameTextField.getText().toString().trim() : "";
        String mobileNumber = customerUsernameTextField.getText() != null ? customerUsernameTextField.getText().toString().trim() : "";
        String emailId = customerEmailTextField.getText() != null ? customerEmailTextField.getText().toString().trim() : "";

        // Client-side validation
        if (contactName.isEmpty() || mobileNumber.isEmpty() || emailId.isEmpty()) {
            Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate phone number
        String phoneValidationResult = isPhoneNumber(mobileNumber);
        if (!"true".equals(phoneValidationResult)) {
            Toast.makeText(getContext(), phoneValidationResult, Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate email
        String emailValidationResult = isEmail(emailId);
        if (!"true".equals(emailValidationResult)) {
            Toast.makeText(getContext(), emailValidationResult, Toast.LENGTH_SHORT).show();
            return;
        }

        // Get accountId from SharedPreferences
        SharedPreferences registrationPrefs = requireActivity().getSharedPreferences("RegistrationPrefs", Context.MODE_PRIVATE);
        int accountId = registrationPrefs.getInt("account_id", -1);

        if (accountId == -1) {
            Toast.makeText(getContext(), "Account ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create CustomerRegistration object with ContactInfo
        CustomerRegistration customer = new CustomerRegistration();
        customer.setContactInfo(new CustomerRegistration.ContactInfo(contactName, mobileNumber, emailId));

        // Show loading screen
        loadingScreen.show("Submitting Details...");

        // Make API call to update customer personal info
        ApiService apiService = ApiClient.getApiService();
        Call<BaseResponse> call = apiService.updateCustomerPersonalInfo(accountId, customer);

        call.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                if (response.isSuccessful()) {
                    BaseResponse baseResponse = response.body();
                    if (baseResponse != null && baseResponse.getCode() == 200 && "success".equals(baseResponse.getStatus())) {
                        loadingScreen.dismiss();
                        // Navigate to the next fragment on success
                        Fragment newFragment = new CustomerAddressInfoFragment();
                        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragmentContainer, newFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    } else {
                        String errorMsg = baseResponse != null ? baseResponse.getMessage() : "Unknown error";
                        loadingScreen.dismiss();
                        Toast.makeText(getContext(), "Submission failed: " + errorMsg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Toast.makeText(getContext(), "Server error: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                    loadingScreen.dismiss();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                loadingScreen.dismiss();
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
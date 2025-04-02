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

public class CustomerBankInfoFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private TextInputEditText accountHolderNameTextField, bankNameTextField, bankBranchNameTextField,
            ifscCodeTextField, upiNumberTextField, bankAccountNumberTextField; // Added new field
    private LoadingScreen loadingScreen;

    public CustomerBankInfoFragment() {
        // Required empty public constructor
    }

    public static CustomerBankInfoFragment newInstance(String param1, String param2) {
        CustomerBankInfoFragment fragment = new CustomerBankInfoFragment();
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_bank_info, container, false);

        // Initialize LoadingScreen
        loadingScreen = new LoadingScreen(requireContext());

        // Initialize UI elements
        accountHolderNameTextField = view.findViewById(R.id.account_holder_name_textfield);
        bankNameTextField = view.findViewById(R.id.bank_name_textfield);
        bankBranchNameTextField = view.findViewById(R.id.bank_branch_name_textfield);
        ifscCodeTextField = view.findViewById(R.id.ifsc_code_textfield);
        upiNumberTextField = view.findViewById(R.id.upi_number_textfield);
        bankAccountNumberTextField = view.findViewById(R.id.bank_account_number_textfield); // Added new field
        MaterialButton btnSubmit = view.findViewById(R.id.CustomerBankBtnSubmit);

        // Fetch bank details when the fragment loads
        fetchCustomerBankDetails();

        // Set click listener for the submit button
        btnSubmit.setOnClickListener(v -> submitForm());

        return view;
    }

    private void fetchCustomerBankDetails() {
        SharedPreferences registrationPrefs = requireActivity().getSharedPreferences("RegistrationPrefs", Context.MODE_PRIVATE);
        int accountId = registrationPrefs.getInt("account_id", -1);

        if (accountId == -1) {
            Toast.makeText(requireContext(), "Account ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingScreen.show("Fetching Bank Details...");

        ApiService apiService = ApiClient.getApiService();
        Call<BaseResponse> call = apiService.getCustomerDetails(accountId); // Updated to use specific endpoint

        call.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                loadingScreen.dismiss();
                if (response.isSuccessful()) {
                    BaseResponse baseResponse = response.body();
                    if (baseResponse != null && baseResponse.getCode() == 200 && "success".equals(baseResponse.getStatus())) {
                        Map<String, Object> data = baseResponse.getData();
                        if (data != null) {
                            Map<String, Object> bankInfo = baseResponse.getNestedMap("bankInfo");
                            if (bankInfo != null && !bankInfo.isEmpty()) {
                                populateFields(bankInfo);
                                Toast.makeText(requireContext(), "Bank details fetched successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "No bank info returned", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(requireContext(), "No data returned", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String errorMsg = baseResponse != null ? baseResponse.getMessage() : "Unknown error";
                        Toast.makeText(requireContext(), "Failed to fetch bank details: " + errorMsg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Toast.makeText(requireContext(), "Server error: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                loadingScreen.dismiss();
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateFields(Map<String, Object> bankInfo) {
        Object accountHolderName = bankInfo.get("accountHolderName");
        Object bankName = bankInfo.get("bankName");
        Object branchName = bankInfo.get("branchName");
        Object ifscCode = bankInfo.get("ifscCode");
        Object upiNumber = bankInfo.get("upiNumber");
        Object accountNumber = bankInfo.get("accountNumber"); // Added new field

        if (accountHolderName instanceof String) accountHolderNameTextField.setText((String) accountHolderName);
        if (bankName instanceof String) bankNameTextField.setText((String) bankName);
        if (branchName instanceof String) bankBranchNameTextField.setText((String) branchName);
        if (ifscCode instanceof String) ifscCodeTextField.setText((String) ifscCode);
        if (upiNumber instanceof String) upiNumberTextField.setText((String) upiNumber);
        if (accountNumber instanceof String) bankAccountNumberTextField.setText((String) accountNumber); // Populate new field
    }

    private void submitForm() {
        // Fetch values from TextInputEditText fields
        String accountHolderName = accountHolderNameTextField.getText() != null ? accountHolderNameTextField.getText().toString().trim() : "";
        String bankName = bankNameTextField.getText() != null ? bankNameTextField.getText().toString().trim() : "";
        String branchName = bankBranchNameTextField.getText() != null ? bankBranchNameTextField.getText().toString().trim() : "";
        String ifscCode = ifscCodeTextField.getText() != null ? ifscCodeTextField.getText().toString().trim() : "";
        String upiNumber = upiNumberTextField.getText() != null ? upiNumberTextField.getText().toString().trim() : "";
        String accountNumber = bankAccountNumberTextField.getText() != null ? bankAccountNumberTextField.getText().toString().trim() : ""; // Added new field

        // Client-side validation
        if (accountHolderName.isEmpty() || bankName.isEmpty() || branchName.isEmpty() || ifscCode.isEmpty() ||
                upiNumber.isEmpty() || accountNumber.isEmpty()) {
            Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get accountId from SharedPreferences
        SharedPreferences registrationPrefs = requireActivity().getSharedPreferences("RegistrationPrefs", Context.MODE_PRIVATE);
        int accountId = registrationPrefs.getInt("account_id", -1);

        if (accountId == -1) {
            Toast.makeText(requireContext(), "Account ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create CustomerRegistration object with BankInfo
        CustomerRegistration customer = new CustomerRegistration();
        customer.setBankInfo(new CustomerRegistration.BankInfo(accountHolderName, bankName, branchName, ifscCode, upiNumber, accountNumber));

        // Show loading screen
        loadingScreen.show("Submitting Bank Details...");

        // Make API call to update customer bank info
        ApiService apiService = ApiClient.getApiService();
        Call<BaseResponse> call = apiService.updateCustomerBankInfo(accountId, customer);

        call.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                loadingScreen.dismiss();
                if (response.isSuccessful()) {
                    BaseResponse baseResponse = response.body();
                    if (baseResponse != null && baseResponse.getCode() == 200 && "success".equals(baseResponse.getStatus())) {
                        Toast.makeText(requireContext(), baseResponse.getMessage(), Toast.LENGTH_SHORT).show();

                        // Navigate to the next fragment on success
                        Fragment newFragment = new CustomerBusinessInfoFragment();
                        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragmentContainer, newFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    } else {
                        String errorMsg = baseResponse != null ? baseResponse.getMessage() : "Unknown error";
                        Toast.makeText(requireContext(), "Submission failed: " + errorMsg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Toast.makeText(requireContext(), "Server error: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                loadingScreen.dismiss();
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
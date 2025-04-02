package com.example.vehiclerentalapp.ui.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerAccountInfoFragment extends Fragment {

    private TextInputEditText usernameEditText, passwordEditText, confirmPasswordEditText;
    private MaterialButton btnSubmit;
    private SharedPreferences registration_prefs;

    public CustomerAccountInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_account_info, container, false);

        // Initialize UI elements
        usernameEditText = view.findViewById(R.id.customer_username_textfield);
        passwordEditText = view.findViewById(R.id.customer_password_textfield);
        confirmPasswordEditText = view.findViewById(R.id.customer_confirm_password_textfield);
        btnSubmit = view.findViewById(R.id.btnSubmit);

        // Initialize SharedPreferences
        registration_prefs = requireActivity().getSharedPreferences("RegistrationPrefs", Context.MODE_PRIVATE);

        // Set the OnFocusChangeListener (onBlur equivalent)
        passwordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    validatePassword(Objects.requireNonNull(passwordEditText.getText()).toString(), false);
                }
            }
        });

        // Set click listener for the submit button
        btnSubmit.setOnClickListener(v -> customerAccountSubmitForm());

        return view;
    }

    private boolean validatePassword(String password, boolean blockSubmission) {
        List<String> errors = new ArrayList<>();
        if (password.length() < 8) {
            errors.add("Password must be at least 8 characters long.");
        }
        if (!password.matches(".*[a-z].*")) {
            errors.add("Password must contain at least one lowercase letter.");
        }
        if (!password.matches(".*[A-Z].*")) {
            errors.add("Password must contain at least one uppercase letter.");
        }
        if (!password.matches(".*[\\W_].*")) {
            errors.add("Password must contain at least one symbol (e.g., !@#$%^&*).");
        }
        if (!errors.isEmpty()) {
            showErrorsSequentially(errors, 0);
            return false;
        }
        return true;
    }

    private void showErrorsSequentially(final List<String> errors, final int index) {
        if (index >= errors.size()) {
            return;
        }
        Toast.makeText(getContext(), errors.get(index), Toast.LENGTH_SHORT).show();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                showErrorsSequentially(errors, index + 1);
            }
        }, 2000);
    }

    private void customerAccountSubmitForm() {
        String username = usernameEditText.getText() != null ? usernameEditText.getText().toString().trim() : "";
        String password = passwordEditText.getText() != null ? passwordEditText.getText().toString().trim() : "";
        String confirmPassword = confirmPasswordEditText.getText() != null ? confirmPasswordEditText.getText().toString().trim() : "";

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!validatePassword(password, true)) {
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        CustomerRegistration customer = new CustomerRegistration();
        customer.setAccountInfo(new CustomerRegistration.AccountInfo(username, password));

        LoadingScreen loadingScreen = new LoadingScreen(requireContext());
        loadingScreen.show("Loading...");

        ApiService apiService = ApiClient.getApiService();
        Call<BaseResponse> call = apiService.registerCustomer(customer);
        call.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {

                if (response.isSuccessful()) {
                    BaseResponse baseResponse = response.body();
                    Log.d("CustomerAccountInfo", "Response received: " + (baseResponse != null ? baseResponse.getMessage() : "null"));
                    if (baseResponse != null && baseResponse.getCode() == 200 && "success".equals(baseResponse.getStatus())) {
                        Toast.makeText(getContext(), baseResponse.getMessage(), Toast.LENGTH_SHORT).show();

                        Map<String, Object> data = baseResponse.getData();
                        if (data != null) {
                            Integer accountId = baseResponse.getInt("account_id");
                            Log.d("CustomerAccountInfo", "Account ID: " + accountId);
                            if (accountId != null) {
                                SharedPreferences.Editor editor = registration_prefs.edit();
                                editor.putInt("account_id", accountId);
                                editor.apply();
                                loadingScreen.dismiss();
                                Fragment newFragment = new CustomerPersonalInfoFragment();
                                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                                transaction.replace(R.id.fragmentContainer, newFragment);
                                transaction.addToBackStack(null);
                                transaction.commit();
                            } else {
                                Toast.makeText(getContext(), "Registration failed: No account ID returned", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "Registration failed: No data returned", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String errorMsg = baseResponse != null ? baseResponse.getMessage() : "Unknown error";
                        Toast.makeText(getContext(), "Registration failed: " + errorMsg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.d("CustomerAccountInfo", "Server error body: " + errorBody);
                        Toast.makeText(getContext(), "Server error: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.d("CustomerAccountInfo", "Error parsing error body: " + e.getMessage());
                        Toast.makeText(getContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                loadingScreen.dismiss();
                Log.d("CustomerAccountInfo", "Network error: " + t.getMessage());
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(null);
        }
    }
}
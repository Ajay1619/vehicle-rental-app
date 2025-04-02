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
import com.example.vehiclerentalapp.models.GuestRegistration;
import com.example.vehiclerentalapp.networks.ApiClient;
import com.example.vehiclerentalapp.networks.ApiService;
import com.example.vehiclerentalapp.utils.LoadingScreen;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GuestAccountInfoFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private TextInputEditText usernameEditText, passwordEditText, confirmPasswordEditText;
    private LoadingScreen loadingScreen;
    private SharedPreferences registrationPrefs;

    public GuestAccountInfoFragment() {
        // Required empty public constructor
    }

    public static GuestAccountInfoFragment newInstance(String param1, String param2) {
        GuestAccountInfoFragment fragment = new GuestAccountInfoFragment();
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
        View view = inflater.inflate(R.layout.fragment_guest_account_info, container, false);

        // Initialize LoadingScreen
        loadingScreen = new LoadingScreen(requireContext());

        // Initialize UI elements
        usernameEditText = view.findViewById(R.id.guest_username_textfield);
        passwordEditText = view.findViewById(R.id.guest_password_textfield);
        confirmPasswordEditText = view.findViewById(R.id.guest_confirm_password_textfield);
        MaterialButton btnSubmit = view.findViewById(R.id.guestAccountBtnSubmit);

        // Initialize SharedPreferences
        registrationPrefs = requireActivity().getSharedPreferences("RegistrationPrefs", Context.MODE_PRIVATE);

        // Set click listener for the submit button
        btnSubmit.setOnClickListener(v -> submitForm());

        return view;
    }

    private void submitForm() {
        // Fetch values from TextInputEditText fields
        String username = usernameEditText.getText() != null ? usernameEditText.getText().toString().trim() : "";
        String password = passwordEditText.getText() != null ? passwordEditText.getText().toString().trim() : "";
        String confirmPassword = confirmPasswordEditText.getText() != null ? confirmPasswordEditText.getText().toString().trim() : "";

        // Client-side validation
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create GuestRegistration object with AccountInfo
        GuestRegistration guest = new GuestRegistration();
        GuestRegistration.AccountInfo accountInfo = new GuestRegistration.AccountInfo(
                username,
                password,
                2, // 2 for Guest as per your comment
                null, // Example account_code, adjust as needed
                null, // master_key (optional)
                null, // salt_value (optional)
                null, // iterations_value (optional)
                null  // iv_value (optional)
        );
        guest.setAccountInfo(accountInfo);

        // Show loading screen
        loadingScreen.show("Submitting Account Details...");

        // Make API call to register guest account info
        ApiService apiService = ApiClient.getApiService();
        Call<BaseResponse> call = apiService.registerGuestAccount(guest);

        call.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {

                if (response.isSuccessful()) {
                    BaseResponse baseResponse = response.body();
                    if (baseResponse != null && baseResponse.getCode() == 200 && "success".equals(baseResponse.getStatus())) {
                        Toast.makeText(requireContext(), baseResponse.getMessage(), Toast.LENGTH_SHORT).show();

                        // Save account_id to SharedPreferences if returned
                        Map<String, Object> data = baseResponse.getData();
                        if (data != null && data.containsKey("account_id")) {
                            Integer accountId = baseResponse.getInt("account_id");
                            if (accountId != null) {
                                SharedPreferences.Editor editor = registrationPrefs.edit();
                                editor.putInt("account_id", accountId);
                                editor.apply();
                            }
                        }
                        loadingScreen.dismiss();
                        // Navigate to the next fragment on success
                        Fragment newFragment = new GuestPersonalInfoFragment();
                        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragmentContainer, newFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    } else {
                        String errorMsg = baseResponse != null ? baseResponse.getMessage() : "Unknown error";
                        loadingScreen.dismiss();
                        Toast.makeText(requireContext(), "Submission failed: " + errorMsg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Toast.makeText(requireContext(), "Server error: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                    loadingScreen.dismiss();
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
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GuestPersonalInfoFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private TextInputEditText guestNameTextField, guestMobileNumberTextField, guestEmailTextField;
    private LoadingScreen loadingScreen;
    private SharedPreferences registrationPrefs;

    public GuestPersonalInfoFragment() {
        // Required empty public constructor
    }

    public static GuestPersonalInfoFragment newInstance(String param1, String param2) {
        GuestPersonalInfoFragment fragment = new GuestPersonalInfoFragment();
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
        View view = inflater.inflate(R.layout.fragment_guest_personal_info, container, false);

        // Initialize LoadingScreen
        loadingScreen = new LoadingScreen(requireContext());

        // Initialize UI elements
        guestNameTextField = view.findViewById(R.id.guest_contact_name_textfield);
        guestMobileNumberTextField = view.findViewById(R.id.guest_contact_number_textfield);
        guestEmailTextField = view.findViewById(R.id.guest_email_textfield);
        MaterialButton btnSubmit = view.findViewById(R.id.guestPersonalInfoBtnSubmit);

        // Initialize SharedPreferences
        registrationPrefs = requireActivity().getSharedPreferences("RegistrationPrefs", Context.MODE_PRIVATE);

        // Fetch guest personal details when the fragment loads
        fetchGuestPersonalDetails();

        // Set click listener for the submit button
        btnSubmit.setOnClickListener(v -> submitForm());

        return view;
    }

    private void fetchGuestPersonalDetails() {
        int accountId = registrationPrefs.getInt("account_id", -1);

        if (accountId == -1) {
            Toast.makeText(requireContext(), "Account ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingScreen.show("Fetching Personal Details...");

        ApiService apiService = ApiClient.getApiService();
        Call<BaseResponse> call = apiService.getGuestDetails(accountId);

        call.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {

                if (response.isSuccessful()) {
                    BaseResponse baseResponse = response.body();
                    if (baseResponse != null && baseResponse.getCode() == 200 && "success".equals(baseResponse.getStatus())) {
                        Map<String, Object> data = baseResponse.getData();
                        if (data != null) {
                            Map<String, Object> personalInfo = baseResponse.getNestedMap("personalInfo");
                            if (personalInfo != null && !personalInfo.isEmpty()) {
                                populateFields(personalInfo);
                                Toast.makeText(requireContext(), "Guest details fetched successfully", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Toast.makeText(requireContext(), "Server error: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                }
                loadingScreen.dismiss();
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                loadingScreen.dismiss();
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateFields(Map<String, Object> personalInfo) {
        Object guestName = personalInfo.get("guest_name");
        Object guestMobileNumber = personalInfo.get("guest_mobile_number");
        Object guestEmailId = personalInfo.get("guest_email_id");

        if (guestName instanceof String) guestNameTextField.setText((String) guestName);
        if (guestMobileNumber instanceof String) guestMobileNumberTextField.setText((String) guestMobileNumber);
        if (guestEmailId instanceof String) guestEmailTextField.setText((String) guestEmailId);
    }

    private void submitForm() {
        // Fetch values from TextInputEditText fields
        String guestName = guestNameTextField.getText() != null ? guestNameTextField.getText().toString().trim() : "";
        String guestMobileNumber = guestMobileNumberTextField.getText() != null ? guestMobileNumberTextField.getText().toString().trim() : "";
        String guestEmailId = guestEmailTextField.getText() != null ? guestEmailTextField.getText().toString().trim() : "";

        // Client-side validation
        if (guestName.isEmpty() || guestMobileNumber.isEmpty() || guestEmailId.isEmpty()) {
            Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get accountId from SharedPreferences
        int accountId = registrationPrefs.getInt("account_id", -1);
        if (accountId == -1) {
            Toast.makeText(requireContext(), "Account ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create GuestRegistration object with PersonalInfo
        GuestRegistration guest = new GuestRegistration();
        GuestRegistration.PersonalInfo personalInfo = new GuestRegistration.PersonalInfo(
                guestName,
                guestMobileNumber,
                guestEmailId
        );
        guest.setPersonalInfo(personalInfo);

        // Show loading screen
        loadingScreen.show("Submitting Personal Details...");

        // Make API call to update guest personal info
        ApiService apiService = ApiClient.getApiService();
        Call<BaseResponse> call = apiService.updateGuestPersonalInfo(accountId, guest);

        call.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {

                if (response.isSuccessful()) {
                    BaseResponse baseResponse = response.body();
                    if (baseResponse != null && baseResponse.getCode() == 200 && "success".equals(baseResponse.getStatus())) {
                        Toast.makeText(requireContext(), baseResponse.getMessage(), Toast.LENGTH_SHORT).show();

                        // Navigate to the next fragment on success (e.g., a confirmation or login screen)
                        Fragment newFragment = new LoginFragment(); // Replace with actual next fragment
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
                loadingScreen.dismiss();
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                loadingScreen.dismiss();
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
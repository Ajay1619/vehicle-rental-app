package com.example.vehiclerentalapp.ui.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.vehiclerentalapp.R;
import com.example.vehiclerentalapp.models.BaseResponse;
import com.example.vehiclerentalapp.networks.ApiClient;
import com.example.vehiclerentalapp.networks.ApiService;
import com.example.vehiclerentalapp.utils.LoadingScreen;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerBusinessInfoFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private TextInputEditText businessNameTextField, businessRegisterNumberTextField, gstinTextField;
    private TextView uploadedLogoNameTextView, uploadedLicenseNameTextView;
    private LoadingScreen loadingScreen;
    private Uri logoFileUri; // For file upload
    private Uri licenseFileUri; // For file upload
    private String serverLogoPath = ""; // For displaying fetched path
    private String serverLicensePath = ""; // For displaying fetched path
    private String logoFileName = ""; // Store simplified file name
    private String licenseFileName = ""; // Store simplified file name

    private ActivityResultLauncher<String> logoPickerLauncher;
    private ActivityResultLauncher<String> licensePickerLauncher;

    public CustomerBusinessInfoFragment() {
        // Required empty public constructor
    }

    public static CustomerBusinessInfoFragment newInstance(String param1, String param2) {
        CustomerBusinessInfoFragment fragment = new CustomerBusinessInfoFragment();
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

        // Initialize file picker launchers
        logoPickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                logoFileUri = uri;
                // Extract a simplified file name (e.g., "logo" + extension)
                String path = uri.getLastPathSegment();
                logoFileName = "logo_" + System.currentTimeMillis() + "." + getFileExtension(uri); // Custom name
                uploadedLogoNameTextView.setText(logoFileName);
            }
        });

        licensePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                licenseFileUri = uri;
                // Extract a simplified file name (e.g., "license" + extension)
                String path = uri.getLastPathSegment();
                licenseFileName = "license_" + System.currentTimeMillis() + "." + getFileExtension(uri); // Custom name
                uploadedLicenseNameTextView.setText(licenseFileName);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_business_info, container, false);

        // Initialize LoadingScreen
        loadingScreen = new LoadingScreen(requireContext());

        // Initialize UI elements
        businessNameTextField = view.findViewById(R.id.business_name_textfield);
        businessRegisterNumberTextField = view.findViewById(R.id.business_register_number_textfield);
        gstinTextField = view.findViewById(R.id.gstin_textfield);
        uploadedLogoNameTextView = view.findViewById(R.id.uploadedImageName);
        uploadedLicenseNameTextView = view.findViewById(R.id.uploadedFileName);
        MaterialButton btnBrowseLogo = view.findViewById(R.id.btnBrowseFile);
        MaterialButton btnBrowseLicense = view.findViewById(R.id.btnBrowseFileFile);
        MaterialButton btnSubmit = view.findViewById(R.id.btnSubmit);

        // Set click listeners for file uploads
        btnBrowseLogo.setOnClickListener(v -> logoPickerLauncher.launch("image/*"));
        btnBrowseLicense.setOnClickListener(v -> licensePickerLauncher.launch("*/*"));

        // Fetch business details when the fragment loads
        fetchCustomerBusinessDetails();

        // Set click listener for the submit button
        btnSubmit.setOnClickListener(v -> submitForm());

        return view;
    }

    private void fetchCustomerBusinessDetails() {
        SharedPreferences registrationPrefs = requireActivity().getSharedPreferences("RegistrationPrefs", Context.MODE_PRIVATE);
        int accountId = registrationPrefs.getInt("account_id", -1);

        if (accountId == -1) {
            Toast.makeText(requireContext(), "Account ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingScreen.show("Fetching Business Details...");

        ApiService apiService = ApiClient.getApiService();
        Call<BaseResponse> call = apiService.getCustomerDetails(accountId);

        call.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                loadingScreen.dismiss();
                if (response.isSuccessful()) {
                    BaseResponse baseResponse = response.body();
                    if (baseResponse != null && baseResponse.getCode() == 200 && "success".equals(baseResponse.getStatus())) {
                        Map<String, Object> data = baseResponse.getData();
                        if (data != null) {
                            Map<String, Object> businessInfo = baseResponse.getNestedMap("businessInfo");
                            if (businessInfo != null && !businessInfo.isEmpty()) {
                                populateFields(businessInfo);
                                Toast.makeText(requireContext(), "Business details fetched successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "No business info returned", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(requireContext(), "No data returned", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String errorMsg = baseResponse != null ? baseResponse.getMessage() : "Unknown error";
                        Toast.makeText(requireContext(), "Failed to fetch business details: " + errorMsg, Toast.LENGTH_SHORT).show();
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

    private void populateFields(Map<String, Object> businessInfo) {
        Object businessName = businessInfo.get("businessName");
        Object registerNumber = businessInfo.get("registerNumber");
        Object gstin = businessInfo.get("gstin");
        Object logoPath = businessInfo.get("logoPath");
        Object licensePath = businessInfo.get("licensePath");

        if (businessName instanceof String) businessNameTextField.setText((String) businessName);
        if (registerNumber instanceof String) businessRegisterNumberTextField.setText((String) registerNumber);
        if (gstin instanceof String) gstinTextField.setText((String) gstin);
        if (logoPath instanceof String) {
            serverLogoPath = (String) logoPath;
            uploadedLogoNameTextView.setText(((String) logoPath).substring(((String) logoPath).lastIndexOf("/") + 1));
        }
        if (licensePath instanceof String) {
            serverLicensePath = (String) licensePath;
            uploadedLicenseNameTextView.setText(((String) licensePath).substring(((String) licensePath).lastIndexOf("/") + 1));
        }
    }

    private String getFileExtension(Uri uri) {
        String mimeType = requireContext().getContentResolver().getType(uri);
        if (mimeType != null) {
            return mimeType.substring(mimeType.lastIndexOf("/") + 1);
        }
        return "unknown"; // Fallback extension
    }

    private File getFileFromUri(Uri uri, String fileName) {
        File file = new File(requireContext().getCacheDir(), fileName);
        try (java.io.InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
             java.io.FileOutputStream outputStream = new java.io.FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error processing file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return file;
    }

    private void submitForm() {
        // Fetch values from TextInputEditText fields
        String businessName = businessNameTextField.getText() != null ? businessNameTextField.getText().toString().trim() : "";
        String registerNumber = businessRegisterNumberTextField.getText() != null ? businessRegisterNumberTextField.getText().toString().trim() : "";
        String gstin = gstinTextField.getText() != null ? gstinTextField.getText().toString().trim() : "";

        // Client-side validation
        if (businessName.isEmpty() || registerNumber.isEmpty()) {
            Toast.makeText(requireContext(), "Business Name and Register Number are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get accountId from SharedPreferences
        SharedPreferences registrationPrefs = requireActivity().getSharedPreferences("RegistrationPrefs", Context.MODE_PRIVATE);
        int accountId = registrationPrefs.getInt("account_id", -1);

        if (accountId == -1) {
            Toast.makeText(requireContext(), "Account ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading screen
        loadingScreen.show("Submitting Business Details...");

        // Prepare form data
        RequestBody accountIdPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(accountId));
        RequestBody businessNamePart = RequestBody.create(MediaType.parse("text/plain"), businessName);
        RequestBody registerNumberPart = RequestBody.create(MediaType.parse("text/plain"), registerNumber);
        RequestBody gstinPart = RequestBody.create(MediaType.parse("text/plain"), gstin);

        // Prepare file parts (if selected)
        MultipartBody.Part logoPart = null;
        if (logoFileUri != null) {
            File logoFile = getFileFromUri(logoFileUri, logoFileName); // Use simplified file name
            RequestBody logoRequestBody = RequestBody.create(MediaType.parse("image/*"), logoFile);
            logoPart = MultipartBody.Part.createFormData("logoFile", logoFileName, logoRequestBody);
        } else if (!serverLogoPath.isEmpty()) {
            RequestBody logoPathBody = RequestBody.create(MediaType.parse("text/plain"), serverLogoPath);
            logoPart = MultipartBody.Part.createFormData("logoFile", serverLogoPath.substring(serverLogoPath.lastIndexOf("/") + 1), logoPathBody);
        }

        MultipartBody.Part licensePart = null;
        if (licenseFileUri != null) {
            File licenseFile = getFileFromUri(licenseFileUri, licenseFileName); // Use simplified file name
            RequestBody licenseRequestBody = RequestBody.create(MediaType.parse("*/*"), licenseFile);
            licensePart = MultipartBody.Part.createFormData("licenseFile", licenseFileName, licenseRequestBody);
        } else if (!serverLicensePath.isEmpty()) {
            RequestBody licensePathBody = RequestBody.create(MediaType.parse("text/plain"), serverLicensePath);
            licensePart = MultipartBody.Part.createFormData("licenseFile", serverLicensePath.substring(serverLicensePath.lastIndexOf("/") + 1), licensePathBody);
        }

        // Make API call to update customer business info with files
        ApiService apiService = ApiClient.getApiService();
        Call<BaseResponse> call = apiService.updateCustomerBusinessInfo(accountIdPart, businessNamePart, registerNumberPart, gstinPart, logoPart, licensePart);

        call.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                loadingScreen.dismiss();
                if (response.isSuccessful()) {
                    BaseResponse baseResponse = response.body();
                    if (baseResponse != null && baseResponse.getCode() == 200 && "success".equals(baseResponse.getStatus())) {
                        Toast.makeText(requireContext(), baseResponse.getMessage(), Toast.LENGTH_SHORT).show();

                        // Navigate to the next fragment on success
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
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                loadingScreen.dismiss();
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
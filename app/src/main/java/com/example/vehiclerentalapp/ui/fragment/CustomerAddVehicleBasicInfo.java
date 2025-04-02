package com.example.vehiclerentalapp.ui.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.vehiclerentalapp.R;
import com.example.vehiclerentalapp.databinding.FragmentCustomerAddVehicleBasicInfoBinding;
import com.example.vehiclerentalapp.models.BaseResponse;
import com.example.vehiclerentalapp.models.Vehicle;
import com.example.vehiclerentalapp.models.VehicleBrand;
import com.example.vehiclerentalapp.networks.ApiClient;
import com.example.vehiclerentalapp.networks.ApiService;
import com.example.vehiclerentalapp.utils.LoadingScreen;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerAddVehicleBasicInfo extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "CustomerAddVehicleBasicInfo";
    private String mParam1;
    private String mParam2;
    private FragmentCustomerAddVehicleBasicInfoBinding binding;
    private LoadingScreen loadingScreen;
    private int vehicleId = -1; // -1 if new vehicle
    private int prVehicleId = -1; // Previous vehicle ID for updates
    private Vehicle currentVehicle;
    private Map<String, Byte> vehicleTypeMap;
    private Map<String, Byte> vehicleCategoryMap;
    private List<VehicleBrand> vehicleBrands;
    private ArrayAdapter<String> brandAdapter;

    // File upload fields
    private Uri registrationCertificateUri; // For new local file
    private String registrationCertificateFileName = "";
    private String serverRegistrationCertificatePath = ""; // Server path
    private ActivityResultLauncher<String> certificatePickerLauncher;

    public CustomerAddVehicleBasicInfo() {}

    public static CustomerAddVehicleBasicInfo newInstance(String param1, String param2, int vehicleId) {
        CustomerAddVehicleBasicInfo fragment = new CustomerAddVehicleBasicInfo();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putInt("vehicleId", vehicleId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            vehicleId = getArguments().getInt("vehicleId", -1);
            prVehicleId = vehicleId;
        }

        certificatePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                registrationCertificateUri = uri;
                registrationCertificateFileName = "certificate_" + System.currentTimeMillis() + "." + getFileExtension(uri);
                binding.uploadedFileName.setText(registrationCertificateFileName);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCustomerAddVehicleBasicInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadingScreen = new LoadingScreen(requireContext());
        MaterialButton btnSubmit = view.findViewById(R.id.customerAddVehicleBasicInfoSubmitBtn);

        // Initialize maps
        vehicleTypeMap = new HashMap<>();
        vehicleTypeMap.put("Bike", (byte) 1);
        vehicleTypeMap.put("Car", (byte) 2);
        vehicleTypeMap.put("Bicycle", (byte) 3);

        vehicleCategoryMap = new HashMap<>();
        vehicleCategoryMap.put("Petrol", (byte) 1);
        vehicleCategoryMap.put("Diesel", (byte) 2);
        vehicleCategoryMap.put("CNG", (byte) 3);
        vehicleCategoryMap.put("Electric", (byte) 4);
        vehicleCategoryMap.put("Hybrid", (byte) 5);
        vehicleCategoryMap.put("None", (byte) 6);

        Map<Byte, String> vehicleTypeReverseMap = new HashMap<>();
        vehicleTypeReverseMap.put((byte) 1, "Bike");
        vehicleTypeReverseMap.put((byte) 2, "Car");
        vehicleTypeReverseMap.put((byte) 3, "Bicycle");

        Map<Byte, String> vehicleCategoryReverseMap = new HashMap<>();
        vehicleCategoryReverseMap.put((byte) 1, "Petrol");
        vehicleCategoryReverseMap.put((byte) 2, "Diesel");
        vehicleCategoryReverseMap.put((byte) 3, "CNG");
        vehicleCategoryReverseMap.put((byte) 4, "Electric");
        vehicleCategoryReverseMap.put((byte) 5, "Hybrid");
        vehicleCategoryReverseMap.put((byte) 6, "None");

        vehicleBrands = new ArrayList<>();

        // Setup Autocomplete Fields
        String[] vehicleTypes = {"Bike", "Car", "Bicycle"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, vehicleTypes);
        binding.vehicleTypeAutoComplete.setAdapter(typeAdapter);
        binding.vehicleTypeAutoComplete.post(() -> binding.vehicleTypeAutoComplete.setDropDownWidth(binding.vehicleTypeAutoComplete.getWidth()));

        String[] categories = {"Petrol", "Diesel", "CNG", "Electric", "Hybrid", "None"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categories);
        binding.vehicleCategoryAutoComplete.setAdapter(categoryAdapter);
        binding.vehicleCategoryAutoComplete.post(() -> binding.vehicleCategoryAutoComplete.setDropDownWidth(binding.vehicleCategoryAutoComplete.getWidth()));

        brandAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        binding.vehicleBrandAutoComplete.setAdapter(brandAdapter);
        binding.vehicleBrandAutoComplete.post(() -> binding.vehicleBrandAutoComplete.setDropDownWidth(binding.vehicleBrandAutoComplete.getWidth()));

        binding.vehicleTypeAutoComplete.setOnItemClickListener((parent, v, position, id) -> {
            String selectedType = vehicleTypes[position];
            boolean isCar = selectedType.equalsIgnoreCase("Car");
            binding.carSpecificFields.setVisibility(isCar ? View.VISIBLE : View.GONE);
            byte vehicleType = vehicleTypeMap.get(selectedType);
            fetchBrandsByVehicleType(vehicleType); // No selected brand yet
        });

        binding.vehicleTypeAutoComplete.setOnClickListener(v -> binding.vehicleTypeAutoComplete.showDropDown());
        binding.vehicleBrandAutoComplete.setOnClickListener(v -> binding.vehicleBrandAutoComplete.showDropDown());
        binding.vehicleCategoryAutoComplete.setOnClickListener(v -> binding.vehicleCategoryAutoComplete.showDropDown());

        binding.btnBrowseFile.setOnClickListener(v -> certificatePickerLauncher.launch("*/*"));

        binding.officialInfoContainer.setVisibility(View.VISIBLE);

        if (vehicleId != -1) {
            fetchVehicleDetails(vehicleId, vehicleTypeReverseMap, vehicleCategoryReverseMap);
        } else {
            currentVehicle = new Vehicle();
        }

        btnSubmit.setOnClickListener(v -> submitForm());
    }

    // Overloaded method for when no brand is pre-selected
    private void fetchBrandsByVehicleType(byte vehicleType) {
        fetchBrandsByVehicleType(vehicleType, null);
    }

    private void fetchBrandsByVehicleType(byte vehicleType, Integer selectedBrandId) {
        loadingScreen.show("Fetching brands...");
        ApiService apiService = ApiClient.getApiService();
        Call<BaseResponse> call = apiService.getBrandsByVehicleType(vehicleType);

        call.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                loadingScreen.dismiss();
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 200) {
                    BaseResponse baseResponse = response.body();
                    Map<String, Object> data = baseResponse.getData();
                    if (data != null && data.containsKey("brands")) {
                        Gson gson = new Gson();
                        Type brandListType = new TypeToken<List<VehicleBrand>>() {}.getType();
                        vehicleBrands = gson.fromJson(gson.toJson(data.get("brands")), brandListType);
                        List<String> brandNames = new ArrayList<>();
                        String selectedBrandName = null;
                        for (VehicleBrand brand : vehicleBrands) {
                            brandNames.add(brand.getBrandName());
                            if (selectedBrandId != null && brand.getBrandId() == selectedBrandId) {
                                selectedBrandName = brand.getBrandName();
                            }
                        }
                        brandAdapter.clear();
                        brandAdapter.addAll(brandNames);
                        brandAdapter.notifyDataSetChanged();

                        // Set the selected brand if it exists
                        if (selectedBrandName != null) {
                            binding.vehicleBrandAutoComplete.setText(selectedBrandName, false);
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch brands", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                loadingScreen.dismiss();
                Toast.makeText(requireContext(), "Network error fetching brands: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchVehicleDetails(int vehicleId, Map<Byte, String> vehicleTypeReverseMap,
                                     Map<Byte, String> vehicleCategoryReverseMap) {
        loadingScreen.show("Fetching vehicle details...");
        ApiService apiService = ApiClient.getApiService();
        Call<BaseResponse> call = apiService.getVehicleById(vehicleId);

        call.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                loadingScreen.dismiss();
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 200) {
                    BaseResponse baseResponse = response.body();
                    Map<String, Object> data = baseResponse.getData();
                    if (data != null) {
                        try {

                            Gson gson = new Gson();
                            currentVehicle = gson.fromJson(gson.toJson(data), Vehicle.class);
                            Vehicle.VehicleBasicInfo basicInfo = currentVehicle.getVehicleBasicInfo();
                            fetchBrandsByVehicleType(basicInfo.getVehicleType(), basicInfo.getVehicleBrand());
                            if (basicInfo != null) {
                                binding.vehicleNameTextField.setText(basicInfo.getVehicleTitle());
                                binding.vehicleTypeAutoComplete.setText(vehicleTypeReverseMap.get(basicInfo.getVehicleType()), false);
                                binding.vehicleModelTextField.setText(basicInfo.getVehicleModel());
                                binding.vehicleCategoryAutoComplete.setText(vehicleCategoryReverseMap.get(basicInfo.getVehicleCategory()), false);
                                binding.vehicleColorTextField.setText(basicInfo.getVehicleColor());
                                binding.seatingCapacityTextField.setText(String.valueOf(basicInfo.getVehicleSeatingCapacity()));
                                binding.luggageCapacityTextField.setText(String.valueOf(basicInfo.getVehicleLuggageCapacity()));
                                binding.vehicleNumberTextField.setText(basicInfo.getVehicleNumber());

                                // Load existing registration certificate if it exists
                                if (basicInfo.getrcPath() != null && !basicInfo.getrcPath().isEmpty()) {
                                    serverRegistrationCertificatePath = basicInfo.getrcPath();
                                    String fileName = serverRegistrationCertificatePath.contains("/")
                                            ? serverRegistrationCertificatePath.substring(serverRegistrationCertificatePath.lastIndexOf("/") + 1)
                                            : serverRegistrationCertificatePath;
                                    registrationCertificateUri = Uri.parse(getString(R.string.api_url) + "uploads/vehicles_rc" + serverRegistrationCertificatePath);
                                    registrationCertificateFileName = fileName;
                                    binding.uploadedFileName.setText(fileName);
                                } else {
                                    registrationCertificateUri = null;
                                    serverRegistrationCertificatePath = "";
                                    registrationCertificateFileName = "";
                                    binding.uploadedFileName.setText("");
                                }

                                boolean isCar = basicInfo.getVehicleType() == 2;
                                binding.carSpecificFields.setVisibility(isCar ? View.VISIBLE : View.GONE);

                                // Fetch brands and set the selected brand

                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing vehicle data", e);
                            Toast.makeText(requireContext(), "Error parsing vehicle data", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch vehicle details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                loadingScreen.dismiss();
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getFileExtension(Uri uri) {
        String mimeType = requireContext().getContentResolver().getType(uri);
        return mimeType != null ? mimeType.substring(mimeType.lastIndexOf("/") + 1) : "unknown";
    }

    private File getFileFromUri(Uri uri, String fileName) {
        File file = new File(requireContext().getCacheDir(), fileName);
        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (java.io.IOException e) {
            Log.e(TAG, "Error processing file", e);
            Toast.makeText(requireContext(), "Error processing file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return file;
    }

    private void submitForm() {
        String vehicleTitle = binding.vehicleNameTextField.getText() != null ? binding.vehicleNameTextField.getText().toString().trim() : "";
        String vehicleTypeStr = binding.vehicleTypeAutoComplete.getText().toString().trim();
        String vehicleBrandStr = binding.vehicleBrandAutoComplete.getText() != null ? binding.vehicleBrandAutoComplete.getText().toString().trim() : "";
        String vehicleModel = binding.vehicleModelTextField.getText() != null ? binding.vehicleModelTextField.getText().toString().trim() : "";
        String vehicleCategoryStr = binding.vehicleCategoryAutoComplete.getText().toString().trim();
        String vehicleColor = binding.vehicleColorTextField.getText() != null ? binding.vehicleColorTextField.getText().toString().trim() : "";
        String seatingCapacityStr = binding.seatingCapacityTextField.getText() != null ? binding.seatingCapacityTextField.getText().toString().trim() : "";
        String luggageCapacityStr = binding.luggageCapacityTextField.getText() != null ? binding.luggageCapacityTextField.getText().toString().trim() : "";
        String vehicleNumber = binding.vehicleNumberTextField.getText() != null ? binding.vehicleNumberTextField.getText().toString().trim() : "";

        // Validation
        if (vehicleTitle.isEmpty() || vehicleTypeStr.isEmpty() || vehicleBrandStr.isEmpty() ||
                vehicleModel.isEmpty() || vehicleCategoryStr.isEmpty() || vehicleColor.isEmpty() ||
                vehicleNumber.isEmpty()) {
            Toast.makeText(requireContext(), "All fields are required except seating and luggage capacity", Toast.LENGTH_SHORT).show();
            return;
        }

        Byte vehicleType = vehicleTypeMap.get(vehicleTypeStr);
        if (vehicleType == null) {
            Toast.makeText(requireContext(), "Invalid vehicle type selected", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer vehicleBrandId = null;
        for (VehicleBrand brand : vehicleBrands) {
            if (brand.getBrandName().equals(vehicleBrandStr)) {
                vehicleBrandId = brand.getBrandId();
                break;
            }
        }
        if (vehicleBrandId == null) {
            Toast.makeText(requireContext(), "Invalid vehicle brand selected", Toast.LENGTH_SHORT).show();
            return;
        }

        Byte vehicleCategory = vehicleCategoryMap.get(vehicleCategoryStr);
        if (vehicleCategory == null) {
            Toast.makeText(requireContext(), "Invalid vehicle category selected", Toast.LENGTH_SHORT).show();
            return;
        }

        int seatingCapacity = 0;
        int luggageCapacity = 0;
        try {
            if (!seatingCapacityStr.isEmpty()) seatingCapacity = Integer.parseInt(seatingCapacityStr);
            if (!luggageCapacityStr.isEmpty()) luggageCapacity = Integer.parseInt(luggageCapacityStr);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid number format for capacity", e);
        }

        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int customerId = prefs.getInt("account_id", -1);
        if (customerId == -1) {
            Toast.makeText(requireContext(), "Customer ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Initialize VehicleBasicInfo
        if (currentVehicle == null) currentVehicle = new Vehicle();
        Vehicle.VehicleBasicInfo basicInfo = new Vehicle.VehicleBasicInfo(
                vehicleId != -1 ? vehicleId : 0,
                customerId,
                vehicleType,
                vehicleTitle,
                vehicleBrandId,
                vehicleModel,
                vehicleCategory,
                vehicleColor,
                seatingCapacity,
                luggageCapacity,
                vehicleNumber,
                serverRegistrationCertificatePath // Use server path as final value
        );

        // Prepare multipart form data
        RequestBody vehicleIdPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(vehicleId));
        RequestBody customerIdPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(customerId));
        RequestBody vehicleTypePart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(vehicleType));
        RequestBody vehicleTitlePart = RequestBody.create(MediaType.parse("text/plain"), vehicleTitle);
        RequestBody vehicleBrandIdPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(vehicleBrandId));
        RequestBody vehicleModelPart = RequestBody.create(MediaType.parse("text/plain"), vehicleModel);
        RequestBody vehicleCategoryPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(vehicleCategory));
        RequestBody vehicleColorPart = RequestBody.create(MediaType.parse("text/plain"), vehicleColor);
        RequestBody seatingCapacityPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(seatingCapacity));
        RequestBody luggageCapacityPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(luggageCapacity));
        RequestBody vehicleNumberPart = RequestBody.create(MediaType.parse("text/plain"), vehicleNumber);

        MultipartBody.Part certificatePart = null;
        if (registrationCertificateUri != null) {
            // Determine if it's a local file or server file
            boolean isLocal = !registrationCertificateUri.toString().startsWith(getString(R.string.api_url));
            if (isLocal) {
                // New local file selected
                File certificateFile = getFileFromUri(registrationCertificateUri, registrationCertificateFileName);
                RequestBody certificateRequestBody = RequestBody.create(MediaType.parse("*/*"), certificateFile);
                certificatePart = MultipartBody.Part.createFormData("registrationCertificate", registrationCertificateFileName, certificateRequestBody);
            } else {
                // Existing server file
                RequestBody certificateRequestBody = RequestBody.create(MediaType.parse("text/plain"), serverRegistrationCertificatePath);
                certificatePart = MultipartBody.Part.createFormData("registrationCertificate", registrationCertificateFileName, certificateRequestBody);
            }
        }

        loadingScreen.show(vehicleId != -1 ? "Updating..." : "Adding...");

        ApiService apiService = ApiClient.getApiService();
        Call<BaseResponse> call = apiService.addVehicle(
                vehicleIdPart, customerIdPart, vehicleTypePart, vehicleTitlePart, vehicleBrandIdPart, vehicleModelPart,
                vehicleCategoryPart, vehicleColorPart, seatingCapacityPart, luggageCapacityPart,
                vehicleNumberPart, certificatePart
        );

        call.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                loadingScreen.dismiss();
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 200) {
                    BaseResponse baseResponse = response.body();
                    Map<String, Object> data = baseResponse.getData();
                    int newVehicleId = -1;
                    if (data != null) {
                        if (data.containsKey("vehicleId")) {
                            newVehicleId = ((Number) data.get("vehicleId")).intValue();
                        } else if (data.containsKey("vehicle_id")) {
                            newVehicleId = ((Number) data.get("vehicle_id")).intValue();
                        } else if (data.containsKey("id")) {
                            newVehicleId = ((Number) data.get("id")).intValue();
                        }
                    }

                    if (newVehicleId != -1) {
                        SharedPreferences vehiclePrefs = requireActivity().getSharedPreferences("customer_update_vehicle", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = vehiclePrefs.edit();
                        editor.putInt("vehicle_id", newVehicleId);
                        editor.apply();

                        basicInfo.setVehicleId(newVehicleId);

                        if (data != null && data.containsKey("registration_certificate_path")) {
                            basicInfo.setrcPath((String) data.get("registration_certificate_path"));
                            serverRegistrationCertificatePath = basicInfo.getrcPath();
                            registrationCertificateFileName = serverRegistrationCertificatePath.contains("/")
                                    ? serverRegistrationCertificatePath.substring(serverRegistrationCertificatePath.lastIndexOf("/") + 1)
                                    : serverRegistrationCertificatePath;
                            registrationCertificateUri = Uri.parse(getString(R.string.api_url) + "/uploads/vehicles_rc/" + serverRegistrationCertificatePath);
                        }

                        currentVehicle.setVehicleBasicInfo(basicInfo);

                        Toast.makeText(requireContext(), "Vehicle " + (vehicleId != -1 ? "updated" : "added") + " successfully", Toast.LENGTH_SHORT).show();

                        Fragment newFragment = CustomerAddVehiclesImages.newInstance(currentVehicle, prVehicleId);
                        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.customer_vehicles_fragment_container, newFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    } else {
                        Toast.makeText(requireContext(), "Vehicle processed but ID not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMsg = response.body() != null ? response.body().getMessage() : "Unknown error";
                    Toast.makeText(requireContext(), "Failed to process vehicle: " + errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                loadingScreen.dismiss();
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
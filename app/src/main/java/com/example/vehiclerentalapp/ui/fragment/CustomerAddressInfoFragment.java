package com.example.vehiclerentalapp.ui.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.vehiclerentalapp.R;
import com.example.vehiclerentalapp.models.BaseResponse;
import com.example.vehiclerentalapp.models.CustomerRegistration;
import com.example.vehiclerentalapp.networks.ApiClient;
import com.example.vehiclerentalapp.networks.ApiService;
import com.example.vehiclerentalapp.utils.LoadingScreen;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerAddressInfoFragment extends Fragment {

    private FusedLocationProviderClient fusedLocationClient;
    private double latitude, longitude;
    private TextInputEditText houseNumberTextField, streetNameTextField, localityTextField, cityTextField,
            districtTextField, stateTextField, countryTextField, postalCodeTextField;
    private LoadingScreen loadingScreen;

    public CustomerAddressInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_address_info, container, false);

        // Initialize LoadingScreen
        loadingScreen = new LoadingScreen(requireContext());

        // Initialize UI elements
        houseNumberTextField = view.findViewById(R.id.house_number_textfield);
        streetNameTextField = view.findViewById(R.id.street_name_textfield);
        localityTextField = view.findViewById(R.id.locality_textfield);
        cityTextField = view.findViewById(R.id.city_textfield);
        districtTextField = view.findViewById(R.id.district_textfield);
        stateTextField = view.findViewById(R.id.state_textfield);
        countryTextField = view.findViewById(R.id.country_textfield);
        postalCodeTextField = view.findViewById(R.id.postal_code_textfield);
        MaterialButton btnSubmit = view.findViewById(R.id.CustomerAddressBtnSubmit);

        // Initially hide the input fields
        view.findViewById(R.id.addressFields).setVisibility(View.GONE);

        // Fetch customer address details from DB when the fragment loads
        fetchCustomerAddressDetails(view);

        // Set click listener for the submit button
        btnSubmit.setOnClickListener(v -> submitForm());

        return view;
    }

    private void fetchCustomerAddressDetails(View view) {
        SharedPreferences registrationPrefs = requireActivity().getSharedPreferences("RegistrationPrefs", Context.MODE_PRIVATE);
        int accountId = registrationPrefs.getInt("account_id", -1);

        if (accountId == -1) {
            Toast.makeText(requireContext(), "Account ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingScreen.show("Fetching Address Details...");

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
                            Map<String, Object> addressInfo = baseResponse.getNestedMap("addressInfo");
                            if (addressInfo != null && !addressInfo.isEmpty()) {
                                populateFields(addressInfo);
                                loadingScreen.dismiss();
                                view.findViewById(R.id.addressFields).setVisibility(View.VISIBLE);
                                Toast.makeText(requireContext(), "Address details fetched successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                // No address data from DB, fetch current location
                                getLocation(view);
                            }
                        } else {
                            getLocation(view);
                        }
                    } else {
                        getLocation(view);
                    }
                    loadingScreen.dismiss();
                } else {
                    try {
                        String errorBody = response.errorBody().string();

                        Toast.makeText(requireContext(), "Server error: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                    getLocation(view);
                    loadingScreen.dismiss();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                loadingScreen.dismiss();
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                getLocation(view);
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void getLocation(View view) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        loadingScreen.show("Fetching Current Location...");
        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    getAddressFromLocation(latitude, longitude, view);
                    loadingScreen.dismiss();
                } else {
                    Toast.makeText(requireContext(), "Unable to get location!", Toast.LENGTH_SHORT).show();
                    view.findViewById(R.id.addressFields).setVisibility(View.VISIBLE); // Show fields for manual entry
                }
            }
        });
    }

    private void getAddressFromLocation(double latitude, double longitude, View view) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);
                String fullAddress = address.getAddressLine(0);
                String[] addressParts = fullAddress.split(",");

                String locality = addressParts.length > 2 ? addressParts[2].trim() : "";

                view.findViewById(R.id.addressFields).setVisibility(View.VISIBLE);
                houseNumberTextField.setText(address.getSubThoroughfare());
                streetNameTextField.setText(address.getThoroughfare());
                localityTextField.setText(locality);
                cityTextField.setText(address.getSubLocality());
                districtTextField.setText(address.getLocality());
                stateTextField.setText(address.getAdminArea());
                countryTextField.setText(address.getCountryName());
                postalCodeTextField.setText(address.getPostalCode());
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Failed to get address!", Toast.LENGTH_SHORT).show();
            view.findViewById(R.id.addressFields).setVisibility(View.VISIBLE); // Show fields for manual entry
        }
    }

    private void populateFields(Map<String, Object> addressInfo) {
        Object doorNo = addressInfo.get("doorNo");
        Object streetName = addressInfo.get("streetName");
        Object locality = addressInfo.get("locality");
        Object city = addressInfo.get("city");
        Object district = addressInfo.get("district");
        Object state = addressInfo.get("state");
        Object country = addressInfo.get("country");
        Object postalCode = addressInfo.get("postalCode");

        if (doorNo instanceof String) houseNumberTextField.setText((String) doorNo);
        if (streetName instanceof String) streetNameTextField.setText((String) streetName);
        if (locality instanceof String) localityTextField.setText((String) locality);
        if (city instanceof String) cityTextField.setText((String) city);
        if (district instanceof String) districtTextField.setText((String) district);
        if (state instanceof String) stateTextField.setText((String) state);
        if (country instanceof String) countryTextField.setText((String) country);
        if (postalCode instanceof String) postalCodeTextField.setText((String) postalCode);
    }

    private void submitForm() {
        // Fetch values from TextInputEditText fields
        String houseNumber = houseNumberTextField.getText() != null ? houseNumberTextField.getText().toString().trim() : "";
        String streetName = streetNameTextField.getText() != null ? streetNameTextField.getText().toString().trim() : "";
        String locality = localityTextField.getText() != null ? localityTextField.getText().toString().trim() : "";
        String city = cityTextField.getText() != null ? cityTextField.getText().toString().trim() : "";
        String district = districtTextField.getText() != null ? districtTextField.getText().toString().trim() : "";
        String state = stateTextField.getText() != null ? stateTextField.getText().toString().trim() : "";
        String country = countryTextField.getText() != null ? countryTextField.getText().toString().trim() : "";
        String postalCode = postalCodeTextField.getText() != null ? postalCodeTextField.getText().toString().trim() : "";

        // Client-side validation
        if (houseNumber.isEmpty() || streetName.isEmpty() || locality.isEmpty() || city.isEmpty() ||
                district.isEmpty() || state.isEmpty() || country.isEmpty() || postalCode.isEmpty()) {
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

        // Create CustomerRegistration object with AddressInfo
        CustomerRegistration customer = new CustomerRegistration();
        customer.setAddressInfo(new CustomerRegistration.AddressInfo(houseNumber, streetName, locality, city, district, state, country, postalCode, String.valueOf(latitude), String.valueOf(longitude)));

        // Show loading screen
        loadingScreen.show("Submitting Address Details...");

        // Make API call to update customer address info
        ApiService apiService = ApiClient.getApiService();
        Call<BaseResponse> call = apiService.updateCustomerAddressInfo(accountId, customer);

        call.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {

                if (response.isSuccessful()) {
                    BaseResponse baseResponse = response.body();
                    if (baseResponse != null && baseResponse.getCode() == 200 && "success".equals(baseResponse.getStatus())) {
                        Toast.makeText(requireContext(), baseResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        loadingScreen.dismiss();
                        // Navigate to the next fragment on success
                        Fragment newFragment = new CustomerBankInfoFragment();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation(getView());
        } else {
            Toast.makeText(requireContext(), "Permission denied! Enter address manually.", Toast.LENGTH_SHORT).show();
            getView().findViewById(R.id.addressFields).setVisibility(View.VISIBLE); // Show fields for manual entry
        }
    }
}
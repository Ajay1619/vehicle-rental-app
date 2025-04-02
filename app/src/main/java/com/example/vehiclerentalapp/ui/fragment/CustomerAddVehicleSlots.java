package com.example.vehiclerentalapp.ui.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.example.vehiclerentalapp.R;
import com.example.vehiclerentalapp.databinding.FragmentCustomerAddVehicleSlotsBinding;
import com.example.vehiclerentalapp.models.BaseResponse;
import com.example.vehiclerentalapp.models.Vehicle;
import com.example.vehiclerentalapp.networks.ApiClient;
import com.example.vehiclerentalapp.networks.ApiService;
import com.example.vehiclerentalapp.utils.LoadingScreen;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerAddVehicleSlots extends Fragment {

    private static final String ARG_VEHICLE = "vehicle";
    private static final String ARG_VEHICLE_ID = "vehicle_id";

    private Vehicle vehicle;
    private int vehicleId = -1;
    private FragmentCustomerAddVehicleSlotsBinding binding;
    private LoadingScreen loadingScreen;

    public CustomerAddVehicleSlots() {}

    public static CustomerAddVehicleSlots newInstance(Vehicle vehicle, int vehicleId) {
        CustomerAddVehicleSlots fragment = new CustomerAddVehicleSlots();
        Bundle args = new Bundle();
        args.putSerializable(ARG_VEHICLE, vehicle);
        args.putInt(ARG_VEHICLE_ID, vehicleId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            vehicle = (Vehicle) getArguments().getSerializable(ARG_VEHICLE);
            vehicleId = getArguments().getInt(ARG_VEHICLE_ID, -1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCustomerAddVehicleSlotsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadingScreen = new LoadingScreen(requireContext());

        // Setup Duration Type Autocomplete for the initial slot
        setupDurationType(binding.durationTypeAutoComplete);

        // Add Slot Button Click Listener
        binding.addSlotButton.setOnClickListener(v -> addNewSlotFields(null));

        // Submit Button Click Listener
        MaterialButton btnSubmit = view.findViewById(R.id.CustomerAddVehicleBasicInfoSbtBtn);
        btnSubmit.setOnClickListener(v -> submitSlots());

        // Pre-populate slots from vehicle object if present
        if (vehicle != null && vehicle.getVehicleSlots() != null && !vehicle.getVehicleSlots().isEmpty()) {
            List<Vehicle.VehicleSlot> slots = vehicle.getVehicleSlots();
            // Populate the initial slot with the first slot data
            if (!slots.isEmpty()) {
                Vehicle.VehicleSlot firstSlot = slots.get(0);
                binding.slotDurationTextField.setText(firstSlot.getVehicleSlotDuration());
                binding.slotPriceTextField.setText(String.valueOf(firstSlot.getVehicleSlotPrice()));
                binding.durationTypeAutoComplete.setText(firstSlot.getVehicleSlotDurationType() == 1 ? "Minutes" : "Hours", false);
            }
            // Add additional slots if more than one exists
            for (int i = 1; i < slots.size(); i++) {
                addNewSlotFields(slots.get(i));
            }
        }
    }

    private void setupDurationType(AutoCompleteTextView autoCompleteTextView) {
        String[] durationTypes = {"Minutes", "Hours"};
        ArrayAdapter<String> durationAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                durationTypes
        );
        autoCompleteTextView.setAdapter(durationAdapter);
        autoCompleteTextView.post(() ->
                autoCompleteTextView.setDropDownWidth(autoCompleteTextView.getWidth())
        );
    }

    private void addNewSlotFields(Vehicle.VehicleSlot slot) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View slotView = inflater.inflate(R.layout.slot_fields_layout, binding.slotFieldsContainer, false);

        AutoCompleteTextView newDurationType = slotView.findViewById(R.id.durationTypeAutoComplete);
        TextInputEditText durationField = slotView.findViewById(R.id.slotDurationTextField);
        TextInputEditText priceField = slotView.findViewById(R.id.slotPriceTextField);

        // Setup Duration Type for the new AutoCompleteTextView
        setupDurationType(newDurationType);

        // Pre-fill fields if slot data exists
        if (slot != null) {
            durationField.setText(slot.getVehicleSlotDuration());
            priceField.setText(String.valueOf(slot.getVehicleSlotPrice()));
            newDurationType.setText(slot.getVehicleSlotDurationType() == 1 ? "Minutes" : "Hours", false);
        }

        // Setup Remove Button
        slotView.findViewById(R.id.removeSlotButton).setOnClickListener(v ->
                binding.slotFieldsContainer.removeView(slotView)
        );

        binding.slotFieldsContainer.addView(slotView);
    }

    private void submitSlots() {
        if (vehicleId == -1) {
            SharedPreferences prefs = requireActivity().getSharedPreferences("customer_update_vehicle", Context.MODE_PRIVATE);
            vehicleId = prefs.getInt("vehicle_id", -1);
            if (vehicleId == -1) {
                Toast.makeText(requireContext(), "Vehicle ID not found", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        List<Vehicle.VehicleSlot> slotsData = new ArrayList<>();
        int slotCount = binding.slotFieldsContainer.getChildCount();

        for (int i = 0; i < slotCount; i++) {
            View slotView = binding.slotFieldsContainer.getChildAt(i);
            AutoCompleteTextView durationTypeView = slotView.findViewById(R.id.durationTypeAutoComplete);
            String durationStr = ((TextInputEditText) slotView.findViewById(R.id.slotDurationTextField)).getText().toString().trim();
            String durationType = durationTypeView.getText().toString().trim();
            String priceStr = ((TextInputEditText) slotView.findViewById(R.id.slotPriceTextField)).getText().toString().trim();

            if (durationStr.isEmpty() || durationType.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all slot fields", Toast.LENGTH_SHORT).show();
                return;
            }

            float duration;
            double price;
            byte durationTypeByte;
            try {
                duration = Float.parseFloat(durationStr);
                price = Double.parseDouble(priceStr); // Fixed: Changed parseFloat to parseDouble
                durationTypeByte = "Minutes".equalsIgnoreCase(durationType) ? (byte) 1 : (byte) 2;
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Invalid number format in slot " + (i + 1), Toast.LENGTH_SHORT).show();
                return;
            }

            Vehicle.VehicleSlot slot = new Vehicle.VehicleSlot(
                    0, // vehicleSlotsId will be set by server or use existing ID if updating
                    vehicleId,
                    String.valueOf(duration),
                    durationTypeByte,
                    price
            );
            slotsData.add(slot);
        }

        if (slotsData.isEmpty()) {
            Toast.makeText(requireContext(), "Please add at least one slot", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingScreen.show("Submitting Slots...");
        ApiService apiService = ApiClient.getApiService();
        Call<BaseResponse> call = apiService.addVehicleSlots(vehicleId, slotsData);

        call.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                loadingScreen.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse baseResponse = response.body();
                    if (baseResponse.getCode() == 200 && "success".equals(baseResponse.getStatus())) {
                        Toast.makeText(requireContext(), "Slots added successfully", Toast.LENGTH_SHORT).show();
                        Fragment newFragment = new CustomerVehiclesViewFragment(); // Replace with your next fragment
                        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.customer_vehicles_fragment_container, newFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    } else {
                        Toast.makeText(requireContext(), "Failed to add slots: " + baseResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        Toast.makeText(requireContext(), "Server error: " + response.errorBody().string(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
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
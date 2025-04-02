package com.example.vehiclerentalapp.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.vehiclerentalapp.R;
import com.example.vehiclerentalapp.models.Vehicle;
import com.example.vehiclerentalapp.networks.ApiClient;
import com.example.vehiclerentalapp.networks.ApiService;
import com.example.vehiclerentalapp.models.BaseResponse;
import com.example.vehiclerentalapp.utils.VehicleIndividualViewSliderImageAdapter;
import com.example.vehiclerentalapp.utils.VehicleIndividualViewSlotAdapter;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerVehiclesIndividualView extends Fragment {

    private static final String ARG_VEHICLE_ID = "vehicle_id";
    private int vehicleId;
    private ViewPager2 imageSlider;
    private LinearLayout dotsContainer;
    private VehicleIndividualViewSliderImageAdapter imageAdapter;
    private List<String> imageUrls = new ArrayList<>();
    private RecyclerView slotsRecyclerView;
    private VehicleIndividualViewSlotAdapter slotAdapter;
    private Handler sliderHandler = new Handler(Looper.getMainLooper()); // Handler for auto-sliding
    private Runnable sliderRunnable; // Runnable to switch pages
    private static final int SLIDE_INTERVAL = 5000; // 3 seconds interval

    public CustomerVehiclesIndividualView() {
        // Required empty public constructor
    }

    public static CustomerVehiclesIndividualView newInstance(int vehicleId) {
        CustomerVehiclesIndividualView fragment = new CustomerVehiclesIndividualView();
        Bundle args = new Bundle();
        args.putInt(ARG_VEHICLE_ID, vehicleId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            vehicleId = getArguments().getInt(ARG_VEHICLE_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_vehicle_individual_view, container, false);

        // Initialize UI components
        imageSlider = view.findViewById(R.id.imageSlider);
        dotsContainer = view.findViewById(R.id.dotsContainer);
        slotsRecyclerView = view.findViewById(R.id.slotsRecyclerView);

        // Fetch vehicle details from the server
        fetchVehicleDetails(vehicleId, view);

        return view;
    }

    private void fetchVehicleDetails(int vehicleId, View view) {
        ApiService apiService = ApiClient.getApiService();
        Call<BaseResponse> call = apiService.getVehicleById(vehicleId);

        call.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 200) {
                    BaseResponse baseResponse = response.body();
                    try {
                        Vehicle vehicle = new Gson().fromJson(new Gson().toJson(baseResponse.getData()), Vehicle.class);
                        populateVehicleData(vehicle, view);
                    } catch (Exception e) {
                        Log.e("CustomerVehiclesIndividualView", "Error parsing vehicle data", e);
                        Toast.makeText(requireContext(), "Error parsing vehicle data", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch vehicle details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateVehicleData(Vehicle vehicle, View view) {
        Vehicle.VehicleBasicInfo basicInfo = vehicle.getVehicleBasicInfo();

        // Populate image slider
        if (vehicle.getVehicleImages() != null && !vehicle.getVehicleImages().isEmpty()) {
            imageUrls.clear();
            String baseUrl = getString(R.string.api_url) + "uploads/vehicles_images/";
            for (Vehicle.VehicleImage image : vehicle.getVehicleImages()) {
                if (image.getVehicleImagePath() != null && !image.getVehicleImagePath().isEmpty()) {
                    imageUrls.add(baseUrl + image.getVehicleImagePath());
                }
            }
        }
        if (imageUrls.isEmpty()) {
            imageUrls.add(getString(R.string.api_url) + "uploads/vehicles_images/default_image.jpg");
        }
        imageAdapter = new VehicleIndividualViewSliderImageAdapter(requireContext(), imageUrls);
        imageSlider.setAdapter(imageAdapter);
        setupDots(imageUrls.size(), dotsContainer, 0);

        // Setup auto-sliding
        setupAutoSliding();

        imageSlider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateDots(position, dotsContainer);
                // Reset the auto-slide timer when user manually changes page
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, SLIDE_INTERVAL);
            }
        });

        // Populate basic info
        ((TextView) view.findViewById(R.id.titleText)).setText(basicInfo.getVehicleTitle());
        ((TextView) view.findViewById(R.id.subtitleText)).setText(basicInfo.getVehicleModel());
        ((TextView) view.findViewById(R.id.statusText)).setText(basicInfo.getVehicleColor());


        // Populate cards
        String fuelType = basicInfo.getVehicleCategory() != null ? basicInfo.getvehicleCategoryTitle() : "N/A";
        ((TextView) view.findViewById(R.id.accountInfoValue)).setText(fuelType);
        ((TextView) view.findViewById(R.id.welcomeMsgValue)).setText(String.valueOf(basicInfo.getVehicleSeatingCapacity()));
        ((TextView) view.findViewById(R.id.infoValue)).setText(basicInfo.getVehicleLuggageCapacity() + " ltrs");

        // Populate slots RecyclerView
        if (vehicle.getVehicleSlots() != null && !vehicle.getVehicleSlots().isEmpty()) {
            slotAdapter = new VehicleIndividualViewSlotAdapter(vehicle.getVehicleSlots());
            slotsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            slotsRecyclerView.setAdapter(slotAdapter);
        } else {
            slotsRecyclerView.setVisibility(View.GONE);
        }
    }

    private void setupAutoSliding() {
        sliderRunnable = new Runnable() {
            @Override
            public void run() {
                int currentItem = imageSlider.getCurrentItem();
                int totalItems = imageAdapter.getItemCount();
                if (totalItems > 0) {
                    int nextItem = (currentItem + 1) % totalItems; // Loop back to start
                    imageSlider.setCurrentItem(nextItem, true);
                    sliderHandler.postDelayed(this, SLIDE_INTERVAL);
                }
            }
        };
        sliderHandler.postDelayed(sliderRunnable, SLIDE_INTERVAL);
    }


    private void setupDots(int count, LinearLayout dotsContainer, int currentIndex) {
        dotsContainer.removeAllViews();
        for (int i = 0; i < count; i++) {
            View dot = new View(requireContext());
            int selectedSize = 30;
            int normalSize = 15;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    (i == currentIndex) ? selectedSize : normalSize,
                    (i == currentIndex) ? selectedSize : normalSize
            );
            params.setMargins(6, 0, 6, 0);
            dot.setLayoutParams(params);
            dot.setBackground(ContextCompat.getDrawable(requireContext(),
                    (i == currentIndex) ? R.drawable.individual_vehicle_view_slider_indicator_selected
                            : R.drawable.individual_vehicle_view_slider_indicator_unselected));
            dotsContainer.addView(dot);
        }
    }

    private void updateDots(int currentIndex, LinearLayout dotsContainer) {
        for (int i = 0; i < dotsContainer.getChildCount(); i++) {
            View dot = dotsContainer.getChildAt(i);
            int selectedSize = 30;
            int normalSize = 15;
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) dot.getLayoutParams();
            params.width = (i == currentIndex) ? selectedSize : normalSize;
            params.height = (i == currentIndex) ? selectedSize : normalSize;
            params.setMargins(6, 0, 6, 0);
            dot.setLayoutParams(params);
            dot.setBackground(ContextCompat.getDrawable(requireContext(),
                    (i == currentIndex) ? R.drawable.individual_vehicle_view_slider_indicator_selected
                            : R.drawable.individual_vehicle_view_slider_indicator_unselected));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up the handler to prevent memory leaks
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Pause auto-sliding when the fragment is not visible
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Resume auto-sliding when the fragment becomes visible again
        sliderHandler.postDelayed(sliderRunnable, SLIDE_INTERVAL);
    }
}
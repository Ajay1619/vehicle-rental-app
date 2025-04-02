package com.example.vehiclerentalapp.ui.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vehiclerentalapp.R;
import com.example.vehiclerentalapp.models.CustomerVehicle;
import com.example.vehiclerentalapp.networks.ApiClient;
import com.example.vehiclerentalapp.networks.ApiService;
import com.example.vehiclerentalapp.utils.LoadingScreen;
import com.example.vehiclerentalapp.utils.VehicleAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerVehiclesViewFragment extends Fragment implements VehicleAdapter.OnVehicleInteractionListener {

    private RecyclerView recyclerView;
    private VehicleAdapter vehicleAdapter;
    private TabLayout tabLayout;
    private SharedPreferences userPrefs;
    private ApiService apiService;
    private LoadingScreen loadingScreen;
    private List<CustomerVehicle> allVehicles;
    private Map<Integer, Boolean> vehicleStatusMap;

    public CustomerVehiclesViewFragment() {
        // Required empty public constructor
    }

    public static CustomerVehiclesViewFragment newInstance() {
        return new CustomerVehiclesViewFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userPrefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        apiService = ApiClient.getApiService();
        allVehicles = new ArrayList<>();
        vehicleStatusMap = new HashMap<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_vehicles_view, container, false);

        recyclerView = view.findViewById(R.id.vehiclesRecyclerView);
        tabLayout = view.findViewById(R.id.tabLayout);
        LinearLayout customerAddNewVehicle = view.findViewById(R.id.customer_add_new_vehicle);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        vehicleAdapter = new VehicleAdapter(allVehicles, this);
        recyclerView.setAdapter(vehicleAdapter);

        customerAddNewVehicle.setOnClickListener(v -> loadFragment(new CustomerAddVehicleBasicInfo()));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateVehicleList(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                updateVehicleList(tab.getPosition());
            }
        });

        tabLayout.getTabAt(0).select(); // Triggers updateVehicleList(0)

        return view;
    }

    private void fetchVehicles(Integer vehicleType) {
        int customerId = userPrefs.getInt("user_id", -1);
        if (customerId == -1) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingScreen = new LoadingScreen(requireContext());
        loadingScreen.show("Fetching vehicles...");

        Call<ResponseBody> call = apiService.getCustomerVehiclesRaw(customerId, vehicleType);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                loadingScreen.dismiss();
                Log.d("CustomerVehicles", "onResponse triggered");
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonString = response.body().string();
                        Log.d("CustomerVehicles", "Raw JSON: " + jsonString);

                        Gson gson = new Gson();
                        JsonObject jsonObject = gson.fromJson(jsonString, JsonObject.class);
                        int code = jsonObject.get("code").getAsInt();
                        Log.d("CustomerVehicles", "Code: " + code);

                        if (code == 200) {
                            allVehicles.clear();
                            String dataJson = jsonObject.get("data").toString();
                            List<CustomerVehicle> vehicles = gson.fromJson(dataJson, new TypeToken<List<CustomerVehicle>>() {}.getType());

                            if (vehicles != null && !vehicles.isEmpty()) {
                                allVehicles.addAll(vehicles);
                                for (CustomerVehicle vehicle : allVehicles) {
                                    vehicleStatusMap.put(vehicle.getVehicleId(), vehicle.getVehicleStatus() == 1);
                                }
                                vehicleAdapter.updateList(allVehicles);
                                updateVehicleCounts();
                            } else {
                                Toast.makeText(getContext(), "No vehicles found", Toast.LENGTH_SHORT).show();
                                vehicleAdapter.updateList(new ArrayList<>());
                            }
                        } else {
                            Toast.makeText(getContext(), "Failed to fetch vehicles: " + code, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("CustomerVehicles", "Parsing error: " + e.toString());
                        Toast.makeText(getContext(), "Parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.e("CustomerVehicles", "Unsuccessful response: " + response.code());
                    Toast.makeText(getContext(), "Failed to fetch vehicles: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                loadingScreen.dismiss();
                Log.e("CustomerVehicles", "Network error: " + t.toString());
                Toast.makeText(getContext(), "Network error: " + t.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateVehicleList(int tabPosition) {
        Integer vehicleType;
        switch (tabPosition) {
            case 0: // Motorbikes
                vehicleType = 1;
                break;
            case 1: // Cars
                vehicleType = 2;
                break;
            case 2: // Bicycles
                vehicleType = 3;
                break;
            default:
                vehicleType = null;
                break;
        }
        fetchVehicles(vehicleType);
    }

    private void updateVehicleCounts() {
        int total = allVehicles.size();
        int active = 0;

        for (CustomerVehicle vehicle : allVehicles) {
            if (vehicle.getVehicleStatus() == 1) {
                active++;
            }
        }
        int inactive = total - active;

        TextView totalCount = getView().findViewById(R.id.totalVehiclesCount);
        TextView activeCount = getView().findViewById(R.id.activeVehiclesCount);
        TextView inactiveCount = getView().findViewById(R.id.inactiveVehiclesCount);

        if (totalCount != null) totalCount.setText(String.valueOf(total));
        if (activeCount != null) activeCount.setText(String.valueOf(active));
        if (inactiveCount != null) inactiveCount.setText(String.valueOf(inactive));
    }

    @Override
    public void onEditClick(CustomerVehicle vehicle) {
        Fragment editFragment = new CustomerAddVehicleBasicInfo();
        Bundle args = new Bundle();
        args.putInt("vehicleId", vehicle.getVehicleId());
        editFragment.setArguments(args);
        loadFragment(editFragment);
    }

    @Override
    public void onStatusChange(CustomerVehicle vehicle, boolean isActive) {
        updateVehicleStatus(vehicle, isActive);
    }

    @Override
    public void onDeleteClick(CustomerVehicle vehicle) {
        showDeleteConfirmationDialog(vehicle);
    }

    private void showDeleteConfirmationDialog(CustomerVehicle vehicle) {
        // Create a custom Dialog instance
        Dialog dialog = new Dialog(requireContext(), R.style.CustomDialogTheme);
        dialog.setContentView(R.layout.dialog_confirm_vehicle_delete);
        dialog.setCancelable(false);

        // Center the dialog and set transparent background
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.CENTER; // Center the dialog
            params.width = (int) (300 * requireContext().getResources().getDisplayMetrics().density + 0.5f); // 300dp in pixels
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Initialize dialog components
        TextView message = dialog.findViewById(R.id.delete_message);
        MaterialButton btnCancel = dialog.findViewById(R.id.btn_cancel);
        MaterialButton btnConfirm = dialog.findViewById(R.id.btn_confirm);

        // Customize message
        message.setText("Are you sure you want to delete " + vehicle.getVehicleTitle() + "?");

        // Optional: Add fade-in animation (like LoadingScreen)
        Animation fadeIn = AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_in);
        fadeIn.setDuration(300);
        dialog.getWindow().getDecorView().startAnimation(fadeIn);

        // Set button listeners
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            proceedWithDelete(vehicle);
        });

        // Show the dialog
        dialog.show();
    }

    private void proceedWithDelete(CustomerVehicle vehicle) {
        int customerId = userPrefs.getInt("user_id", -1);
        if (customerId == -1) return;

        LoadingScreen deleteLoading = new LoadingScreen(requireContext());
        deleteLoading.show("Deleting vehicle...");

        Call<Void> call = apiService.deleteVehicle(vehicle.getVehicleId());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                deleteLoading.dismiss();
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Vehicle deleted", Toast.LENGTH_SHORT).show();
                    allVehicles.remove(vehicle);
                    vehicleStatusMap.remove(vehicle.getVehicleId());
                    vehicleAdapter.updateList(allVehicles);
                    updateVehicleCounts();
                } else {
                    Toast.makeText(getContext(), "Failed to delete vehicle: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                deleteLoading.dismiss();
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.customer_vehicles_fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void updateVehicleStatus(CustomerVehicle vehicle, boolean isActive) {
        int customerId = userPrefs.getInt("user_id", -1);
        if (customerId == -1) return;

        LoadingScreen statusLoading = new LoadingScreen(requireContext());
        statusLoading.show("Updating status...");

        int vehicleId = vehicle.getVehicleId();
        Call<Void> call = apiService.updateVehicleStatus(vehicleId, isActive);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                statusLoading.dismiss();
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Status updated", Toast.LENGTH_SHORT).show();
                    vehicle.setVehicleStatus((byte) (isActive ? 1 : 2));
                    vehicleStatusMap.put(vehicleId, isActive);
                    vehicleAdapter.notifyDataSetChanged();
                    updateVehicleCounts();
                } else {
                    Toast.makeText(getContext(), "Failed to update status: " + response.code(), Toast.LENGTH_SHORT).show();
                    vehicleAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                statusLoading.dismiss();
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                vehicleAdapter.notifyDataSetChanged();
            }
        });
    }
}
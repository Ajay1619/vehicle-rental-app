package com.example.vehiclerentalapp.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.vehiclerentalapp.R;
import com.example.vehiclerentalapp.models.CustomerVehicle;
import com.example.vehiclerentalapp.ui.fragment.CustomerVehiclesIndividualView;

import java.util.ArrayList;
import java.util.List;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {
    private List<CustomerVehicle> vehicleList;
    private final OnVehicleInteractionListener interactionListener;

    public interface OnVehicleInteractionListener {
        void onEditClick(CustomerVehicle vehicle);
        void onStatusChange(CustomerVehicle vehicle, boolean isActive);
        void onDeleteClick(CustomerVehicle vehicle);
    }

    public VehicleAdapter(List<CustomerVehicle> vehicleList, OnVehicleInteractionListener listener) {
        this.vehicleList = new ArrayList<>(vehicleList);
        this.interactionListener = listener;
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.customer_vehicles_list, parent, false);
        return new VehicleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
        CustomerVehicle vehicle = vehicleList.get(position);

        holder.brandName.setText(vehicle.getVehicleBrandString() != null ? vehicle.getVehicleBrandString() : "Unknown Brand");
        holder.vehicleName.setText(vehicle.getVehicleTitle());
        holder.vehicleModel.setText(vehicle.getVehicleModel() != null ? vehicle.getVehicleModel() : "Unknown Model");

        // Remove listener before changing checked state to prevent unwanted triggers
        holder.activeToggle.setOnCheckedChangeListener(null);
        holder.activeToggle.setChecked(vehicle.getVehicleStatus() == 1);

        // Reattach the listener
        holder.activeToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (vehicle.getVehicleStatus() == (isChecked ? 1 : 0)) {
                return; // Prevent redundant API calls if status is unchanged
            }
            interactionListener.onStatusChange(vehicle, isChecked);
        });

        if (vehicle.getVehicleImagePath() != null && !vehicle.getVehicleImagePath().isEmpty()) {
            String imageUrl = holder.itemView.getContext().getString(R.string.api_url) + "uploads/vehicles_images/" + vehicle.getVehicleImagePath();
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .error(R.mipmap.vehicles)
                    .into(holder.vehicleImage);
        } else {
            holder.vehicleImage.setImageResource(R.drawable.error_image);
        }

        // Set click listeners for buttons and consume the click event
        holder.editButton.setOnClickListener(v -> {
            interactionListener.onEditClick(vehicle);
            v.performClick(); // Consume the click event
        });

        holder.deleteButton.setOnClickListener(v -> {
            interactionListener.onDeleteClick(vehicle);
            v.performClick(); // Consume the click event
        });

        holder.activeToggle.setOnClickListener(v -> v.performClick()); // Consume toggle click

        // Set click listener for the entire card
        holder.itemView.setOnClickListener(v -> {
            // Navigate to CustomerVehiclesIndividualView fragment with vehicle ID
            FragmentActivity activity = (FragmentActivity) holder.itemView.getContext();
            CustomerVehiclesIndividualView fragment = CustomerVehiclesIndividualView.newInstance(vehicle.getVehicleId());
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.customer_vehicles_fragment_container, fragment) // Replace with your container ID
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public int getItemCount() {
        return vehicleList.size();
    }

    public void updateList(List<CustomerVehicle> newList) {
        vehicleList.clear();
        vehicleList.addAll(newList);
        notifyDataSetChanged();
    }

    static class VehicleViewHolder extends RecyclerView.ViewHolder {
        ImageView vehicleImage, editButton, deleteButton;
        TextView brandName, vehicleName, vehicleModel;
        Switch activeToggle;

        public VehicleViewHolder(@NonNull View itemView) {
            super(itemView);
            vehicleImage = itemView.findViewById(R.id.vehicleImage);
            brandName = itemView.findViewById(R.id.brandName);
            vehicleName = itemView.findViewById(R.id.vehicleName);
            vehicleModel = itemView.findViewById(R.id.vehicleModel);
            activeToggle = itemView.findViewById(R.id.activeToggle);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
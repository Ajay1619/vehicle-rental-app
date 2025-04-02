package com.example.vehiclerentalapp.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vehiclerentalapp.R;
import com.example.vehiclerentalapp.models.Vehicle;

import java.util.List;

public class VehicleIndividualViewSlotAdapter extends RecyclerView.Adapter<VehicleIndividualViewSlotAdapter.SlotViewHolder> {

    private final List<Vehicle.VehicleSlot> slotList;

    public VehicleIndividualViewSlotAdapter(List<Vehicle.VehicleSlot> slotList) {
        this.slotList = slotList;
    }

    @NonNull
    @Override
    public SlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.individual_vehicle_slot_adapter, parent, false);
        return new SlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlotViewHolder holder, int position) {
        Vehicle.VehicleSlot slot = slotList.get(position);
        String duration = slot.getVehicleSlotDuration() + " " + (slot.getVehicleSlotDurationType() == 1 ? "min" : "hour");
        holder.vehicleBrand.setText(duration);
        holder.vehiclePrice.setText(String.format("₹ %.2f", slot.getVehicleSlotPrice()));
    }

    @Override
    public int getItemCount() {
        return slotList.size();
    }

    static class SlotViewHolder extends RecyclerView.ViewHolder {
        ImageView vehicleImage;
        TextView vehicleBrand, vehiclePrice;

        public SlotViewHolder(@NonNull View itemView) {
            super(itemView);
            vehicleImage = itemView.findViewById(R.id.vehicleImage);
            vehicleBrand = itemView.findViewById(R.id.vehicleBrand);
            vehiclePrice = itemView.findViewById(R.id.vehiclePrice);
        }
    }
}
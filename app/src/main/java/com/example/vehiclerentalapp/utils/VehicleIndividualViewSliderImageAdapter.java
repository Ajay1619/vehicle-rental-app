package com.example.vehiclerentalapp.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.vehiclerentalapp.R;

import java.util.List;

public class VehicleIndividualViewSliderImageAdapter extends RecyclerView.Adapter<VehicleIndividualViewSliderImageAdapter.ImageViewHolder> {

    private final List<String> imageUrls; // Changed from List<Integer> to List<String>
    private final Context context;

    public VehicleIndividualViewSliderImageAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.individual_vehicle_image_slider_util, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        Glide.with(context)
                .load( imageUrl)
                .error(R.mipmap.ic_launcher) // Replace with your error image
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageSliderItem);
        }
    }
}
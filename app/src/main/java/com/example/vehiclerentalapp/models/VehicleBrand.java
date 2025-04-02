package com.example.vehiclerentalapp.models;

import com.google.gson.annotations.SerializedName;

public class VehicleBrand {
    @SerializedName("brand_id")
    private int brandId;

    @SerializedName("brand_name")
    private String brandName;

    // Default constructor (required for Gson/Retrofit)
    public VehicleBrand() {}

    public VehicleBrand(int brandId, String brandName) {
        this.brandId = brandId;
        this.brandName = brandName;
    }

    // Getters and Setters
    public int getBrandId() { return brandId; }
    public void setBrandId(int brandId) { this.brandId = brandId; }
    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
}
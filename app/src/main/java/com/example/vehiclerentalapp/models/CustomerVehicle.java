package com.example.vehiclerentalapp.models;

import com.google.gson.annotations.SerializedName;

public class CustomerVehicle {
    @SerializedName("vehicle_id")
    private int vehicleId;

    @SerializedName("vehicle_title")
    private String vehicleTitle;

    @SerializedName("vehicle_brand")
    private String vehicleBrand; // Keep as String since JSON returns "5", "4", etc.

    @SerializedName("brand_name")
    private String vehicleBrandString; // Human-readable brand name

    @SerializedName("vehicle_model")
    private String vehicleModel;

    @SerializedName("vehicle_category_id")
    private Byte vehicleCategory;

    @SerializedName("vehicle_category_name")
    private String vehicleCategoryString;

    @SerializedName("vehicle_type_id")
    private Byte vehicleType;

    @SerializedName("vehicle_type_name")
    private String vehicleTypeString;

    @SerializedName("vehicle_status")
    private byte vehicleStatus;

    @SerializedName("vehicle_image_path")
    private String vehicleImagePath;

    @SerializedName("vehicle_color")
    private String vehicleColor; // Added

    @SerializedName("vehicle_seating_capacity")
    private int vehicleSeatingCapacity; // Added, using int as in DB

    @SerializedName("vehicle_luggage_capacity")
    private int vehicleLuggageCapacity; // Added, using int as in DB

    public CustomerVehicle() {}

    // Getters and Setters
    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }

    public String getVehicleTitle() { return vehicleTitle; }
    public void setVehicleTitle(String vehicleTitle) { this.vehicleTitle = vehicleTitle; }

    public String getVehicleBrand() { return vehicleBrand; }
    public void setVehicleBrand(String vehicleBrand) { this.vehicleBrand = vehicleBrand; }

    public String getVehicleBrandString() { return vehicleBrandString; }
    public void setVehicleBrandString(String vehicleBrandString) { this.vehicleBrandString = vehicleBrandString; }

    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }

    public Byte getVehicleCategory() { return vehicleCategory; }
    public void setVehicleCategory(Byte vehicleCategory) { this.vehicleCategory = vehicleCategory; }

    public String getVehicleCategoryString() { return vehicleCategoryString; }
    public void setVehicleCategoryString(String vehicleCategoryString) { this.vehicleCategoryString = vehicleCategoryString; }

    public Byte getVehicleType() { return vehicleType; }
    public void setVehicleType(Byte vehicleType) { this.vehicleType = vehicleType; }

    public String getVehicleTypeString() { return vehicleTypeString; }
    public void setVehicleTypeString(String vehicleTypeString) { this.vehicleTypeString = vehicleTypeString; }

    public byte getVehicleStatus() { return vehicleStatus; }
    public void setVehicleStatus(byte vehicleStatus) { this.vehicleStatus = vehicleStatus; }

    public String getVehicleImagePath() { return vehicleImagePath; }
    public void setVehicleImagePath(String vehicleImagePath) { this.vehicleImagePath = vehicleImagePath; }

    public String getVehicleColor() { return vehicleColor; }
    public void setVehicleColor(String vehicleColor) { this.vehicleColor = vehicleColor; }

    public int getVehicleSeatingCapacity() { return vehicleSeatingCapacity; }
    public void setVehicleSeatingCapacity(int vehicleSeatingCapacity) { this.vehicleSeatingCapacity = vehicleSeatingCapacity; }

    public int getVehicleLuggageCapacity() { return vehicleLuggageCapacity; }
    public void setVehicleLuggageCapacity(int vehicleLuggageCapacity) { this.vehicleLuggageCapacity = vehicleLuggageCapacity; }
}
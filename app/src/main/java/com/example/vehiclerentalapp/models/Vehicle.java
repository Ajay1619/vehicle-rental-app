package com.example.vehiclerentalapp.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Vehicle implements Serializable {
    private VehicleBasicInfo vehicleBasicInfo;
    private List<VehicleImage> vehicleImages;
    private List<VehicleSlot> vehicleSlots;

    // Default constructor allowing partial initialization (required for Retrofit)
    public Vehicle() {
        this.vehicleBasicInfo = null;
        this.vehicleImages = null;
        this.vehicleSlots = null;
    }

    // Nested class for vehicle basic information
    public static class VehicleBasicInfo implements Serializable {
        @SerializedName("vehicle_id")
        private int vehicleId;

        @SerializedName("vehicle_customer_id")
        private int vehicleCustomerId;

        @SerializedName("vehicle_type")
        private byte vehicleType; // 1 - Bike, 2 - Car, 3 - Bicycle

        @SerializedName("vehicle_title")
        private String vehicleTitle;

        @SerializedName("vehicle_brand")
        private int vehicleBrand;

        @SerializedName("vehicle_model")
        private String vehicleModel;

        @SerializedName("vehicle_category")
        private Byte vehicleCategory; // 1 - Petrol, 2 - Diesel, 3 - CNG, 4 - EV (nullable)

        @SerializedName("vehicle_category_name")
        private String vehicleCategoryTitle; //

        @SerializedName("vehicle_color")
        private String vehicleColor;

        @SerializedName("vehicle_seating_capacity")
        private int vehicleSeatingCapacity;

        @SerializedName("vehicle_luggage_capacity")
        private int vehicleLuggageCapacity;

        @SerializedName("vehicle_number")
        private String vehicleNumber;

        @SerializedName("rc_path")
        private String rcPath;

        // Default constructor (required for Retrofit)
        public VehicleBasicInfo() {
            this.vehicleId = 0;
            this.vehicleCustomerId = 0;
            this.vehicleType = 0;
            this.vehicleTitle = null;
            this.vehicleBrand = 0;
            this.vehicleModel = null;
            this.vehicleCategory = null;
            this.vehicleColor = null;
            this.vehicleSeatingCapacity = 0;
            this.vehicleLuggageCapacity = 0;
            this.vehicleNumber = null;
            this.rcPath = null;
        }

        // Full constructor with all fields
        public VehicleBasicInfo(int vehicleId, int vehicleCustomerId, byte vehicleType,
                                String vehicleTitle, int vehicleBrand, String vehicleModel,
                                Byte vehicleCategory, String vehicleColor,
                                int vehicleSeatingCapacity, int vehicleLuggageCapacity,
                                String vehicleNumber, String rcPath) {
            this.vehicleId = vehicleId;
            this.vehicleCustomerId = vehicleCustomerId;
            this.vehicleType = vehicleType;
            this.vehicleTitle = vehicleTitle;
            this.vehicleBrand = vehicleBrand;
            this.vehicleModel = vehicleModel;
            this.vehicleCategory = vehicleCategory;
            this.vehicleColor = vehicleColor;
            this.vehicleSeatingCapacity = vehicleSeatingCapacity;
            this.vehicleLuggageCapacity = vehicleLuggageCapacity;
            this.vehicleNumber = vehicleNumber;
            this.rcPath = rcPath;
        }

        // Getters and Setters with null safety where applicable
        public int getVehicleId() { return vehicleId; }
        public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }
        public int getVehicleCustomerId() { return vehicleCustomerId; }
        public void setVehicleCustomerId(int vehicleCustomerId) { this.vehicleCustomerId = vehicleCustomerId; }
        public byte getVehicleType() { return vehicleType; }
        public void setVehicleType(byte vehicleType) { this.vehicleType = vehicleType; }
        public String getVehicleTitle() { return vehicleTitle; }
        public void setVehicleTitle(String vehicleTitle) { this.vehicleTitle = vehicleTitle; }
        public int getVehicleBrand() { return vehicleBrand; }
        public void setVehicleBrand(int vehicleBrand) { this.vehicleBrand = vehicleBrand; }
        public String getVehicleModel() { return vehicleModel; }
        public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }

        public String getvehicleCategoryTitle() { return vehicleCategoryTitle; }
        public Byte getVehicleCategory() { return vehicleCategory; }
        public void setVehicleCategory(Byte vehicleCategory) { this.vehicleCategory = vehicleCategory; }
        public String getVehicleColor() { return vehicleColor; }
        public void setVehicleColor(String vehicleColor) { this.vehicleColor = vehicleColor; }
        public int getVehicleSeatingCapacity() { return vehicleSeatingCapacity; }
        public void setVehicleSeatingCapacity(int vehicleSeatingCapacity) { this.vehicleSeatingCapacity = vehicleSeatingCapacity; }
        public int getVehicleLuggageCapacity() { return vehicleLuggageCapacity; }
        public void setVehicleLuggageCapacity(int vehicleLuggageCapacity) { this.vehicleLuggageCapacity = vehicleLuggageCapacity; }
        public String getVehicleNumber() { return vehicleNumber; }
        public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }
        public String getrcPath() { return rcPath; }
        public void setrcPath(String rcPath) { this.rcPath = rcPath; }
    }

    // Nested class for vehicle images
    public static class VehicleImage implements Serializable {
        @SerializedName("vehicles_images_id")
        private int vehiclesImagesId;

        @SerializedName("vehicle_image_path")
        private String vehicleImagePath;

        @SerializedName("vehicle_image_order")
        private byte vehicleImageOrder;

        public VehicleImage() {}

        public VehicleImage(int vehiclesImagesId, String vehicleImagePath, byte vehicleImageOrder) {
            this.vehiclesImagesId = vehiclesImagesId;
            this.vehicleImagePath = vehicleImagePath;
            this.vehicleImageOrder = vehicleImageOrder;
        }

        public int getVehiclesImagesId() { return vehiclesImagesId; }
        public void setVehiclesImagesId(int vehiclesImagesId) { this.vehiclesImagesId = vehiclesImagesId; }
        public String getVehicleImagePath() { return vehicleImagePath; }
        public void setVehicleImagePath(String vehicleImagePath) { this.vehicleImagePath = vehicleImagePath; }
        public byte getVehicleImageOrder() { return vehicleImageOrder; }
        public void setVehicleImageOrder(byte vehicleImageOrder) { this.vehicleImageOrder = vehicleImageOrder; }
    }

    // Nested class for vehicle slots
    public static class VehicleSlot implements Serializable {
        @SerializedName("vehicle_slots_id")
        private int vehicleSlotsId;

        @SerializedName("vehicle_id")
        private int vehicleId;

        @SerializedName("vehicle_slot_duration")
        private String vehicleSlotDuration;

        @SerializedName("vehicle_slot_duration_type")
        private byte vehicleSlotDurationType; // 1 - min, 2 - hour

        @SerializedName("vehicle_slot_price")
        private double vehicleSlotPrice;

        public VehicleSlot() {}

        public VehicleSlot(int vehicleSlotsId, int vehicleId, String vehicleSlotDuration,
                           byte vehicleSlotDurationType, double vehicleSlotPrice) {
            this.vehicleSlotsId = vehicleSlotsId;
            this.vehicleId = vehicleId;
            this.vehicleSlotDuration = vehicleSlotDuration;
            this.vehicleSlotDurationType = vehicleSlotDurationType;
            this.vehicleSlotPrice = vehicleSlotPrice;
        }

        public int getVehicleSlotsId() { return vehicleSlotsId; }
        public void setVehicleSlotsId(int vehicleSlotsId) { this.vehicleSlotsId = vehicleSlotsId; }
        public int getVehicleId() { return vehicleId; }
        public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }
        public String getVehicleSlotDuration() { return vehicleSlotDuration; }
        public void setVehicleSlotDuration(String vehicleSlotDuration) { this.vehicleSlotDuration = vehicleSlotDuration; }
        public byte getVehicleSlotDurationType() { return vehicleSlotDurationType; }
        public void setVehicleSlotDurationType(byte vehicleSlotDurationType) { this.vehicleSlotDurationType = vehicleSlotDurationType; }
        public double getVehicleSlotPrice() { return vehicleSlotPrice; }
        public void setVehicleSlotPrice(double vehicleSlotPrice) { this.vehicleSlotPrice = vehicleSlotPrice; }
    }

    // Getters
    public VehicleBasicInfo getVehicleBasicInfo() { return vehicleBasicInfo; }
    public List<VehicleImage> getVehicleImages() { return vehicleImages; }
    public List<VehicleSlot> getVehicleSlots() { return vehicleSlots; }

    // Setters
    public void setVehicleBasicInfo(VehicleBasicInfo vehicleBasicInfo) { this.vehicleBasicInfo = vehicleBasicInfo; }
    public void setVehicleImages(List<VehicleImage> vehicleImages) { this.vehicleImages = vehicleImages; }
    public void setVehicleSlots(List<VehicleSlot> vehicleSlots) { this.vehicleSlots = vehicleSlots; }
}
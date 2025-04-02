package com.example.vehiclerentalapp.networks;

import com.example.vehiclerentalapp.models.BaseResponse;
import com.example.vehiclerentalapp.models.CustomerRegistration;
import com.example.vehiclerentalapp.models.GuestRegistration;
import com.example.vehiclerentalapp.models.Login;
import com.example.vehiclerentalapp.models.Vehicle;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiService {
    @POST("customer-registration?route=add&type=account")
    Call<BaseResponse> registerCustomer(@Body CustomerRegistration request);

    @GET("customer-registration?route=view&type=customer")
    Call<BaseResponse> getCustomerDetails(@Query("id") int accountId);

    @POST("customer-registration?route=edit&type=personal")
    Call<BaseResponse> updateCustomerPersonalInfo(
            @Query("id") int accountId,
            @Body CustomerRegistration customer
    );

    @POST("customer-registration?route=edit&type=address")
    Call<BaseResponse> updateCustomerAddressInfo(
            @Query("id") int accountId,
            @Body CustomerRegistration customer
    );

    @POST("customer-registration?route=edit&type=bank")
    Call<BaseResponse> updateCustomerBankInfo(
            @Query("id") int accountId,
            @Body CustomerRegistration customer
    );

    @Multipart
    @POST("customer-registration?route=edit&type=business")
    Call<BaseResponse> updateCustomerBusinessInfo(
            @Part("accountId") RequestBody accountId,
            @Part("businessName") RequestBody businessName,
            @Part("registerNumber") RequestBody registerNumber,
            @Part("gstin") RequestBody gstin,
            @Part MultipartBody.Part logoFile,
            @Part MultipartBody.Part licenseFile
    );

    @GET("guest-registration?route=view&type=guest")
    Call<BaseResponse> getGuestDetails(@Query("id") int accountId);

    @POST("guest-registration?route=add&type=account")
    Call<BaseResponse> registerGuestAccount(@Body GuestRegistration guest);

    @POST("guest-registration?route=edit&type=personal")
    Call<BaseResponse> updateGuestPersonalInfo(
            @Query("id") int accountId,
            @Body GuestRegistration guest
    );

    @POST("login")
    Call<BaseResponse> loginUser(@Body Login loginRequest);

    @GET("vehicles?route=view&type=vehicle")
    Call<BaseResponse> getVehicleById(@Query("vehicleId") int vehicleId);

    @Multipart
    @POST("vehicles?route=add&type=basic")
    Call<BaseResponse> addVehicle(
            @Part("vehicleId") RequestBody vehicleId,
            @Part("customerId") RequestBody customerId,
            @Part("vehicleType") RequestBody vehicleType,
            @Part("vehicleTitle") RequestBody vehicleTitle,
            @Part("vehicleBrandId") RequestBody vehicleBrandId,
            @Part("vehicleModel") RequestBody vehicleModel,
            @Part("vehicleCategory") RequestBody vehicleCategory,
            @Part("vehicleColor") RequestBody vehicleColor,
            @Part("seatingCapacity") RequestBody seatingCapacity,
            @Part("luggageCapacity") RequestBody luggageCapacity,
            @Part("vehicleNumber") RequestBody vehicleNumber,
            @Part MultipartBody.Part registrationCertificate
    );

    @Multipart
    @POST("vehicles?route=add&type=images")
    Call<BaseResponse> uploadVehicleImages(
            @Part("pr_vehicle_id") RequestBody prVehicleId,
            @Part("vehicle_id") RequestBody vehicleId,
            @Part List<MultipartBody.Part> images,
            @Part("image_order") RequestBody imageOrder
    );

    @POST("vehicles?route=add&type=slots")
    Call<BaseResponse> addVehicleSlots(
            @Query("vehicle_id") int vehicleId,
            @Body List<Vehicle.VehicleSlot> slots
    );

    @GET("vehicles?route=view&type=vehicles")
    Call<BaseResponse> getCustomerVehicles(@Query("customerId") int customerId);

    @GET("vehicles?route=view&type=vehicles")
    Call<ResponseBody> getCustomerVehiclesRaw(
            @Query("customerId") int customerId,
            @Query("vehicleType") Integer vehicleType // Added vehicleType parameter, nullable
    );

    @POST("vehicles?route=edit&type=vehicle_status")
    Call<Void> updateVehicleStatus(
            @Query("vehicleId") int vehicleId,
            @Query("isActive") boolean isActive
    );

    @GET("vehicles?route=delete&type=vehicle")
    Call<Void> deleteVehicle(@Query("vehicle_id") int vehicleId);

    @GET("vehicles?route=view&type=vehicle_brands")
    Call<BaseResponse> getBrandsByVehicleType(@Query("vehicleType") byte vehicleType);
}
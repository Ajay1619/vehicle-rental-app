package com.example.vehiclerentalapp.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.vehiclerentalapp.R;
import com.example.vehiclerentalapp.models.BaseResponse;
import com.example.vehiclerentalapp.models.Vehicle;
import com.example.vehiclerentalapp.networks.ApiClient;
import com.example.vehiclerentalapp.networks.ApiService;
import com.example.vehiclerentalapp.utils.LoadingScreen;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerAddVehiclesImages extends Fragment {

    private static final String ARG_VEHICLE = "vehicle";
    private static final String ARG_PR_VEHICLE_ID = "pr_vehicle_id";
    private static final String TAG = "CustomerAddVehiclesImages";

    private Vehicle vehicle;
    private LinearLayout previewContainer;
    private TextView rearrangeHint;
    private List<ImageItem> imageItems = new ArrayList<>();
    private List<ImageItem> originalImageItems = new ArrayList<>();
    private LoadingScreen loadingScreen;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private boolean hasUnsavedChanges = false;
    private int prVehicleId = -1;

    private static class ImageItem {
        Uri uri; // For local images
        String serverPath; // For server-side images
        boolean isLocal; // True if local, false if from server

        ImageItem(Uri uri, boolean isLocal) {
            this.uri = uri;
            this.isLocal = isLocal;
            this.serverPath = null;
        }

        ImageItem(String serverPath) {
            this.serverPath = serverPath;
            this.isLocal = false;
            this.uri = Uri.parse(serverPath);
        }
    }

    public static CustomerAddVehiclesImages newInstance(Vehicle vehicle, int prVehicleId) {
        CustomerAddVehiclesImages fragment = new CustomerAddVehiclesImages();
        Bundle args = new Bundle();
        args.putSerializable(ARG_VEHICLE, vehicle);
        args.putInt(ARG_PR_VEHICLE_ID, prVehicleId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            vehicle = (Vehicle) getArguments().getSerializable(ARG_VEHICLE);
            prVehicleId = getArguments().getInt(ARG_PR_VEHICLE_ID, -1);
            Log.d(TAG, "onCreate: prVehicleId=" + prVehicleId);
        }

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                Intent data = result.getData();
                if (data.getClipData() != null) {
                    int count = Math.min(data.getClipData().getItemCount(), 5 - imageItems.size());
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        imageItems.add(new ImageItem(imageUri, true));
                        addImagePreview(imageUri, true);
                    }
                } else if (data.getData() != null && imageItems.size() < 5) {
                    Uri imageUri = data.getData();
                    imageItems.add(new ImageItem(imageUri, true));
                    addImagePreview(imageUri, true);
                }
                if (imageItems.size() >= 5) {
                    Toast.makeText(requireContext(), "Maximum 5 images allowed", Toast.LENGTH_SHORT).show();
                }
                rearrangeHint.setVisibility(imageItems.isEmpty() ? View.GONE : View.VISIBLE);
                hasUnsavedChanges = true;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_add_vehicle_images, container, false);

        loadingScreen = new LoadingScreen(requireContext());
        previewContainer = view.findViewById(R.id.preview_container);
        rearrangeHint = view.findViewById(R.id.rearrange_hint);
        MaterialButton btnBrowseFile = view.findViewById(R.id.btnBrowseFile);
        MaterialButton btnSubmit = view.findViewById(R.id.CustomerAddVehicleImgsSbtBtn);

        btnBrowseFile.setOnClickListener(v -> {
            if (imageItems.size() < 5) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                imagePickerLauncher.launch(intent);
            } else {
                Toast.makeText(requireContext(), "Maximum 5 images already selected", Toast.LENGTH_SHORT).show();
            }
        });

        btnSubmit.setOnClickListener(v -> submitImages());

        loadImagesFromModel();
        return view;
    }

    private void loadImagesFromModel() {
        if (vehicle != null && vehicle.getVehicleImages() != null && !vehicle.getVehicleImages().isEmpty()) {
            List<Vehicle.VehicleImage> vehicleImages = vehicle.getVehicleImages();
            Collections.sort(vehicleImages, Comparator.comparingInt(Vehicle.VehicleImage::getVehicleImageOrder));
            for (Vehicle.VehicleImage image : vehicleImages) {
                String imagePath = image.getVehicleImagePath();
                imageItems.add(new ImageItem(imagePath));
                originalImageItems.add(new ImageItem(imagePath));
                addImagePreview(Uri.parse(imagePath), false);
            }
            rearrangeHint.setVisibility(View.VISIBLE);
        }
    }

    private void addImagePreview(Uri imageUri, boolean isLocal) {
        CardView cardView = new CardView(requireContext());
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(8, 0, 8, 0);
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(16f);
        cardView.setCardElevation(4f);
        cardView.setCardBackgroundColor(requireContext().getResources().getColor(android.R.color.white));

        LinearLayout imageContainer = new LinearLayout(requireContext());
        imageContainer.setLayoutParams(new LinearLayout.LayoutParams(180, 180));

        ImageView imageView = new ImageView(requireContext());
        imageView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        if (isLocal) {
            Glide.with(this)
                    .load(imageUri)
                    .apply(new RequestOptions()
                            .override(180, 180)
                            .centerCrop()
                            .transform(new RoundedCorners(24))
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.error_image))
                    .into(imageView);
        } else {
            Glide.with(this)
                    .load(getString(R.string.api_url) + "/uploads/vehicles_images/" + imageUri)
                    .apply(new RequestOptions()
                            .override(180, 180)
                            .centerCrop()
                            .transform(new RoundedCorners(24))
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.error_image))
                    .into(imageView);
        }

        ImageView removeButton = new ImageView(requireContext());
        LinearLayout.LayoutParams removeParams = new LinearLayout.LayoutParams(40, 40);
        removeButton.setLayoutParams(removeParams);
        removeButton.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        removeButton.setBackgroundResource(R.drawable.circle_remove_background);
        removeButton.setPadding(8, 8, 8, 8);
        removeButton.setColorFilter(android.graphics.Color.WHITE);
        removeButton.setTranslationX(130f);
        removeButton.setTranslationY(10f);
        removeButton.setOnClickListener(v -> {
            int index = previewContainer.indexOfChild(cardView);
            imageItems.remove(index);
            previewContainer.removeView(cardView);
            rearrangeHint.setVisibility(imageItems.isEmpty() ? View.GONE : View.VISIBLE);
            Toast.makeText(requireContext(), "Image removed", Toast.LENGTH_SHORT).show();
            hasUnsavedChanges = true;
        });

        imageContainer.addView(imageView);
        cardView.addView(imageContainer);
        cardView.addView(removeButton);
        previewContainer.addView(cardView);

        imageView.setOnLongClickListener(v -> {
            v.startDragAndDrop(null, new View.DragShadowBuilder(v), new DragData(imageUri, cardView), 0);
            return true;
        });

        cardView.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;
                case DragEvent.ACTION_DROP:
                    DragData dragData = (DragData) event.getLocalState();
                    CardView draggedCard = (CardView) dragData.layout;
                    int sourceIndex = previewContainer.indexOfChild(draggedCard);
                    int targetIndex = previewContainer.indexOfChild(cardView);
                    if (sourceIndex != targetIndex) {
                        previewContainer.removeView(draggedCard);
                        previewContainer.addView(draggedCard, targetIndex);
                        ImageItem item = imageItems.remove(sourceIndex);
                        imageItems.add(targetIndex, item);
                        hasUnsavedChanges = true;
                    }
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    return true;
            }
            return true;
        });
    }

    private boolean hasOrderChanged() {
        if (imageItems.size() != originalImageItems.size()) return true;
        for (int i = 0; i < imageItems.size(); i++) {
            ImageItem current = imageItems.get(i);
            ImageItem original = originalImageItems.get(i);
            if (current.isLocal != original.isLocal) return true;
            if (!current.isLocal && !current.serverPath.equals(original.serverPath)) return true;
        }
        return false;
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = "image_" + System.currentTimeMillis();
        String[] projection = {MediaStore.Images.Media.DISPLAY_NAME};
        try (android.database.Cursor cursor = requireContext().getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting filename: " + e.getMessage());
        }
        return fileName;
    }

    private void submitImages() {
        if (imageItems.isEmpty()) {
            Toast.makeText(requireContext(), "Please select at least one image", Toast.LENGTH_SHORT).show();
            return;
        }

        if (prVehicleId == -1 && vehicle != null && vehicle.getVehicleBasicInfo() != null) {
            prVehicleId = vehicle.getVehicleBasicInfo().getVehicleId();
        }

        SharedPreferences prefs = requireActivity().getSharedPreferences("customer_update_vehicle", Context.MODE_PRIVATE);
        int vehicleId = prefs.getInt("vehicle_id", -1);
        if (vehicleId == -1) {
            Toast.makeText(requireContext(), "New Vehicle ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Removed the condition to check for changes
        loadingScreen.show("Uploading...");
        ApiService apiService = ApiClient.getApiService();

        List<MultipartBody.Part> imageParts = new ArrayList<>();
        for (ImageItem item : imageItems) {
            if (item.isLocal) {
                File file = new File(requireContext().getCacheDir(), getFileNameFromUri(item.uri));
                try {
                    java.io.InputStream inputStream = requireContext().getContentResolver().openInputStream(item.uri);
                    if (inputStream != null) {
                        java.io.FileOutputStream outputStream = new java.io.FileOutputStream(file);
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        outputStream.close();
                        inputStream.close();
                        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
                        imageParts.add(MultipartBody.Part.createFormData("images[]", file.getName(), requestFile));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error preparing image: " + e.getMessage());
                    loadingScreen.dismiss();
                    Toast.makeText(requireContext(), "Error uploading image", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                String serverFileName = new File(item.serverPath).getName();
                RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), item.serverPath);
                imageParts.add(MultipartBody.Part.createFormData("images[]", serverFileName, requestBody));
            }
        }

        RequestBody prVehicleIdPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(prVehicleId));
        RequestBody vehicleIdPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(vehicleId));
        RequestBody imageOrderPart = RequestBody.create(MediaType.parse("text/plain"), "1");

        apiService.uploadVehicleImages(prVehicleIdPart, vehicleIdPart, imageParts, imageOrderPart)
                .enqueue(new Callback<BaseResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                        loadingScreen.dismiss();
                        if (response.isSuccessful() && response.body() != null) {
                            BaseResponse baseResponse = response.body();
                            if (baseResponse.getCode() == 200 && "success".equals(baseResponse.getStatus())) {
                                Toast.makeText(requireContext(), "Images processed successfully", Toast.LENGTH_SHORT).show();
                                hasUnsavedChanges = false;
                                originalImageItems.clear();
                                for (ImageItem item : imageItems) {
                                    originalImageItems.add(item.isLocal ?
                                            new ImageItem(item.uri, true) :
                                            new ImageItem(item.serverPath));
                                }
                                navigateToNextFragment();
                            } else {
                                Toast.makeText(requireContext(), "Failed to process images: " +
                                        baseResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            try {
                                Toast.makeText(requireContext(), "Server error: " +
                                        response.errorBody().string(), Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(requireContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                        loadingScreen.dismiss();
                        Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToNextFragment() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("customer_update_vehicle", Context.MODE_PRIVATE);
        int vehicleId = prefs.getInt("vehicle_id", -1);

        Fragment nextFragment = CustomerAddVehicleSlots.newInstance(vehicle, vehicleId);
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.customer_vehicles_fragment_container, nextFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (hasUnsavedChanges) {
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Unsaved Changes")
                            .setMessage("You have unsaved changes. Are you sure you want to proceed?")
                            .setPositiveButton("Yes", (dialog, which) -> requireActivity().getOnBackPressedDispatcher().onBackPressed())
                            .setNegativeButton("No", null)
                            .setCancelable(false)
                            .show();
                } else {
                    setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        });
    }

    private static class DragData {
        Uri uri;
        View layout;

        DragData(Uri uri, View layout) {
            this.uri = uri;
            this.layout = layout;
        }
    }
}
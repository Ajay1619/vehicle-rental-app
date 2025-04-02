package com.example.vehiclerentalapp.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.example.vehiclerentalapp.R;
import com.example.vehiclerentalapp.models.BaseResponse;
import com.example.vehiclerentalapp.models.Login;
import com.example.vehiclerentalapp.networks.ApiClient;
import com.example.vehiclerentalapp.networks.ApiService;
import com.example.vehiclerentalapp.ui.activity.MainActivity;
import com.example.vehiclerentalapp.utils.LoadingScreen;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import java.io.File;
import java.util.Map;

public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    private TextInputEditText etUsername, etPassword;
    private MaterialButton btnLogin;
    private ImageView ivProfilePic;
    private SharedPreferences loginPrefs;

    public LoginFragment() {
        // Required empty public constructor
    }

    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        loginPrefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        btnLogin = rootView.findViewById(R.id.btnLogin);
        TextView tvRegister = rootView.findViewById(R.id.Register);
        etUsername = rootView.findViewById(R.id.login_username_textfield);
        etPassword = rootView.findViewById(R.id.login_password_textfield);

        String text = "Don't have an Account? Register here";
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.colorTertiary)),
                text.indexOf("Register here"),
                text.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvRegister.setText(spannableString);

        tvRegister.setOnClickListener(v -> showRegisterDialog());
        btnLogin.setOnClickListener(v -> submitLogin());

        return rootView;
    }

    private void submitLogin() {
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Login loginRequest = new Login();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        LoadingScreen loadingScreen = new LoadingScreen(requireContext());
        loadingScreen.show("Logging in...");

        ApiService apiService = ApiClient.getApiService();
        Call<BaseResponse> call = apiService.loginUser(loginRequest);
        call.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {

                if (response.isSuccessful()) {
                    BaseResponse baseResponse = response.body();
                    if (baseResponse != null && baseResponse.getCode() == 200 && "success".equals(baseResponse.getStatus())) {
                        Toast.makeText(requireContext(), baseResponse.getMessage(), Toast.LENGTH_SHORT).show();


                        // Get the nested "data" map
                        Map<String, Object> userData = baseResponse.getNestedMap("data");
                        if (userData == null) {
                            Toast.makeText(requireContext(), "Invalid server response format", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Store user data in SharedPreferences
                        SharedPreferences.Editor editor = loginPrefs.edit();
                        Integer userId = baseResponse.getIntFromMap(userData, "user_id");
                        Integer accountId = baseResponse.getIntFromMap(userData, "account_id");
                        String accountCode = baseResponse.getStringFromMap(userData, "account_code");
                        String name = baseResponse.getStringFromMap(userData, "name");
                        String imageUrl = baseResponse.getStringFromMap(userData, "image_url");


                        if (userId != null) editor.putInt("user_id", userId);
                        if (accountId != null) editor.putInt("account_id", accountId);
                        if (accountCode != null) editor.putString("account_code", accountCode);
                        if (name != null) editor.putString("name", name);
                        if (imageUrl != null) editor.putString("image_url", imageUrl);
                        editor.apply();


                        // Cache the image locally with Glide
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            new Thread(() -> {
                                try {
                                    File cachedFile = Glide.with(requireContext())
                                            .downloadOnly()
                                            .load(imageUrl)
                                            .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                                            .get(); // Blocks until the file is downloaded
                                    Log.d(TAG, "Image cached at: " + cachedFile.getAbsolutePath());
                                } catch (Exception e) {
                                    Log.e(TAG, "Glide caching error: " + e.getMessage());
                                }
                            }).start();
                        }
                        loadingScreen.dismiss();
                        // Navigate to MainActivity
                        Intent intent = new Intent(requireActivity(), MainActivity.class);
                        startActivity(intent);
                        requireActivity().finish();
                    } else {
                        String errorMsg = baseResponse != null ? baseResponse.getMessage() : "Login failed";
                        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
                    }
                    loadingScreen.dismiss();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Toast.makeText(requireContext(), "Server error: " + errorBody, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Server error: " + errorBody);
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Response parsing error: " + e.getMessage());
                    }
                    loadingScreen.dismiss();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                loadingScreen.dismiss();
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Network error: " + t.getMessage());
                loadingScreen.dismiss();
            }
        });
    }

    private void showRegisterDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_register_choice);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);

        LinearLayout llCustomer = dialog.findViewById(R.id.llVendor);
        LinearLayout llDriver = dialog.findViewById(R.id.llDriver);

        llCustomer.setOnClickListener(v -> {
            dialog.dismiss();
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentContainer, new CustomerAccountInfoFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        llDriver.setOnClickListener(v -> {
            dialog.dismiss();
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentContainer, new GuestAccountInfoFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        dialog.show();
    }
}
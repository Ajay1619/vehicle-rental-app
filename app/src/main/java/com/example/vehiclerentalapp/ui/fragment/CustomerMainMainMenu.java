package com.example.vehiclerentalapp.ui.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.vehiclerentalapp.R;
import com.example.vehiclerentalapp.ui.activity.CustomerBusinessActivity;
import com.example.vehiclerentalapp.ui.activity.CustomerVehiclesActivity;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CustomerMainMainMenu#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CustomerMainMainMenu extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CustomerMainMainMenu() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_customer_main_menu.
     */
    // TODO: Rename and change types and number of parameters
    public static CustomerMainMainMenu newInstance(String param1, String param2) {
        CustomerMainMainMenu fragment = new CustomerMainMainMenu();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_customer_main_menu, container, false);

        // Find the menu card by ID
        View businessMenuCard = view.findViewById(R.id.your_business_menu_card);
        View vehiclesMenuCard = view.findViewById(R.id.vehicles_menu_card);
        // Set OnClickListener to navigate to CustomerBusinessActivity
        businessMenuCard.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CustomerBusinessActivity.class);
            startActivity(intent);
        });
        vehiclesMenuCard.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CustomerVehiclesActivity.class);
            startActivity(intent);
        });
        return view;
    }

}
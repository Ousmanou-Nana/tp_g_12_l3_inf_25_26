package com.example.tp_g_12_l3_inf_25_26.ui.admin.home;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tp_g_12_l3_inf_25_26.R;

public class HomeAdminFragment extends Fragment {

    private HomeAdminViewModel mViewModel;

    public static HomeAdminFragment newInstance() {
        return new HomeAdminFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_admin, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(HomeAdminViewModel.class);
        // TODO: Use the ViewModel
    }

}
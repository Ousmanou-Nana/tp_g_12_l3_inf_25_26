package com.example.tp_g_12_l3_inf_25_26.ui.admin.home;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.tp_g_12_l3_inf_25_26.R;
import com.example.tp_g_12_l3_inf_25_26.ui.admin.addobject.AddObjectFragment;
import com.example.tp_g_12_l3_inf_25_26.ui.admin.listdeclaration.ListDeclarationFragment;
import com.example.tp_g_12_l3_inf_25_26.ui.admin.listobject.ListObjectFragment;

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

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(HomeAdminViewModel.class);

        view.findViewById(R.id.buttonAddObject).setOnClickListener(v ->open(AddObjectFragment.newInstance()));
        view.findViewById(R.id.buttonListDeclarations).setOnClickListener(v -> open(ListDeclarationFragment.newInstance()));
        view.findViewById(R.id.buttonlistObject).setOnClickListener(v -> open(ListObjectFragment.newInstance()));


    }
    private void open(Fragment fragment) {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.main_admin_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}


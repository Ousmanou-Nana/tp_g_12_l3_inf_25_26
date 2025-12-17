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

        Button buttonAddObject = view.findViewById(R.id.buttonAddObject);
        buttonAddObject.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.main_admin_container, AddObjectFragment.newInstance())
                    .addToBackStack(null)
                    .commit();
        });

//        Button buttonListDeclarations = view.findViewById(R.id.buttonListDeclarations);
//        buttonListDeclarations.setOnClickListener(v -> {
//            getParentFragmentManager().beginTransaction()
//                    .replace(R.id.main_admin_container, ListDeclarationFragment.newInstance())
//                    .addToBackStack(null)
//                    .commit();
//        });
//
//        Button buttonListObject = view.findViewById(R.id.buttonlistObject);
//        buttonListObject.setOnClickListener(v -> {
//            getParentFragmentManager().beginTransaction()
//                    .replace(R.id.main_admin_container, ListObjectFragment.newInstance())
//                    .addToBackStack(null)
//                    .commit();
//        });
    }
}


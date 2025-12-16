package com.example.tp_g_12_l3_inf_25_26.ui.user.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tp_g_12_l3_inf_25_26.R;
import com.example.tp_g_12_l3_inf_25_26.ui.user.mydeclaration.MyDeclaration;
import com.example.tp_g_12_l3_inf_25_26.ui.user.declareobjectfrom.UserDeclareObjectFrom;
import com.example.tp_g_12_l3_inf_25_26.ui.user.LostList.UserLostList;

public class HomeUserFragment extends Fragment {
    private HomeUserViewModel mViewModel;

    public static HomeUserFragment newInstance() {
        return new HomeUserFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(HomeUserViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_home_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button buttonDeclare = view.findViewById(R.id.buttonDeclare);
        Button buttonLostList = view.findViewById(R.id.buttonLostList);
        Button buttonMyDeclarations = view.findViewById(R.id.buttonMyDeclarations);

        buttonDeclare.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_user_container, UserDeclareObjectFrom.newInstance())
                    .addToBackStack(null)
                    .commit();
        });

        buttonLostList.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_user_container, UserLostList.newInstance())
                    .addToBackStack(null)
                    .commit();
        });

        buttonMyDeclarations.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_user_container, MyDeclaration.newInstance())
                    .addToBackStack(null)
                    .commit();
        });
    }

}
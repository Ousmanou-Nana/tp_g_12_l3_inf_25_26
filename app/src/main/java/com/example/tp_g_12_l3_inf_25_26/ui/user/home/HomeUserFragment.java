package com.example.tp_g_12_l3_inf_25_26.ui.user.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tp_g_12_l3_inf_25_26.R;
import com.example.tp_g_12_l3_inf_25_26.ui.user.mydeclaration.MyDeclaration;
import com.example.tp_g_12_l3_inf_25_26.ui.user.declareobjectfrom.UserDeclareObjectFrom;
import com.example.tp_g_12_l3_inf_25_26.ui.user.lostlist.UserLostList;

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
        
        mViewModel = new ViewModelProvider(this).get(HomeUserViewModel.class);

        view.findViewById(R.id.buttonDeclare)
                .setOnClickListener(v -> open(UserDeclareObjectFrom.newInstance()));

        view.findViewById(R.id.buttonLostList)
                .setOnClickListener(v -> open(UserLostList.newInstance()));

        view.findViewById(R.id.buttonMyDeclarations)
                .setOnClickListener(v -> open(MyDeclaration.newInstance()));
    }

    private void open(Fragment fragment) {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.main_user_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
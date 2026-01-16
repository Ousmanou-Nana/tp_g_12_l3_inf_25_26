package com.example.tp_g_12_l3_inf_25_26.ui.admin.loging;

import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.tp_g_12_l3_inf_25_26.R;
import com.example.tp_g_12_l3_inf_25_26.ui.admin.home.HomeAdminFragment;

public class LoginAdminFragment extends Fragment {

    private LogingViewModel mViewModel;
    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;

    private static final String PREFS_NAME = "AdminPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_ADMIN_ID = "adminId";

    public static LoginAdminFragment newInstance() {
        return new LoginAdminFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(LogingViewModel.class);

        // Observer pour le résultat de connexion
        mViewModel.getLoginResult().observe(this, result -> {
            if (result != null) {
                Toast.makeText(requireContext(), result.getMessage(), Toast.LENGTH_SHORT).show();

                if (result.isSuccess()) {
                    // Sauvegarder l'état de connexion avec l'ID admin
                    saveLoginState(true, result.getAdminId());

                    // Naviguer vers HomeAdminFragment
                    navigateToHome();
                }
            }
        });

        // Observer pour l'état de chargement
        mViewModel.getIsLoading().observe(this, isLoading -> {
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
            if (btnLogin != null) {
                btnLogin.setEnabled(!isLoading);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login_admin, container, false);

        // Initialiser les vues
        etUsername = view.findViewById(R.id.et_username);
        etPassword = view.findViewById(R.id.et_password);
        btnLogin = view.findViewById(R.id.btn_login);
        progressBar = view.findViewById(R.id.progress_bar);

        // Configurer le bouton de connexion
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();

            mViewModel.setUsername(username);
            mViewModel.setPassword(password);
            mViewModel.login();
        });

        return view;
    }

    private void saveLoginState(boolean isLoggedIn, int adminId) {
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
                .putInt(KEY_ADMIN_ID, adminId)
                .apply();
    }

    private void navigateToHome() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_admin_container, HomeAdminFragment.newInstance())
                .commit();
    }

    public static boolean isAdminLoggedIn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public static void logout(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .remove(KEY_ADMIN_ID)
                .apply();
    }

    public static int getLoggedInAdminId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_ADMIN_ID, -1);
    }
}
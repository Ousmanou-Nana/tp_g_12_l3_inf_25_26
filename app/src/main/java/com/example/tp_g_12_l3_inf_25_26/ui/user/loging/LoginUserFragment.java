package com.example.tp_g_12_l3_inf_25_26.ui.user.loging;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tp_g_12_l3_inf_25_26.R;
import com.example.tp_g_12_l3_inf_25_26.ui.user.home.HomeUserFragment;

public class LoginUserFragment extends Fragment {

    private LogingViewModel mViewModel;
    private EditText etUsername;
    private EditText etPhone;
    private EditText etMatricule;
    private Button btnLogin;
    private ProgressBar progressBar;

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_MATRICULE = "userMatricule";

    public static LoginUserFragment newInstance() {
        return new LoginUserFragment();
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
                    // Sauvegarder l'état de connexion avec les infos utilisateur
                    saveLoginState(true, result.getUserId(), result.getUserName(), result.getMatricule());

                    // Naviguer vers HomeUserFragment
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
        View view = inflater.inflate(R.layout.fragment_login_user, container, false);

        // Initialiser les vues
        etUsername = view.findViewById(R.id.et_user_username);
        etPhone = view.findViewById(R.id.et_user_phone);
        etMatricule = view.findViewById(R.id.et_user_matricule);
        btnLogin = view.findViewById(R.id.btn_login);
        progressBar = view.findViewById(R.id.progress_bar);

        // Configurer le bouton de connexion
        btnLogin.setOnClickListener(v -> handleLogin());

        return view;
    }

    private void handleLogin() {
        String username = etUsername.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String matricule = etMatricule.getText().toString().trim();

        // Validation
        if (username.isEmpty()) {
            etUsername.setError("Le nom est requis");
            etUsername.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            etPhone.setError("Le téléphone est requis");
            etPhone.requestFocus();
            return;
        }

        if (matricule.isEmpty()) {
            etMatricule.setError("Le matricule est requis");
            etMatricule.requestFocus();
            return;
        }

        // Appeler la méthode de connexion du ViewModel
        mViewModel.login(username, phone, matricule);
    }

    private void saveLoginState(boolean isLoggedIn, int userId, String userName, String matricule) {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, userName);
        editor.putString(KEY_USER_MATRICULE, matricule);
        editor.apply();
    }

    private void navigateToHome() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_user_container, HomeUserFragment.newInstance())
                    .commit();
        }
    }
}

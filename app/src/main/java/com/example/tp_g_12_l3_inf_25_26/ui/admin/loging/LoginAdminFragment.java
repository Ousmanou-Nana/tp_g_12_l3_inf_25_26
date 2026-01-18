// ============================================================================
// LoginAdminFragment.java
// ============================================================================
// Fragment qui gère l'interface utilisateur de la page de connexion
// Affiche les champs de saisie et gère les interactions utilisateur
// ============================================================================

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

    // Référence au ViewModel qui contient la logique
    private LogingViewModel mViewModel;

    // ===== Références aux éléments de l'interface =====
    private EditText etUsername;      // Champ de saisie du nom d'utilisateur
    private EditText etPassword;      // Champ de saisie du mot de passe
    private Button btnLogin;          // Bouton de connexion
    private ProgressBar progressBar;  // Indicateur de chargement

    // ===== Constantes pour SharedPreferences =====
    // SharedPreferences permet de stocker des données de manière persistante
    private static final String PREFS_NAME = "AdminPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_ADMIN_ID = "adminId";

    /**
     * Méthode factory pour créer une instance du fragment
     * @return Nouvelle instance de LoginAdminFragment
     */
    public static LoginAdminFragment newInstance() {
        return new LoginAdminFragment();
    }

    /**
     * Appelé lors de la création du fragment
     * Configure le ViewModel et les observateurs
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ===== INITIALISATION DU VIEWMODEL =====
        // Crée ou récupère le ViewModel associé à ce fragment
        mViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(LogingViewModel.class);

        // ===== OBSERVATION DU RÉSULTAT DE CONNEXION =====
        // S'exécute automatiquement quand loginResult change dans le ViewModel
        mViewModel.getLoginResult().observe(this, result -> {
            if (result != null) {
                // Affiche un message Toast avec le résultat
                Toast.makeText(requireContext(), result.getMessage(), Toast.LENGTH_SHORT).show();

                if (result.isSuccess()) {
                    // Sauvegarde l'état de connexion en mémoire persistante
                    saveLoginState(true, result.getAdminId());

                    // Navigue vers la page d'accueil admin
                    navigateToHome();
                }
            }
        });

        // ===== OBSERVATION DE L'ÉTAT DE CHARGEMENT =====
        // Gère l'affichage de la barre de progression
        mViewModel.getIsLoading().observe(this, isLoading -> {
            if (progressBar != null) {
                // Affiche/cache la barre de progression
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
            if (btnLogin != null) {
                // Active/désactive le bouton pendant le chargement
                btnLogin.setEnabled(!isLoading);
            }
        });
    }

    /**
     * Crée et retourne la vue du fragment
     * Configure les éléments de l'interface et leurs listeners
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate le layout XML en une vue
        View view = inflater.inflate(R.layout.fragment_login_admin, container, false);

        // ===== LIAISON DES VUES =====
        // Récupère les références des éléments définis dans le XML
        etUsername = view.findViewById(R.id.et_username);
        etPassword = view.findViewById(R.id.et_password);
        btnLogin = view.findViewById(R.id.btn_login);
        progressBar = view.findViewById(R.id.progress_bar);

        // ===== CONFIGURATION DU BOUTON DE CONNEXION =====
        // Définit ce qui se passe quand on clique sur "Se connecter"
        btnLogin.setOnClickListener(v -> {
            // Récupère les valeurs saisies
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();

            // Envoie les données au ViewModel
            mViewModel.setUsername(username);
            mViewModel.setPassword(password);

            // Déclenche la tentative de connexion
            mViewModel.login();
        });

        return view;
    }

    /**
     * Sauvegarde l'état de connexion dans SharedPreferences
     * Permet de garder l'utilisateur connecté même après fermeture de l'app
     *
     * @param isLoggedIn true si l'utilisateur est connecté
     * @param adminId ID de l'administrateur connecté
     */
    private void saveLoginState(boolean isLoggedIn, int adminId) {
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Édite et sauvegarde les préférences
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
                .putInt(KEY_ADMIN_ID, adminId)
                .apply();  // apply() sauvegarde de manière asynchrone
    }

    /**
     * Navigue vers le fragment d'accueil de l'administrateur
     * Remplace le fragment de connexion par le fragment home
     */
    private void navigateToHome() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_admin_container, HomeAdminFragment.newInstance())
                .commit();
    }

    /**
     * Méthode statique pour vérifier si un admin est connecté
     * Peut être appelée depuis n'importe où dans l'application
     *
     * @param context Contexte Android
     * @return true si un admin est connecté, false sinon
     */
    public static boolean isAdminLoggedIn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Récupère l'ID de l'admin actuellement connecté
     *
     * @param context Contexte Android
     * @return ID de l'admin ou -1 si aucun admin connecté
     */
    public static int getLoggedInAdminId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_ADMIN_ID, -1);
    }
}
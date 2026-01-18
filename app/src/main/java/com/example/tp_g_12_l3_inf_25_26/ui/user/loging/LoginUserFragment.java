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

/**
 * Fragment de connexion/inscription pour les utilisateurs.
 * <p>
 * Ce fragment gère l'interface utilisateur pour l'authentification avec une approche
 * combinée connexion/inscription:
 * - Les utilisateurs existants se connectent avec leurs informations
 * - Les nouveaux utilisateurs sont automatiquement inscrits
 * <p>
 * Fonctionnalités principales:
 * - Formulaire de connexion avec validation
 * - Persistance de la session utilisateur via SharedPreferences
 * - Affichage d'indicateurs de chargement
 * - Navigation automatique vers l'écran d'accueil après connexion
 * - Feedback utilisateur via Toast
 * <p>
 * Architecture:
 * - Pattern MVVM avec LogingViewModel
 * - Observateurs LiveData pour communication réactive
 * - Gestion d'état via SharedPreferences
 *
 * @author Votre équipe
 * @version 1.0
 */
public class LoginUserFragment extends Fragment {

    // ==================== ATTRIBUTS UI ====================

    /**
     * ViewModel gérant la logique d'authentification
     */
    private LogingViewModel mViewModel;

    /**
     * Champ de saisie pour le nom de l'utilisateur
     */
    private EditText etUsername;

    /**
     * Champ de saisie pour le numéro de téléphone
     */
    private EditText etPhone;

    /**
     * Champ de saisie pour le matricule (identifiant unique)
     */
    private EditText etMatricule;

    /**
     * Bouton pour déclencher la connexion/inscription
     */
    private Button btnLogin;

    /**
     * Indicateur de progression visuel pendant le traitement
     */
    private ProgressBar progressBar;

    // ==================== CONSTANTES SHAREDPREFERENCES ====================

    /**
     * Nom du fichier SharedPreferences pour stocker l'état de session
     */
    private static final String PREFS_NAME = "UserPrefs";

    /**
     * Clé pour stocker l'état de connexion (true/false)
     */
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    /**
     * Clé pour stocker l'ID de l'utilisateur connecté
     */
    private static final String KEY_USER_ID = "userId";

    /**
     * Clé pour stocker le nom de l'utilisateur
     */
    private static final String KEY_USER_NAME = "userName";

    /**
     * Clé pour stocker le matricule de l'utilisateur
     */
    private static final String KEY_USER_MATRICULE = "userMatricule";

    // ==================== MÉTHODES DE CYCLE DE VIE ====================

    /**
     * Factory method pour créer une nouvelle instance du fragment.
     *
     * @return Nouvelle instance de LoginUserFragment
     */
    public static LoginUserFragment newInstance() {
        return new LoginUserFragment();
    }

    /**
     * Appelé lors de la création du fragment.
     * <p>
     * Initialise le ViewModel et configure les observateurs LiveData.
     * Les observateurs sont configurés ici (dans onCreate) plutôt que dans onViewCreated
     * pour garantir qu'ils sont enregistrés avant toute interaction utilisateur.
     *
     * @param savedInstanceState État sauvegardé précédent
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ========== INITIALISATION DU VIEWMODEL ==========
        mViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(LogingViewModel.class);

        // ========== OBSERVATEUR 1: RÉSULTAT DE CONNEXION ==========

        /**
         * Observer le résultat de la tentative de connexion.
         * <p>
         * Réactions:
         * - Affiche un message Toast avec le résultat
         * - Si succès: sauvegarde la session et navigue vers l'accueil
         * - Si échec: reste sur l'écran de connexion
         */
        mViewModel.getLoginResult().observe(this, result -> {
            if (result != null) {
                // Affichage du message (succès ou erreur)
                Toast.makeText(requireContext(), result.getMessage(), Toast.LENGTH_SHORT).show();

                if (result.isSuccess()) {
                    // ✅ CONNEXION RÉUSSIE

                    // Sauvegarde de l'état de connexion et des informations utilisateur
                    saveLoginState(
                            true,                    // Utilisateur connecté
                            result.getUserId(),      // ID de l'utilisateur
                            result.getUserName(),    // Nom
                            result.getMatricule()    // Matricule
                    );

                    // Navigation vers l'écran d'accueil
                    navigateToHome();
                }
                // En cas d'échec, l'utilisateur reste sur l'écran de connexion
                // Le message d'erreur est déjà affiché via Toast
            }
        });

        // ========== OBSERVATEUR 2: ÉTAT DE CHARGEMENT ==========

        /**
         * Observer l'état de chargement pour mettre à jour l'UI.
         * <p>
         * Actions:
         * - Affiche/masque la ProgressBar
         * - Active/désactive le bouton de connexion
         * - Empêche les doubles soumissions
         */
        mViewModel.getIsLoading().observe(this, isLoading -> {
            if (progressBar != null) {
                // Affichage conditionnel de la ProgressBar
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
            if (btnLogin != null) {
                // Désactivation du bouton pendant le chargement
                // Empêche les clics multiples
                btnLogin.setEnabled(!isLoading);
            }
        });
    }

    /**
     * Crée et retourne la hiérarchie de vues du fragment.
     *
     * Initialise toutes les références aux vues et configure les écouteurs d'événements.
     *
     * @param inflater Objet pour gonfler les vues
     * @param container Vue parent
     * @param savedInstanceState État sauvegardé
     * @return Vue racine du fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflation du layout
        View view = inflater.inflate(R.layout.fragment_login_user, container, false);

        // ========== INITIALISATION DES VUES ==========
        etUsername = view.findViewById(R.id.editName);
        etPhone = view.findViewById(R.id.editPhone);
        etMatricule = view.findViewById(R.id.editMatricule);
        btnLogin = view.findViewById(R.id.buttonLogin);
        progressBar = view.findViewById(R.id.progressBar);

        // ========== CONFIGURATION DES ÉCOUTEURS ==========

        // Clic sur le bouton de connexion
        btnLogin.setOnClickListener(v -> handleLogin());

        return view;
    }

    // ==================== GESTION DE LA CONNEXION ====================

    /**
     * Gère le processus de connexion/inscription.
     *
     * Processus:
     * 1. Récupération et nettoyage des valeurs saisies
     * 2. Validation des champs
     * 3. Délégation au ViewModel pour traitement
     *
     * Validation effectuée:
     * - Tous les champs doivent être remplis
     * - Affichage d'erreurs sur les champs invalides
     * - Focus automatique sur le premier champ en erreur
     */
    private void handleLogin() {
        // Récupération et nettoyage des valeurs
        String username = etUsername.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String matricule = etMatricule.getText().toString().trim();

        // ========== VALIDATION DU FORMULAIRE ==========

        // Validation du nom
        if (username.isEmpty()) {
            etUsername.setError("Le nom est requis");
            etUsername.requestFocus(); // Focus sur le champ en erreur
            return; // Arrêt de l'exécution
        }

        // Validation du téléphone
        if (phone.isEmpty()) {
            etPhone.setError("Le téléphone est requis");
            etPhone.requestFocus();
            return;
        }

        // Validation du matricule
        if (matricule.isEmpty()) {
            etMatricule.setError("Le matricule est requis");
            etMatricule.requestFocus();
            return;
        }

        // ========== APPEL AU VIEWMODEL ==========

        // Tous les champs sont valides, délégation au ViewModel
        // Le ViewModel gère la logique métier et notifiera via LiveData
        mViewModel.login(username, phone, matricule);
    }

    // ==================== GESTION DE LA SESSION ====================

    /**
     * Sauvegarde l'état de connexion dans SharedPreferences.
     *
     * Cette méthode persiste les informations de session pour:
     * - Maintenir l'utilisateur connecté entre les redémarrages de l'app
     * - Personnaliser l'expérience utilisateur
     * - Pré-remplir les formulaires avec les infos de l'utilisateur
     *
     * Les données sauvegardées:
     * - isLoggedIn: Booléen indiquant si l'utilisateur est connecté
     * - userId: ID unique de l'utilisateur dans la base de données
     * - userName: Nom de l'utilisateur (pour affichage)
     * - userMatricule: Matricule (identifiant unique)
     *
     * Utilise apply() pour une sauvegarde asynchrone (plus performant).
     *
     * @param isLoggedIn État de connexion (true = connecté)
     * @param userId ID de l'utilisateur dans la base de données
     * @param userName Nom de l'utilisateur
     * @param matricule Matricule de l'utilisateur
     */
    private void saveLoginState(boolean isLoggedIn, int userId, String userName, String matricule) {
        // Récupération des SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Sauvegarde des données
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, userName);
        editor.putString(KEY_USER_MATRICULE, matricule);

        // Application asynchrone (ne bloque pas le thread principal)
        editor.apply();
    }

    // ==================== NAVIGATION ====================

    /**
     * Navigue vers l'écran d'accueil utilisateur.
     *
     * Utilise une transaction de fragment pour:
     * - Remplacer le fragment actuel par HomeUserFragment
     * - Ne pas ajouter à la pile de retour (commit sans addToBackStack)
     *   Car l'utilisateur ne doit pas pouvoir revenir à l'écran de connexion
     *
     * Cette approche garantit que:
     * - L'utilisateur est redirigé vers l'écran principal
     * - Le bouton retour ne ramène pas à l'écran de connexion
     * - La navigation est fluide et cohérente
     */
    private void navigateToHome() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_user_container, HomeUserFragment.newInstance())
                    .commit(); // Pas de addToBackStack - l'utilisateur ne peut pas revenir
        }
    }

}
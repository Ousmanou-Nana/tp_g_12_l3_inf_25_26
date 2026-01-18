package com.example.tp_g_12_l3_inf_25_26.ui.user.declareobjectfrom;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tp_g_12_l3_inf_25_26.R;
import com.example.tp_g_12_l3_inf_25_26.ui.user.home.HomeUserFragment;

/**
 * Fragment permettant aux utilisateurs de déclarer des objets perdus.

 * Ce fragment gère l'interface utilisateur pour la déclaration d'objets perdus,
 * incluant la saisie d'informations personnelles, la description de l'objet,
 * le type d'objet et l'ajout de photos.

 * Fonctionnalités principales:
 * - Formulaire de déclaration avec validation
 * - Sélection multiple d'images
 * - Mise en cache des informations utilisateur
 * - Communication avec le ViewModel via LiveData
 *
 * @author Votre équipe
 * @version 1.0
 */
public class UserDeclareObjectFrom extends Fragment {

    // ==================== ATTRIBUTS ====================

    /**
     * ViewModel associé au fragment, gère la logique métier et les données
     */
    private UserDeclareObjectFromViewModel mViewModel;

    /**
     * Champ de saisie pour le nom de l'utilisateur
     */
    private EditText editName;

    /**
     * Champ de saisie pour le numéro de téléphone de l'utilisateur
     */
    private EditText editPhone;

    /**
     * Champ de saisie pour le matricule de l'utilisateur
     */
    private EditText editMatricule;

    /**
     * Champ de saisie pour la description de l'objet perdu
     */
    private EditText editDescription;

    /**
     * Liste déroulante pour sélectionner le type d'objet
     */
    private Spinner spinnerObjectType;

    /**
     * Conteneur linéaire pour afficher les images sélectionnées
     */
    private LinearLayout containerImages;

    /**
     * Bouton pour soumettre la déclaration
     */
    private Button buttonSubmit;

    /**
     * Bouton pour effacer le cache utilisateur
     */
    private Button buttonClearCache;

    /**
     * Lanceur d'activité pour la sélection d'images depuis la galerie
     * Utilise le contrat OpenDocument pour obtenir des URIs persistants
     */
    private ActivityResultLauncher<String[]> pickImagesLauncher;

    // ==================== MÉTHODES DE CYCLE DE VIE ====================

    /**
     * Crée une nouvelle instance du fragment.
     * Pattern Factory pour instanciation.
     *
     * @return Nouvelle instance de UserDeclareObjectFrom
     */
    public static UserDeclareObjectFrom newInstance() {
        return new UserDeclareObjectFrom();
    }

    /**
     * Appelé lors de la création du fragment.
     * Initialise le ViewModel avec le scope du fragment.
     *
     * @param savedInstanceState État sauvegardé précédent (si disponible)
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialisation du ViewModel avec AndroidViewModelFactory
        // pour passer le contexte Application au ViewModel
        mViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(UserDeclareObjectFromViewModel.class);
    }

    /**
     * Crée et retourne la hiérarchie de vues associée au fragment.
     *
     * @param inflater Objet pour gonfler les vues dans le fragment
     * @param container Vue parent à laquelle l'UI du fragment sera attachée
     * @param savedInstanceState État sauvegardé précédent
     * @return Vue racine du fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_declare_object_from, container, false);
    }

    /**
     * Appelé immédiatement après onCreateView().
     * Configure tous les composants de l'interface utilisateur.
     *
     * @param view Vue retournée par onCreateView()
     * @param savedInstanceState État sauvegardé précédent
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Séquence d'initialisation complète
        initViews(view);           // Liaison des vues
        setupSpinner();            // Configuration du Spinner des types
        setupImagePicker();        // Configuration du sélecteur d'images
        setupButtons();            // Configuration des boutons
        observeViewModel();        // Observation des LiveData
    }

    // ==================== INITIALISATION DES VUES ====================

    /**
     * Initialise toutes les références aux vues de l'interface.
     * Utilise findViewById pour lier les vues du layout XML.
     *
     * @param view Vue racine contenant tous les composants
     */
    private void initViews(View view) {
        editName = view.findViewById(R.id.editName);
        editPhone = view.findViewById(R.id.editPhone);
        editMatricule = view.findViewById(R.id.editMatricule);
        editDescription = view.findViewById(R.id.editDescription);
        spinnerObjectType = view.findViewById(R.id.spinnerObjectType);
        containerImages = view.findViewById(R.id.containerImages);
        buttonSubmit = view.findViewById(R.id.buttonSubmit);
        buttonClearCache = view.findViewById(R.id.buttonClearCache);
    }

    /**
     * Configure le Spinner avec les types d'objets disponibles.
     * Les types sont récupérés depuis le ViewModel qui les charge depuis la BD.
     */
    private void setupSpinner() {
        // Création d'un adaptateur avec layout Android standard
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, mViewModel.getObjectTypes());

        // Définition du layout pour les items déroulants
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Application de l'adaptateur au Spinner
        spinnerObjectType.setAdapter(adapter);
    }

    /**
     * Configure le sélecteur d'images et son lanceur d'activité.

     * Utilise OpenDocument pour obtenir des permissions persistantes sur les URIs,
     * permettant d'accéder aux images même après le redémarrage de l'application.

     * Important: takePersistableUriPermission est crucial pour conserver l'accès
     * aux images sélectionnées après la fermeture du sélecteur.
     */
    private void setupImagePicker() {
        // Enregistrement du lanceur pour la sélection d'images
        pickImagesLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(), // Contrat pour sélection de documents
                uri -> {
                    if (uri != null) {
                        // IMPORTANT: Demande de permission persistante
                        // Sans cela, l'URI ne sera plus accessible après
                        requireActivity().getContentResolver().takePersistableUriPermission(
                                uri,
                                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );

                        // Ajout de l'image sélectionnée à l'interface
                        addImage(uri);
                    }
                }
        );

        // Ajout du premier slot d'image vide (bouton +)
        addImageView();
    }

    /**
     * Configure les écouteurs de clics pour les boutons.

     * - buttonSubmit: Lance la validation et soumission du formulaire
     * - buttonClearCache: Efface les informations utilisateur en cache
     */
    private void setupButtons() {
        // Bouton de soumission
        buttonSubmit.setOnClickListener(v -> submitForm());

        // Bouton d'effacement du cache (optionnel)
        if (buttonClearCache != null) {
            buttonClearCache.setOnClickListener(v -> {
                mViewModel.clearUserCache();
                clearFormFields();
                Toast.makeText(requireContext(), "Cache effacé", Toast.LENGTH_SHORT).show();
            });
        }
    }

    /**
     * Configure les observateurs LiveData pour réagir aux changements de données.

     * Observe trois LiveData:
     * 1. UserInfo - Pour pré-remplir les champs utilisateur depuis le cache
     * 2. SelectedImages - Pour mettre à jour l'affichage des images
     * 3. SubmitResult - Pour afficher le résultat de la soumission
     */
    private void observeViewModel() {
        // Observer 1: Informations utilisateur depuis le cache
        mViewModel.getUserInfoLiveData().observe(getViewLifecycleOwner(), userInfo -> {
            if (userInfo != null && !userInfo.isEmpty()) {
                // Pré-remplissage automatique des champs
                editName.setText(userInfo.getName());
                editPhone.setText(userInfo.getPhone());
                editMatricule.setText(userInfo.getMatricule());
            }
        });

        // Observer 2: Images sélectionnées (optionnel - pour mise à jour UI)
        mViewModel.getSelectedImagesLiveData().observe(getViewLifecycleOwner(), images -> {
            // Peut être utilisé pour mettre à jour un compteur d'images, etc.
        });

        // Observer 3: Résultat de la soumission
        mViewModel.getSubmitResultLiveData().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                // Affichage du message de résultat (succès ou erreur)
                Toast.makeText(requireContext(), result.getMessage(), Toast.LENGTH_LONG).show();

                if (result.isSuccess()) {
                    // En cas de succès: réinitialisation du formulaire
                    clearFormFields();
                    clearImageViews();

                    // Navigation possible vers la liste des objets perdus
                    // navigateToLostList();
                }
            }
        });
    }

    // ==================== GESTION DU FORMULAIRE ====================

    /**
     * Valide et soumet le formulaire de déclaration.

     * Processus:
     * 1. Récupération et nettoyage des valeurs des champs
     * 2. Validation via le ViewModel
     * 3. Soumission si valide, sinon affichage d'erreur
     */
    private void submitForm() {
        // Récupération et nettoyage des valeurs
        String name = editName.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String matricule = editMatricule.getText().toString().trim();
        String description = editDescription.getText().toString().trim();
        String type = spinnerObjectType.getSelectedItem().toString();

        // Validation du formulaire
        if (!mViewModel.isFormValid(name, phone, matricule, description, type)) {
            Toast.makeText(requireContext(),
                    "Veuillez remplir tous les champs et ajouter au moins une image",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Soumission de la déclaration (exécuté en arrière-plan par le ViewModel)
        mViewModel.submitDeclaration(name, phone, matricule, description, type);
        BackHome();

    }

    /**
     * Réinitialise uniquement les champs spécifiques à la déclaration.
     * Les informations utilisateur (nom, téléphone, matricule) sont préservées
     * car elles sont gérées par le cache.
     */
    private void clearFormFields() {
        editDescription.setText("");
        spinnerObjectType.setSelection(0);
    }

    // ==================== GESTION DES IMAGES ====================

    /**
     * Ajoute un nouveau slot d'image vide (avec icône +).

     * Structure du slot:
     * - ImageView pour afficher l'image
     * - ImageView avec icône + pour déclencher la sélection

     * Le slot initial affiche uniquement l'icône +.
     */
    private void addImageView() {
        // Inflation du layout d'item d'image
        View item = getLayoutInflater().inflate(R.layout.image_item, containerImages, false);
        ImageView plusIcon = item.findViewById(R.id.plusIcon);
        ImageView image = item.findViewById(R.id.image);

        // Configuration initiale: icône + visible, pas d'image
        plusIcon.setVisibility(View.VISIBLE);
        image.setImageDrawable(null);

        // Clic sur + déclenche la sélection d'image
        plusIcon.setOnClickListener(v -> pickImagesLauncher.launch(new String[]{"image/*"}));

        // Ajout du slot au conteneur
        containerImages.addView(item);
    }

    /**
     * Remplit le dernier slot vide avec l'image sélectionnée et crée un nouveau slot.

     * Processus:
     * 1. Récupération du dernier slot (celui avec l'icône +)
     * 2. Affichage de l'image dans ce slot
     * 3. Masquage de l'icône +
     * 4. Sauvegarde de l'URI dans le ViewModel
     * 5. Création d'un nouveau slot vide pour la prochaine image
     *
     * @param uri URI de l'image sélectionnée (avec permissions persistantes)
     */
    private void addImage(Uri uri) {
        // Récupération du dernier slot ajouté
        View lastItem = containerImages.getChildAt(containerImages.getChildCount() - 1);
        ImageView image = lastItem.findViewById(R.id.image);
        ImageView plusIcon = lastItem.findViewById(R.id.plusIcon);

        // Affichage de l'image et masquage de l'icône +
        image.setImageURI(uri);
        plusIcon.setVisibility(View.GONE);

        // Sauvegarde de l'URI dans le ViewModel
        mViewModel.addImage(uri);

        // Création d'un nouveau slot vide pour permettre l'ajout d'autres images
        addImageView();
    }

    /**
     * Supprime toutes les vues d'images et réinitialise le système.

     * Utilisé après une soumission réussie pour préparer
     * une nouvelle déclaration.
     */
    private void clearImageViews() {
        containerImages.removeAllViews();  // Suppression de toutes les vues enfants
        mViewModel.clearImages();           // Nettoyage de la liste dans le ViewModel
        addImageView();                     // Ajout d'un nouveau slot vide initial
    }

    private void BackHome() {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.main_user_container, HomeUserFragment.newInstance())
                .addToBackStack(null)
                .commit();
    }
}
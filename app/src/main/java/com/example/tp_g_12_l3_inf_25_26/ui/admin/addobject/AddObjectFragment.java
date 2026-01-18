// ============================================================================
// AddObjectFragment.java
// ============================================================================
// Fragment qui gère l'interface admin pour ajouter un objet trouvé
// Permet de:
// - Saisir les informations de l'objet (nom, téléphone, description(de l'objet))
// - Sélectionner le type d'objet
// - Prendre jusqu'à 5 photos
// - Visualiser et supprimer les photos
// - Soumettre la objet
// ============================================================================

package com.example.tp_g_12_l3_inf_25_26.ui.admin.addobject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.tp_g_12_l3_inf_25_26.R;

public class AddObjectFragment extends Fragment {

    // ===== CONSTANTES =====
    // Code de requête pour identifier le retour de l'appareil photo
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    // ===== VIEWMODEL =====
    private AddObjectViewModel viewModel;

    // ===== RÉFÉRENCES AUX VUES =====
    // Champs de saisie
    private EditText editName;           // Nom de l'objet
    private EditText editPhone;          // Numéro de téléphone
    private EditText editDescription;    // Description détaillée

    // Sélection du type
    private Spinner spinnerObjectType;   // Liste déroulante des types

    // Boutons d'action
    private Button buttonTakePhoto;      // Ouvrir l'appareil photo
    private Button buttonSubmit;         // Soumettre le formulaire
    private Button buttonClear;          // Réinitialiser le formulaire

    // Affichage
    private TextView textImageCount;     // Compteur d'images (ex: "3/5")

    // Conteneur pour afficher les miniatures des images
    private LinearLayout imagesContainer;

    /**
     * Méthode factory pour créer une instance du fragment
     */
    public static AddObjectFragment newInstance() {
        return new AddObjectFragment();
    }

    /**
     * Crée la vue du fragment en inflatant le layout XML
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Inflate le layout qui définit l'interface
        return inflater.inflate(R.layout.fragment_add_object, container, false);
    }

    /**
     * Appelé après la création de la vue
     * Configure tous les composants de l'interface
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // ===== INITIALISATION DU VIEWMODEL =====
        viewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(AddObjectViewModel.class);

        // ===== CONFIGURATION DE L'INTERFACE =====
        initViews(view);           // Récupère les références aux vues
        setupSpinner();            // Configure la liste déroulante des types
        setupListeners();          // Configure les écouteurs de clics
        observeViewModel();        // Configure les observateurs de LiveData
    }

    // ===== INITIALISATION DES VUES =====

    /**
     * Récupère les références à tous les composants de l'interface
     * Utilise findViewById pour lier les vues XML au code Java
     */
    private void initViews(View view) {
        editName = view.findViewById(R.id.editName);
        editPhone = view.findViewById(R.id.editPhone);
        editDescription = view.findViewById(R.id.editDescription);
        spinnerObjectType = view.findViewById(R.id.spinnerObjectType);
        buttonTakePhoto = view.findViewById(R.id.buttonTakePhoto);
        buttonSubmit = view.findViewById(R.id.buttonSubmit);
        buttonClear = view.findViewById(R.id.buttonClear);
        textImageCount = view.findViewById(R.id.textImageCount);
        imagesContainer = view.findViewById(R.id.imagesContainer);
    }

    // ===== CONFIGURATION DU SPINNER (LISTE DÉROULANTE) =====

    /**
     * Configure le Spinner avec les types d'objets récupérés de la base
     * Crée un adaptateur qui lie les données aux vues du Spinner
     */
    private void setupSpinner() {
        // Crée un adaptateur avec les types d'objets
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,  // Layout par défaut Android
                        viewModel.getObjectTypes()              // Données du ViewModel
                );

        // Définit le layout pour les éléments déroulants
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        // Associe l'adaptateur au Spinner
        spinnerObjectType.setAdapter(adapter);
    }

    // ===== CONFIGURATION DES ÉCOUTEURS DE CLICS =====

    /**
     * Configure les actions à effectuer lors des clics sur les boutons
     */
    private void setupListeners() {

        // ===== BOUTON "PRENDRE UNE PHOTO" =====
        buttonTakePhoto.setOnClickListener(v -> {
            // Vérifie qu'on peut encore ajouter des images (< 5)
            if (viewModel.canAddMoreImages()) {
                // Crée un Intent pour ouvrir l'appareil photo
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                // Vérifie qu'une app caméra est disponible
                if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                    // Lance l'activité caméra et attend le résultat
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                }
            } else {
                // Maximum d'images atteint
                Toast.makeText(requireContext(),
                        "Maximum 5 images autorisées",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // ===== BOUTON "SOUMETTRE" =====
        buttonSubmit.setOnClickListener(v -> {
            // Récupère les valeurs saisies
            String name = editName.getText().toString().trim();
            String phone = editPhone.getText().toString().trim();
            String description = editDescription.getText().toString().trim();
            String type = spinnerObjectType.getSelectedItem().toString();

            // Demande au ViewModel de valider le formulaire
            // Si valide, le ViewModel déclenchera automatiquement la soumission
            viewModel.validateForm(name, phone, description, type);
        });

        // ===== BOUTON "RÉINITIALISER" =====
        buttonClear.setOnClickListener(v -> {
            clearForm(); // Vide tous les champs
        });
    }

    // ===== OBSERVATION DU VIEWMODEL =====

    /**
     * Configure les observateurs pour réagir aux changements dans le ViewModel
     * Pattern Observer: le Fragment est notifié automatiquement des changements
     */
    private void observeViewModel() {

        // ===== OBSERVER LA VALIDATION DU FORMULAIRE =====
        viewModel.isFormValid().observe(
                getViewLifecycleOwner(),
                valid -> {
                    if (!valid) {
                        // Formulaire invalide: affiche un message d'erreur
                        Toast.makeText(
                                requireContext(),
                                "Veuillez remplir tous les champs et ajouter au moins une image",
                                Toast.LENGTH_SHORT
                        ).show();
                        return; // Arrête ici, ne soumet pas
                    }

                    // Formulaire valide: soumet la déclaration
                    viewModel.submitDeclaration(
                            editName.getText().toString().trim(),
                            editPhone.getText().toString().trim(),
                            editDescription.getText().toString().trim(),
                            spinnerObjectType.getSelectedItem().toString()
                    );
                }
        );

        // ===== OBSERVER LES IMAGES =====
        // S'exécute chaque fois qu'une image est ajoutée ou supprimée
        viewModel.getImages().observe(
                getViewLifecycleOwner(),
                images -> {
                    // Met à jour l'affichage des miniatures
                    updateImagesDisplay(images);

                    // Met à jour le compteur "3/5"
                    updateImageCount();
                }
        );

        // ===== OBSERVER LE RÉSULTAT DE SOUMISSION =====
        viewModel.getSubmitResult().observe(
                getViewLifecycleOwner(),
                result -> {
                    if (result != null) {
                        // Affiche le message de résultat (succès ou erreur)
                        Toast.makeText(
                                requireContext(),
                                result.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();

                        if (result.isSuccess()) {
                            // Si succès, réinitialise le formulaire
                            clearForm();
                        }
                    }
                }
        );
    }

    // ===== MISE À JOUR DE L'AFFICHAGE DES IMAGES =====

    /**
     * Affiche toutes les images sous forme de miniatures avec bouton de suppression
     * Reconstruit complètement l'interface à chaque changement
     *
     * @param images Liste des images à afficher
     */
    private void updateImagesDisplay(java.util.List<Bitmap> images) {
        // Vide le conteneur d'images
        imagesContainer.removeAllViews();

        // Crée une vue pour chaque image
        for (int i = 0; i < images.size(); i++) {
            final int position = i;  // Capture l'index pour le listener
            Bitmap bitmap = images.get(i);

            // ===== CRÉATION DU CONTENEUR POUR UNE IMAGE =====
            LinearLayout imageLayout = new LinearLayout(requireContext());
            imageLayout.setOrientation(LinearLayout.VERTICAL);
            imageLayout.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    )
            );
            imageLayout.setPadding(0, 8, 0, 8);  // Espacement vertical

            // ===== CRÉATION DE L'IMAGEVIEW =====
            ImageView imageView = new ImageView(requireContext());
            imageView.setImageBitmap(bitmap);  // Affiche l'image
            imageView.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            300  // Hauteur fixe de 300px
                    )
            );
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);  // Recadrage

            // ===== CRÉATION DU BOUTON SUPPRIMER =====
            Button btnRemove = new Button(requireContext());
            btnRemove.setText("Supprimer cette image");
            btnRemove.setOnClickListener(v -> {
                // Supprime l'image à cette position
                viewModel.removeImage(position);
            });

            // ===== ASSEMBLAGE =====
            // Ajoute l'image et le bouton au conteneur
            imageLayout.addView(imageView);
            imageLayout.addView(btnRemove);

            // Ajoute le conteneur au layout principal
            imagesContainer.addView(imageLayout);
        }
    }

    // ===== MISE À JOUR DU COMPTEUR D'IMAGES =====

    /**
     * Met à jour le texte du compteur et l'état du bouton photo
     * Désactive le bouton si 5 images sont déjà ajoutées
     */
    private void updateImageCount() {
        int count = viewModel.getImageCount();

        // Affiche "Images: 3/5" par exemple
        textImageCount.setText("Images: " + count + "/5");

        // Active/désactive le bouton selon la limite
        buttonTakePhoto.setEnabled(viewModel.canAddMoreImages());
    }

    // ===== RÉINITIALISATION DU FORMULAIRE =====

    /**
     * Vide tous les champs du formulaire et supprime toutes les images
     */
    private void clearForm() {
        editName.setText("");
        editPhone.setText("");
        editDescription.setText("");
        spinnerObjectType.setSelection(0);  // Sélectionne le premier élément
        viewModel.clearImages();  // Vide les images dans le ViewModel

        Toast.makeText(requireContext(),
                "Formulaire réinitialisé",
                Toast.LENGTH_SHORT).show();
    }

    // ===== RÉCEPTION DU RÉSULTAT DE L'APPAREIL PHOTO =====

    /**
     * Appelée automatiquement quand l'utilisateur revient de l'appareil photo
     * Récupère la photo prise et l'ajoute à la liste
     *
     * @param requestCode Code pour identifier quelle activité a été lancée
     * @param resultCode Indique si l'action a réussi (RESULT_OK) ou été annulée
     * @param data Intent contenant les données retournées (la photo)
     */
    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        // Vérifie que c'est bien le retour de la caméra
        if (requestCode == REQUEST_IMAGE_CAPTURE
                && resultCode == Activity.RESULT_OK  // Photo prise avec succès
                && data != null) {  // Des données sont retournées

            // Récupère les extras de l'Intent
            Bundle extras = data.getExtras();

            if (extras != null) {
                // Récupère la miniature de la photo (pas la photo complète)
                // Note: Pour avoir la photo complète, il faudrait utiliser un URI
                Bitmap bitmap = (Bitmap) extras.get("data");

                if (bitmap != null) {
                    // Ajoute la photo au ViewModel
                    viewModel.addImage(bitmap);
                }
            }
        }
    }
}
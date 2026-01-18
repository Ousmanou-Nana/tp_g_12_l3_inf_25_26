// ============================================================================
// AddObjectViewModel.java
// ============================================================================
// ViewModel qui gère la logique métier pour l'ajout d'objets trouvés
// Permet de déclarer un objet trouvé avec plusieurs photos et informations
// ============================================================================

package com.example.tp_g_12_l3_inf_25_26.ui.admin.addobject;

import android.app.Application;
import android.database.Cursor;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.tp_g_12_l3_inf_25_26.DB.DatabaseHelper;
import com.example.tp_g_12_l3_inf_25_26.ui.admin.loging.LoginAdminFragment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddObjectViewModel extends AndroidViewModel {

    // ===== CONSTANTES =====
    // Limite maximale d'images qu'on peut ajouter par objet
    private static final int MAX_IMAGES = 5;

    // Helper pour accéder à la base de données
    private final DatabaseHelper databaseHelper;

    // ===== LIVEDATA - DONNÉES OBSERVABLES =====

    /**
     * Liste des images (Bitmap) sélectionnées par l'utilisateur
     * Initialisée avec une ArrayList vide
     */
    private final MutableLiveData<List<Bitmap>> images =
            new MutableLiveData<>(new ArrayList<>());

    /**
     * Indique si le formulaire est valide (tous les champs remplis)
     * Utilisé pour activer/désactiver le bouton de soumission
     */
    private final MutableLiveData<Boolean> formValid =
            new MutableLiveData<>(false);

    /**
     * Résultat de la soumission du formulaire (succès ou erreur)
     * Contient un message à afficher à l'utilisateur
     */
    private final MutableLiveData<SubmitResult> submitResult =
            new MutableLiveData<>();

    /**
     * Constructeur du ViewModel
     * @param application Contexte de l'application
     */
    public AddObjectViewModel(@NonNull Application application) {
        super(application);
        databaseHelper = new DatabaseHelper(application);
    }

    // ===== MÉTHODES PUBLIQUES - GETTERS =====

    /**
     * Récupère tous les types d'objets disponibles depuis la base de données
     * Utilisé pour remplir le Spinner (liste déroulante) de sélection de type
     *
     * @return Tableau de String contenant les noms des types d'objets
     *         Exemple: ["Téléphone", "Portefeuille", "Clés", "Sac à dos"]
     */
    public String[] getObjectTypes() {
        List<String> typesList = new ArrayList<>();

        // Récupère tous les types depuis la base de données
        Cursor cursor = databaseHelper.getAllTypes();

        if (cursor != null) {
            // Parcourt tous les résultats
            while (cursor.moveToNext()) {
                // Récupère l'index de la colonne "nom_type"
                int nameIndex = cursor.getColumnIndex("nom_type");

                if (nameIndex != -1) {
                    // Ajoute le nom du type à la liste
                    typesList.add(cursor.getString(nameIndex));
                }
            }
            cursor.close(); // Libère les ressources
        }

        // Convertit la List en tableau de String
        return typesList.toArray(new String[0]);
    }

    /**
     * Retourne le LiveData des images pour observation
     * Le Fragment peut observer ce LiveData pour être notifié des changements
     */
    public LiveData<List<Bitmap>> getImages() {
        return images;
    }

    /**
     * Retourne le LiveData de validation du formulaire
     */
    public LiveData<Boolean> isFormValid() {
        return formValid;
    }

    /**
     * Retourne le LiveData du résultat de soumission
     */
    public LiveData<SubmitResult> getSubmitResult() {
        return submitResult;
    }

    // ===== GESTION DES IMAGES =====

    /**
     * Ajoute une nouvelle image à la liste
     * Vérifie qu'on ne dépasse pas la limite de MAX_IMAGES (5)
     *
     * @param bitmap Image à ajouter (capturée par la caméra)
     */
    public void addImage(Bitmap bitmap) {
        // Récupère la liste actuelle d'images
        List<Bitmap> current = images.getValue();

        if (current != null) {
            // Vérifie qu'on n'a pas atteint la limite
            if (current.size() < MAX_IMAGES) {
                current.add(bitmap);
                // Met à jour le LiveData pour notifier les observateurs
                images.setValue(current);
            }
        }
    }

    /**
     * Supprime une image de la liste à une position donnée
     *
     * @param position Index de l'image à supprimer (0-4)
     */
    public void removeImage(int position) {
        List<Bitmap> current = images.getValue();

        // Vérifie que la position est valide
        if (current != null && position >= 0 && position < current.size()) {
            current.remove(position);
            // Met à jour le LiveData
            images.setValue(current);
        }
    }

    /**
     * Retourne le nombre d'images actuellement dans la liste
     *
     * @return Nombre d'images (0-5)
     */
    public int getImageCount() {
        List<Bitmap> current = images.getValue();
        return current != null ? current.size() : 0;
    }

    /**
     * Vérifie si on peut encore ajouter des images
     *
     * @return true si on peut ajouter (< 5 images), false sinon
     */
    public boolean canAddMoreImages() {
        return getImageCount() < MAX_IMAGES;
    }

    // ===== VALIDATION DU FORMULAIRE =====

    /**
     * Valide le formulaire en vérifiant que tous les champs requis sont remplis
     * Met à jour le LiveData formValid qui sera observé par le Fragment
     *
     * @param name Nom de l'objet trouvé
     * @param phone Numéro de téléphone de contact
     * @param description Description détaillée de l'objet
     * @param type Type de l'objet sélectionné
     */
    public void validateForm(String name,
                             String phone,
                             String description,
                             String type) {

        // Le formulaire est valide si:
        // - Le nom n'est pas vide
        // - Le téléphone n'est pas vide
        // - La description n'est pas vide
        // - Un type est sélectionné
        // - Au moins une image a été ajoutée
        boolean valid =
                !name.isEmpty()
                        && !phone.isEmpty()
                        && !description.isEmpty()
                        && !type.isEmpty()
                        && images.getValue() != null
                        && !images.getValue().isEmpty();

        // Met à jour le LiveData
        formValid.setValue(valid);
    }

    // ===== SOUMISSION DU FORMULAIRE =====

    /**
     * Soumet la déclaration d'objet trouvé
     * Processus:
     * 1. Vérifie que l'admin est connecté
     * 2. Récupère l'ID du type d'objet
     * 3. Insère l'objet dans la base de données
     * 4. Sauvegarde les images sur le disque
     * 5. Enregistre les chemins des images dans la base
     * 6. Notifie le résultat via LiveData
     *
     * Cette opération se fait dans un thread séparé pour ne pas bloquer l'UI
     *
     * @param name Nom de l'objet
     * @param phone Téléphone de contact
     * @param description Description de l'objet
     * @param type Type de l'objet
     */
    public void submitDeclaration(String name,
                                  String phone,
                                  String description,
                                  String type) {

        // Exécute dans un thread séparé (opération longue)
        new Thread(() -> {
            try {
                // ===== ÉTAPE 1: VÉRIFICATION DE L'ADMIN =====
                // Récupère l'ID de l'admin actuellement connecté
                int adminId = LoginAdminFragment.getLoggedInAdminId(getApplication());

                if (adminId == -1) {
                    // Admin non connecté, impossible de continuer
                    submitResult.postValue(new SubmitResult(false, "Admin non connecté"));
                    return;
                }

                // ===== ÉTAPE 2: RÉCUPÉRATION DE L'ID DU TYPE =====
                // Convertit le nom du type en ID (clé étrangère)
                int typeId = databaseHelper.getTypeIdByName(type);

                if (typeId == -1) {
                    // Type invalide ou inexistant
                    submitResult.postValue(new SubmitResult(false, "Type d'objet invalide"));
                    return;
                }

                // ===== ÉTAPE 3: GÉNÉRATION DE LA DATE ACTUELLE =====
                // Format: "2026-01-18 14:30:45"
                String currentDate = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault()
                ).format(new Date());

                // ===== ÉTAPE 4: INSERTION DE L'OBJET EN BASE =====
                // Insère l'objet et récupère son ID auto-généré
                long objetId = databaseHelper.insertObjet(
                        name,           // Nom de l'objet
                        phone,          // Téléphone
                        description,    // Description
                        typeId,         // ID du type
                        currentDate,    // Date de déclaration
                        adminId         // ID de l'admin qui déclare
                );

                if (objetId == -1) {
                    // Erreur lors de l'insertion
                    submitResult.postValue(new SubmitResult(false, "Erreur lors de l'enregistrement"));
                    return;
                }

                // ===== ÉTAPE 5: SAUVEGARDE DES IMAGES =====
                List<Bitmap> currentImages = images.getValue();

                if (currentImages != null && !currentImages.isEmpty()) {
                    // Sauvegarde toutes les images sur le disque
                    boolean allImagesSaved = saveImages((int) objetId, currentImages);

                    if (!allImagesSaved) {
                        // Au moins une image n'a pas pu être sauvegardée
                        submitResult.postValue(new SubmitResult(false, "Erreur lors de la sauvegarde des images"));
                        return;
                    }
                }

                // ===== ÉTAPE 6: RÉINITIALISATION ET SUCCÈS =====
                // Vide la liste des images pour le prochain ajout
                images.postValue(new ArrayList<>());

                // Notifie le succès
                submitResult.postValue(new SubmitResult(true, "Déclaration enregistrée avec succès"));

            } catch (Exception e) {
                // Gestion des erreurs inattendues
                e.printStackTrace();
                submitResult.postValue(new SubmitResult(false, "Erreur: " + e.getMessage()));
            }
        }).start();
    }

    // ===== SAUVEGARDE DES IMAGES SUR LE DISQUE =====

    /**
     * Sauvegarde les images sur le stockage interne de l'application
     * et enregistre leurs chemins dans la base de données
     *
     * Processus pour chaque image:
     * 1. Crée un dossier "images" s'il n'existe pas
     * 2. Génère un nom de fichier unique
     * 3. Compresse et sauvegarde l'image en JPEG
     * 4. Enregistre le chemin dans la table "images" de la BDD
     *
     * @param objetId ID de l'objet auquel appartiennent les images
     * @param bitmaps Liste des images à sauvegarder
     * @return true si toutes les images ont été sauvegardées, false sinon
     */
    private boolean saveImages(int objetId, List<Bitmap> bitmaps) {
        // ===== CRÉATION DU RÉPERTOIRE D'IMAGES =====
        // Crée un dossier "images" dans le stockage privé de l'app
        File imageDir = new File(getApplication().getFilesDir(), "images");

        if (!imageDir.exists()) {
            imageDir.mkdirs(); // Crée le dossier s'il n'existe pas
        }

        // ===== SAUVEGARDE DE CHAQUE IMAGE =====
        for (int i = 0; i < bitmaps.size(); i++) {
            try {
                // Génère un nom de fichier unique
                // Format: "objet_42_img_0.jpg", "objet_42_img_1.jpg", etc.
                String fileName = "objet_" + objetId + "_img_" + i + ".jpg";
                File imageFile = new File(imageDir, fileName);

                // Ouvre un flux de sortie pour écrire l'image
                FileOutputStream fos = new FileOutputStream(imageFile);

                // Compresse le Bitmap en JPEG avec qualité 85%
                // (85 = bon compromis entre qualité et taille)
                bitmaps.get(i).compress(Bitmap.CompressFormat.JPEG, 85, fos);

                // Ferme le flux
                fos.close();

                // ===== ENREGISTREMENT DU CHEMIN EN BASE =====
                // Insère une entrée dans la table "images"
                // Lie l'image à l'objet via objetId
                boolean inserted = databaseHelper.insertImage(
                        objetId,
                        imageFile.getAbsolutePath() // Chemin complet du fichier
                );

                if (!inserted) {
                    // Échec de l'insertion en base
                    return false;
                }

            } catch (IOException e) {
                // Erreur lors de l'écriture du fichier
                e.printStackTrace();
                return false;
            }
        }

        // Toutes les images ont été sauvegardées avec succès
        return true;
    }

    // ===== RÉINITIALISATION =====

    /**
     * Vide la liste des images
     * Utilisé pour réinitialiser le formulaire après soumission
     */
    public void clearImages() {
        images.setValue(new ArrayList<>());
    }

    // ===== CLASSE INTERNE - RÉSULTAT DE SOUMISSION =====

    /**
     * Classe simple qui encapsule le résultat d'une soumission
     * Contient un état (succès/échec) et un message
     */
    public static class SubmitResult {
        private final boolean success;  // true = succès, false = échec
        private final String message;   // Message descriptif pour l'utilisateur

        public SubmitResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}

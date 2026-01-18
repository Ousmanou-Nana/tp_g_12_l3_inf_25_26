package com.example.tp_g_12_l3_inf_25_26.ui.user.declareobjectfrom;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tp_g_12_l3_inf_25_26.DB.DatabaseHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * ViewModel pour la déclaration d'objets perdus par les utilisateurs.
 *
 * Ce ViewModel gère toute la logique métier liée à la déclaration d'objets perdus:
 * - Persistance des informations utilisateur via SharedPreferences
 * - Gestion des images sélectionnées
 * - Validation des formulaires
 * - Communication avec la base de données via DatabaseHelper
 * - Sauvegarde des images dans le stockage interne
 *
 * Architecture MVVM:
 * - Le ViewModel survit aux changements de configuration (rotation, etc.)
 * - Utilise LiveData pour communication réactive avec l'UI
 * - Sépare la logique métier de l'interface utilisateur
 *
 * @author Votre équipe
 * @version 1.0
 */
public class UserDeclareObjectFromViewModel extends AndroidViewModel {

    // ==================== CONSTANTES ====================

    /**
     * Nom du fichier SharedPreferences pour stocker les données utilisateur
     */
    private static final String PREFS_NAME = "UserPrefs";

    /**
     * Clé pour l'ID utilisateur dans SharedPreferences
     */
    private static final String KEY_USER_ID = "userId";

    /**
     * Clé pour le nom de l'utilisateur dans SharedPreferences
     */
    private static final String KEY_USER_NAME = "userName";

    /**
     * Clé pour le téléphone de l'utilisateur dans SharedPreferences
     */
    private static final String KEY_USER_PHONE = "userPhone";

    /**
     * Clé pour le matricule de l'utilisateur dans SharedPreferences
     */
    private static final String KEY_USER_MATRICULE = "userMatricule";

    // ==================== ATTRIBUTS ====================

    /**
     * Helper pour toutes les opérations sur la base de données SQLite
     */
    private final DatabaseHelper databaseHelper;

    /**
     * SharedPreferences pour la persistance des données utilisateur
     * entre les sessions de l'application
     */
    private final SharedPreferences sharedPreferences;

    /**
     * LiveData contenant la liste des URIs des images sélectionnées.
     * Observable par les fragments pour mettre à jour l'UI.
     */
    private final MutableLiveData<List<Uri>> selectedImagesLiveData = new MutableLiveData<>(new ArrayList<>());

    /**
     * LiveData contenant les informations utilisateur en cache.
     * Permet le pré-remplissage automatique des champs du formulaire.
     */
    private final MutableLiveData<UserInfo> userInfoLiveData = new MutableLiveData<>();

    /**
     * LiveData pour communiquer le résultat de la soumission (succès ou échec).
     * Observé par le fragment pour afficher des messages Toast.
     */
    private final MutableLiveData<SubmitResult> submitResultLiveData = new MutableLiveData<>();

    // ==================== CONSTRUCTEUR ====================

    /**
     * Constructeur du ViewModel.
     *
     * Initialise:
     * - DatabaseHelper pour accès à la BD
     * - SharedPreferences pour le cache utilisateur
     * - Charge immédiatement les infos utilisateur depuis le cache
     *
     * @param application Contexte de l'application (survit aux Activities/Fragments)
     */
    public UserDeclareObjectFromViewModel(@NonNull Application application) {
        super(application);

        // Initialisation de la base de données
        databaseHelper = new DatabaseHelper(application);

        // Initialisation des SharedPreferences en mode privé
        sharedPreferences = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Chargement immédiat des informations utilisateur depuis le cache
        loadUserInfoFromCache();
    }

    // ==================== OPÉRATIONS SUR LE CACHE ====================

    /**
     * Charge les informations utilisateur depuis SharedPreferences.
     *
     * Processus:
     * 1. Récupération des valeurs depuis SharedPreferences
     * 2. Création d'un objet UserInfo
     * 3. Mise à jour du LiveData pour notifier les observateurs
     *
     * Cette méthode est appelée automatiquement au démarrage du ViewModel
     * pour restaurer les informations utilisateur d'une session précédente.
     */
    private void loadUserInfoFromCache() {
        // Récupération des données avec valeurs par défaut vides
        String name = sharedPreferences.getString(KEY_USER_NAME, "");
        String phone = sharedPreferences.getString(KEY_USER_PHONE, "");
        String matricule = sharedPreferences.getString(KEY_USER_MATRICULE, "");

        // Création et publication de l'objet UserInfo
        UserInfo userInfo = new UserInfo(name, phone, matricule);
        userInfoLiveData.setValue(userInfo);
    }

    /**
     * Sauvegarde les informations utilisateur dans SharedPreferences.
     *
     * Cette méthode est appelée lors de la soumission d'une déclaration
     * pour mémoriser les informations de l'utilisateur et faciliter
     * les déclarations futures.
     *
     * Utilise apply() pour une sauvegarde asynchrone (plus performant que commit()).
     *
     * @param name Nom de l'utilisateur
     * @param phone Numéro de téléphone
     * @param matricule Matricule de l'utilisateur
     */
    public void saveUserInfoToCache(String name, String phone, String matricule) {
        // Sauvegarde dans SharedPreferences
        sharedPreferences.edit()
                .putString(KEY_USER_NAME, name)
                .putString(KEY_USER_PHONE, phone)
                .putString(KEY_USER_MATRICULE, matricule)
                .apply(); // Asynchrone

        // Mise à jour du LiveData pour notification immédiate
        UserInfo userInfo = new UserInfo(name, phone, matricule);
        userInfoLiveData.setValue(userInfo);
    }

    /**
     * Efface toutes les informations utilisateur du cache.
     *
     * Utilisé quand l'utilisateur souhaite réinitialiser ses informations
     * ou se "déconnecter" du formulaire.
     *
     * Supprime toutes les entrées utilisateur de SharedPreferences
     * et réinitialise le LiveData avec des valeurs vides.
     */
    public void clearUserCache() {
        // Suppression de toutes les clés utilisateur
        sharedPreferences.edit()
                .remove(KEY_USER_NAME)
                .remove(KEY_USER_PHONE)
                .remove(KEY_USER_MATRICULE)
                .apply();

        // Réinitialisation du LiveData
        userInfoLiveData.setValue(new UserInfo("", "", ""));
    }

    // ==================== GETTERS POUR LIVEDATA ====================

    /**
     * Retourne le LiveData des informations utilisateur.
     *
     * @return LiveData observable contenant les informations de l'utilisateur
     */
    public LiveData<UserInfo> getUserInfoLiveData() {
        return userInfoLiveData;
    }

    /**
     * Retourne le LiveData de la liste des images sélectionnées.
     *
     * @return LiveData observable contenant la liste des URIs d'images
     */
    public LiveData<List<Uri>> getSelectedImagesLiveData() {
        return selectedImagesLiveData;
    }

    /**
     * Retourne le LiveData du résultat de soumission.
     *
     * @return LiveData observable contenant le résultat (succès/échec)
     */
    public LiveData<SubmitResult> getSubmitResultLiveData() {
        return submitResultLiveData;
    }

    // ==================== OPÉRATIONS SUR LES IMAGES ====================

    /**
     * Ajoute une image URI à la liste des images sélectionnées.
     *
     * @param uri URI de l'image à ajouter (doit avoir les permissions persistantes)
     */
    public void addImage(Uri uri) {
        List<Uri> current = selectedImagesLiveData.getValue();
        if (current != null) {
            current.add(uri);
            selectedImagesLiveData.setValue(current);
        }
    }

    /**
     * Supprime une image de la liste à une position donnée.
     *
     * @param position Index de l'image à supprimer
     */
    public void removeImage(int position) {
        List<Uri> current = selectedImagesLiveData.getValue();
        if (current != null && position >= 0 && position < current.size()) {
            current.remove(position);
            selectedImagesLiveData.setValue(current);
        }
    }

    /**
     * Supprime toutes les images sélectionnées.
     *
     * Utilisé après une soumission réussie ou pour réinitialiser le formulaire.
     * Utilise postValue() car peut être appelé depuis un thread d'arrière-plan.
     */
    public void clearImages() {
        selectedImagesLiveData.postValue(new ArrayList<>());
    }

    // ==================== VALIDATION ====================

    /**
     * Valide les champs du formulaire avant soumission.
     *
     * Règles de validation:
     * - Tous les champs texte doivent être non vides
     * - Au moins une image doit être sélectionnée
     *
     * @param name Nom de l'utilisateur
     * @param phone Numéro de téléphone
     * @param matricule Matricule
     * @param description Description de l'objet
     * @param type Type de l'objet
     * @return true si le formulaire est valide, false sinon
     */
    public boolean isFormValid(String name, String phone, String matricule,
                               String description, String type) {
        // Vérification de la présence d'images
        boolean hasImages = selectedImagesLiveData.getValue() != null
                && !selectedImagesLiveData.getValue().isEmpty();

        // Validation: tous les champs remplis ET au moins une image
        return !name.isEmpty() && !phone.isEmpty() && !matricule.isEmpty()
                && !description.isEmpty() && !type.isEmpty() && hasImages;
    }

    // ==================== TYPES D'OBJETS ====================

    /**
     * Récupère tous les types d'objets disponibles depuis la base de données.
     *
     * Utilisé pour peupler le Spinner de sélection de type.
     *
     * Processus:
     * 1. Requête à la BD via DatabaseHelper
     * 2. Parcours du Cursor pour extraire les noms de types
     * 3. Conversion de la liste en tableau
     * 4. Fermeture du Cursor
     *
     * @return Tableau de chaînes contenant tous les noms de types
     */
    public String[] getObjectTypes() {
        List<String> typesList = new ArrayList<>();
        Cursor cursor = databaseHelper.getAllTypes();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                // Récupération sécurisée de la colonne "nom_type"
                int nameIndex = cursor.getColumnIndex("nom_type");
                if (nameIndex != -1) {
                    typesList.add(cursor.getString(nameIndex));
                }
            }
            cursor.close(); // IMPORTANT: Toujours fermer le Cursor
        }

        // Conversion de List<String> en String[]
        return typesList.toArray(new String[0]);
    }

    // ==================== SOUMISSION DE DÉCLARATION ====================

    /**
     * Soumet une déclaration d'objet perdu à la base de données.
     *
     * Cette méthode coordonne tout le processus de soumission:
     * 1. Sauvegarde des infos utilisateur dans le cache
     * 2. Création/récupération de l'utilisateur dans la BD
     * 3. Validation du type d'objet
     * 4. Insertion de la déclaration
     * 5. Sauvegarde des images
     * 6. Notification du résultat
     *
     * IMPORTANT: Exécuté dans un thread séparé pour éviter de bloquer l'UI.
     *
     * @param name Nom de l'utilisateur
     * @param phone Numéro de téléphone
     * @param matricule Matricule
     * @param description Description de l'objet perdu
     * @param type Type de l'objet
     */
    public void submitDeclaration(String name, String phone, String matricule,
                                  String description, String type) {

        // Sauvegarde immédiate dans le cache pour utilisation future
        saveUserInfoToCache(name, phone, matricule);

        // Exécution dans un thread séparé (opérations BD et I/O)
        new Thread(() -> {
            try {
                // ÉTAPE 1: Obtenir ou créer l'utilisateur
                int userId = getUserIdOrCreate(name, phone, matricule);
                if (userId == -1) {
                    submitResultLiveData.postValue(
                            new SubmitResult(false, "Erreur lors de la création de l'utilisateur")
                    );
                    return;
                }

                // ÉTAPE 2: Obtenir l'ID du type depuis le nom
                int typeId = databaseHelper.getTypeIdByName(type);
                if (typeId == -1) {
                    submitResultLiveData.postValue(
                            new SubmitResult(false, "Type d'objet invalide")
                    );
                    return;
                }

                // ÉTAPE 3: Obtenir la date et heure actuelles
                String currentDate = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault()
                ).format(new Date());

                // ÉTAPE 4: Insertion de la déclaration dans la table DECLARATION
                long declarationId = databaseHelper.insertDeclaration(
                        userId,
                        description,
                        typeId,
                        currentDate
                );

                if (declarationId == -1) {
                    submitResultLiveData.postValue(
                            new SubmitResult(false, "Erreur lors de l'enregistrement de la déclaration")
                    );
                    return;
                }

                // ÉTAPE 5: Sauvegarde des images
                List<Uri> images = selectedImagesLiveData.getValue();
                if (images != null && !images.isEmpty()) {
                    boolean allImagesSaved = saveImagesFromUri((int) declarationId, images);
                    if (!allImagesSaved) {
                        submitResultLiveData.postValue(
                                new SubmitResult(false, "Erreur lors de la sauvegarde des images")
                        );
                        return;
                    }
                }

                // ÉTAPE 6: Nettoyage des images après succès
                clearImages();

                // ÉTAPE 7: Notification de succès
                submitResultLiveData.postValue(
                        new SubmitResult(true, "Déclaration envoyée avec succès")
                );

            } catch (Exception e) {
                // Gestion des erreurs inattendues
                e.printStackTrace();
                submitResultLiveData.postValue(
                        new SubmitResult(false, "Erreur: " + e.getMessage())
                );
            }
        }).start(); // Démarrage du thread
    }

    /**
     * Obtient l'ID d'un utilisateur par son matricule ou crée un nouvel utilisateur.
     *
     * Logique:
     * 1. Recherche de l'utilisateur par matricule
     * 2. Si trouvé: mise à jour des infos si nécessaire, retour de l'ID
     * 3. Si non trouvé: création d'un nouvel utilisateur, retour du nouvel ID
     *
     * Cette approche garantit qu'un utilisateur avec le même matricule
     * ne sera jamais dupliqué dans la base de données.
     *
     * @param name Nom de l'utilisateur
     * @param phone Numéro de téléphone
     * @param matricule Matricule (identifiant unique)
     * @return ID de l'utilisateur, ou -1 en cas d'erreur
     */
    private int getUserIdOrCreate(String name, String phone, String matricule) {
        // Recherche de l'utilisateur existant
        Cursor cursor = databaseHelper.getUserByMatricule(matricule);

        if (cursor != null && cursor.moveToFirst()) {
            // Utilisateur trouvé
            int idIndex = cursor.getColumnIndex("id_user");
            int userId = -1;

            if (idIndex != -1) {
                userId = cursor.getInt(idIndex);

                // Vérification si mise à jour nécessaire
                int nameIndex = cursor.getColumnIndex("name");
                int phoneIndex = cursor.getColumnIndex("phone");

                if (nameIndex != -1 && phoneIndex != -1) {
                    String existingName = cursor.getString(nameIndex);
                    String existingPhone = cursor.getString(phoneIndex);

                    // Mise à jour si les informations ont changé
                    if (!existingName.equals(name) || !existingPhone.equals(phone)) {
                        databaseHelper.updateUser(userId, name, phone, matricule);
                    }
                }
            }
            cursor.close();
            return userId;
        }

        // Fermeture du cursor si non null
        if (cursor != null) {
            cursor.close();
        }

        // Utilisateur non trouvé: création d'un nouveau
        long newUserId = databaseHelper.insertUser(name, phone, matricule);
        return (int) newUserId;
    }

    /**
     * Sauvegarde les images depuis leurs URIs vers le stockage interne
     * et enregistre les chemins dans la base de données.
     *
     * Processus pour chaque image:
     * 1. Création d'un nom de fichier unique
     * 2. Copie du contenu de l'URI vers un fichier interne
     * 3. Insertion du chemin dans la table IMAGES de la BD
     *
     * Avantages du stockage interne:
     * - Les fichiers persistent même si l'URI d'origine est révoquée
     * - Contrôle total sur les fichiers
     * - Pas besoin de permissions externes
     *
     * @param declarationId ID de la déclaration associée
     * @param imageUris Liste des URIs des images à sauvegarder
     * @return true si toutes les images ont été sauvegardées avec succès, false sinon
     */
    private boolean saveImagesFromUri(int declarationId, List<Uri> imageUris) {
        // Création du répertoire images dans le stockage interne
        File imageDir = new File(getApplication().getFilesDir(), "images");
        if (!imageDir.exists()) {
            imageDir.mkdirs(); // Création récursive si nécessaire
        }

        // Traitement de chaque image
        for (int i = 0; i < imageUris.size(); i++) {
            try {
                Uri uri = imageUris.get(i);

                // Génération d'un nom unique pour l'image
                String fileName = "user_declaration_" + declarationId + "_img_" + i + ".jpg";
                File imageFile = new File(imageDir, fileName);

                // Ouverture du flux d'entrée depuis l'URI
                InputStream inputStream = getApplication()
                        .getContentResolver()
                        .openInputStream(uri);

                if (inputStream != null) {
                    // Création du flux de sortie vers le fichier
                    FileOutputStream outputStream = new FileOutputStream(imageFile);

                    // Copie par blocs de 1024 octets
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }

                    // Fermeture des flux
                    outputStream.close();
                    inputStream.close();

                    // Insertion du chemin dans la base de données
                    boolean inserted = databaseHelper.insertImageForDeclaration(
                            declarationId,
                            imageFile.getAbsolutePath()
                    );

                    if (!inserted) {
                        return false; // Échec d'insertion en BD
                    }
                } else {
                    return false; // Impossible d'ouvrir l'URI
                }

            } catch (Exception e) {
                e.printStackTrace();
                return false; // Erreur pendant la copie
            }
        }

        return true; // Toutes les images sauvegardées avec succès
    }

    // ==================== CLASSES INTERNES ====================

    /**
     * Classe conteneur pour les informations utilisateur.
     *
     * Immutable (final fields) pour garantir la cohérence des données.
     * Utilisée pour transporter les données entre le cache et l'UI.
     */
    public static class UserInfo {
        private final String name;
        private final String phone;
        private final String matricule;

        /**
         * Constructeur.
         *
         * @param name Nom de l'utilisateur
         * @param phone Numéro de téléphone
         * @param matricule Matricule
         */
        public UserInfo(String name, String phone, String matricule) {
            this.name = name;
            this.phone = phone;
            this.matricule = matricule;
        }

        public String getName() {
            return name;
        }

        public String getPhone() {
            return phone;
        }

        public String getMatricule() {
            return matricule;
        }

        /**
         * Vérifie si toutes les informations sont vides.
         *
         * @return true si tous les champs sont vides
         */
        public boolean isEmpty() {
            return name.isEmpty() && phone.isEmpty() && matricule.isEmpty();
        }
    }

    /**
     * Classe conteneur pour le résultat d'une soumission.
     *
     * Encapsule le statut (succès/échec) et le message associé.
     * Utilisée pour communiquer les résultats via LiveData.
     */
    public static class SubmitResult {
        private final boolean success;
        private final String message;

        /**
         * Constructeur.
         *
         * @param success true si la soumission a réussi
         * @param message Message descriptif du résultat
         */
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
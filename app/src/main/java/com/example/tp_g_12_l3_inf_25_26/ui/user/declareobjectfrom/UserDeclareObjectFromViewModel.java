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

public class UserDeclareObjectFromViewModel extends AndroidViewModel {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_PHONE = "userPhone";
    private static final String KEY_USER_MATRICULE = "userMatricule";

    private final DatabaseHelper databaseHelper;
    private final SharedPreferences sharedPreferences;

    private final MutableLiveData<List<Uri>> selectedImagesLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<UserInfo> userInfoLiveData = new MutableLiveData<>();
    private final MutableLiveData<SubmitResult> submitResultLiveData = new MutableLiveData<>();

    public UserDeclareObjectFromViewModel(@NonNull Application application) {
        super(application);
        databaseHelper = new DatabaseHelper(application);
        sharedPreferences = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Charger les informations utilisateur depuis le cache
        loadUserInfoFromCache();
    }

    // ==================== CACHE OPERATIONS ====================

    /**
     * Charge les informations utilisateur depuis SharedPreferences
     */
    private void loadUserInfoFromCache() {
        String name = sharedPreferences.getString(KEY_USER_NAME, "");
        String phone = sharedPreferences.getString(KEY_USER_PHONE, "");
        String matricule = sharedPreferences.getString(KEY_USER_MATRICULE, "");

        UserInfo userInfo = new UserInfo(name, phone, matricule);
        userInfoLiveData.setValue(userInfo);
    }

    /**
     * Sauvegarde les informations utilisateur dans SharedPreferences
     */
    public void saveUserInfoToCache(String name, String phone, String matricule) {
        sharedPreferences.edit()
                .putString(KEY_USER_NAME, name)
                .putString(KEY_USER_PHONE, phone)
                .putString(KEY_USER_MATRICULE, matricule)
                .apply();

        UserInfo userInfo = new UserInfo(name, phone, matricule);
        userInfoLiveData.setValue(userInfo);
    }

    /**
     * Efface les informations utilisateur du cache
     */
    public void clearUserCache() {
        sharedPreferences.edit()
                .remove(KEY_USER_NAME)
                .remove(KEY_USER_PHONE)
                .remove(KEY_USER_MATRICULE)
                .apply();

        userInfoLiveData.setValue(new UserInfo("", "", ""));
    }

    // ==================== GETTERS ====================

    public LiveData<UserInfo> getUserInfoLiveData() {
        return userInfoLiveData;
    }

    public LiveData<List<Uri>> getSelectedImagesLiveData() {
        return selectedImagesLiveData;
    }

    public LiveData<SubmitResult> getSubmitResultLiveData() {
        return submitResultLiveData;
    }

    // ==================== IMAGE OPERATIONS ====================

    /**
     * Ajoute une image à la liste des images sélectionnées
     */
    public void addImage(Uri uri) {
        List<Uri> current = selectedImagesLiveData.getValue();
        if (current != null) {
            current.add(uri);
            selectedImagesLiveData.setValue(current);
        }
    }

    /**
     * Supprime une image de la liste
     */
    public void removeImage(int position) {
        List<Uri> current = selectedImagesLiveData.getValue();
        if (current != null && position >= 0 && position < current.size()) {
            current.remove(position);
            selectedImagesLiveData.setValue(current);
        }
    }

    /**
     * Efface toutes les images
     */
    public void clearImages() {
        selectedImagesLiveData.setValue(new ArrayList<>());
    }

    // ==================== VALIDATION ====================

    /**
     * Vérifie que tous les champs du formulaire sont remplis
     */
    public boolean isFormValid(String name, String phone, String matricule, String description, String type) {
        boolean hasImages = selectedImagesLiveData.getValue() != null
                && !selectedImagesLiveData.getValue().isEmpty();

        return !name.isEmpty() && !phone.isEmpty() && !matricule.isEmpty()
                && !description.isEmpty() && !type.isEmpty() && hasImages;
    }

    // ==================== TYPES D'OBJETS ====================

    /**
     * Retourne les types d'objets disponibles depuis la base de données
     */
    public String[] getObjectTypes() {
        List<String> typesList = new ArrayList<>();
        Cursor cursor = databaseHelper.getAllTypes();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int nameIndex = cursor.getColumnIndex("nom_type");
                if (nameIndex != -1) {
                    typesList.add(cursor.getString(nameIndex));
                }
            }
            cursor.close();
        }

        return typesList.toArray(new String[0]);
    }

    // ==================== SUBMIT DECLARATION ====================

    /**
     * Soumet la déclaration à la base de données
     */
    public void submitDeclaration(String name, String phone, String matricule,
                                  String description, String type) {

        // Sauvegarder les infos utilisateur dans le cache pour la prochaine fois
        saveUserInfoToCache(name, phone, matricule);

        new Thread(() -> {
            try {
                // Récupérer l'ID du type d'objet
                int typeId = databaseHelper.getTypeIdByName(type);
                if (typeId == -1) {
                    submitResultLiveData.postValue(new SubmitResult(false, "Type d'objet invalide"));
                    return;
                }

                // Date actuelle
                String currentDate = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault()
                ).format(new Date());

                // Ajouter le matricule à la description
                String fullDescription = "Matricule: " + matricule + "\n\n" + description;

                // Insérer l'objet (id_admin = null car c'est un utilisateur)
                long objetId = databaseHelper.insertObjet(
                        name,
                        phone,
                        fullDescription,
                        typeId,
                        currentDate,
                        0  // 0 ou null pour les déclarations utilisateur
                );

                if (objetId == -1) {
                    submitResultLiveData.postValue(new SubmitResult(false, "Erreur lors de l'enregistrement"));
                    return;
                }

                // Sauvegarder les images
                List<Uri> images = selectedImagesLiveData.getValue();
                if (images != null && !images.isEmpty()) {
                    boolean allImagesSaved = saveImagesFromUri((int) objetId, images);
                    if (!allImagesSaved) {
                        submitResultLiveData.postValue(new SubmitResult(false, "Erreur lors de la sauvegarde des images"));
                        return;
                    }
                }

                // Effacer les images après soumission réussie
                clearImages();

                submitResultLiveData.postValue(new SubmitResult(true, "Déclaration envoyée avec succès"));

            } catch (Exception e) {
                e.printStackTrace();
                submitResultLiveData.postValue(new SubmitResult(false, "Erreur: " + e.getMessage()));
            }
        }).start();
    }

    /**
     * Sauvegarde les images depuis les URI
     */
    private boolean saveImagesFromUri(int objetId, List<Uri> imageUris) {
        File imageDir = new File(getApplication().getFilesDir(), "images");
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }

        for (int i = 0; i < imageUris.size(); i++) {
            try {
                Uri uri = imageUris.get(i);
                String fileName = "user_objet_" + objetId + "_img_" + i + ".jpg";
                File imageFile = new File(imageDir, fileName);

                // Copier le contenu de l'URI vers le fichier
                InputStream inputStream = getApplication().getContentResolver().openInputStream(uri);
                if (inputStream != null) {
                    FileOutputStream outputStream = new FileOutputStream(imageFile);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                    outputStream.close();
                    inputStream.close();

                    // Enregistrer le chemin dans la base de données
                    boolean inserted = databaseHelper.insertImage(objetId, imageFile.getAbsolutePath());
                    if (!inserted) {
                        return false;
                    }
                } else {
                    return false;
                }

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    // ==================== INNER CLASSES ====================

    /**
     * Classe pour stocker les informations utilisateur
     */
    public static class UserInfo {
        private final String name;
        private final String phone;
        private final String matricule;

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

        public boolean isEmpty() {
            return name.isEmpty() && phone.isEmpty() && matricule.isEmpty();
        }
    }

    /**
     * Classe pour le résultat de soumission
     */
    public static class SubmitResult {
        private final boolean success;
        private final String message;

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
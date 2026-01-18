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
    private static final String KEY_USER_ID = "userId";
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

        // Load user info immediately from SharedPreferences
        loadUserInfoFromCache();
    }

    // ==================== CACHE OPERATIONS ====================

    /**
     * Load user information from SharedPreferences cache
     */
    private void loadUserInfoFromCache() {
        String name = sharedPreferences.getString(KEY_USER_NAME, "");
        String phone = sharedPreferences.getString(KEY_USER_PHONE, "");
        String matricule = sharedPreferences.getString(KEY_USER_MATRICULE, "");

        UserInfo userInfo = new UserInfo(name, phone, matricule);
        userInfoLiveData.setValue(userInfo);
    }

    /**
     * Save user information to SharedPreferences cache
     * This is called when a declaration is submitted to remember user data
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
     * Clear user cache - removes saved user information
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
     * Add an image URI to the selected images list
     */
    public void addImage(Uri uri) {
        List<Uri> current = selectedImagesLiveData.getValue();
        if (current != null) {
            current.add(uri);
            selectedImagesLiveData.setValue(current);
        }
    }

    /**
     * Remove an image from the selected images list
     */
    public void removeImage(int position) {
        List<Uri> current = selectedImagesLiveData.getValue();
        if (current != null && position >= 0 && position < current.size()) {
            current.remove(position);
            selectedImagesLiveData.setValue(current);
        }
    }

    /**
     * Clear all selected images
     */
    public void clearImages() {
        selectedImagesLiveData.postValue(new ArrayList<>());
    }


    // ==================== VALIDATION ====================

    /**
     * Validate form fields before submission
     */
    public boolean isFormValid(String name, String phone, String matricule,
                               String description, String type) {
        boolean hasImages = selectedImagesLiveData.getValue() != null
                && !selectedImagesLiveData.getValue().isEmpty();

        return !name.isEmpty() && !phone.isEmpty() && !matricule.isEmpty()
                && !description.isEmpty() && !type.isEmpty() && hasImages;
    }

    // ==================== TYPES D'OBJETS ====================

    /**
     * Get all object types from database
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
     * Submit the declaration to the database
     * Uses the DECLARATION table instead of OBJET
     */
    public void submitDeclaration(String name, String phone, String matricule,
                                  String description, String type) {

        // Save user info to cache for future use
        saveUserInfoToCache(name, phone, matricule);

        new Thread(() -> {
            try {
                // 1. Get or create user ID
                int userId = getUserIdOrCreate(name, phone, matricule);
                if (userId == -1) {
                    submitResultLiveData.postValue(
                            new SubmitResult(false, "Erreur lors de la création de l'utilisateur")
                    );
                    return;
                }

                // 2. Get type ID from type name
                int typeId = databaseHelper.getTypeIdByName(type);
                if (typeId == -1) {
                    submitResultLiveData.postValue(
                            new SubmitResult(false, "Type d'objet invalide")
                    );
                    return;
                }

                // 3. Get current date and time
                String currentDate = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault()
                ).format(new Date());

                // 4. Insert declaration into DECLARATION table
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

                // 5. Save images to internal storage and database
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

                // 6. Clear images after successful submission
                clearImages();


                submitResultLiveData.postValue(
                        new SubmitResult(true, "Déclaration envoyée avec succès")
                );

            } catch (Exception e) {
                e.printStackTrace();
                submitResultLiveData.postValue(
                        new SubmitResult(false, "Erreur: " + e.getMessage() )
                );
            }
        }).start();
    }

    /**
     * Get user ID by matricule or create a new user if doesn't exist
     */
    private int getUserIdOrCreate(String name, String phone, String matricule) {
        // Check if user exists by matricule
        Cursor cursor = databaseHelper.getUserByMatricule(matricule);

        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex("id_user");
            int userId = -1;

            if (idIndex != -1) {
                userId = cursor.getInt(idIndex);

                // Update user info if it has changed
                int nameIndex = cursor.getColumnIndex("name");
                int phoneIndex = cursor.getColumnIndex("phone");

                if (nameIndex != -1 && phoneIndex != -1) {
                    String existingName = cursor.getString(nameIndex);
                    String existingPhone = cursor.getString(phoneIndex);

                    if (!existingName.equals(name) || !existingPhone.equals(phone)) {
                        databaseHelper.updateUser(userId, name, phone, matricule);
                    }
                }
            }
            cursor.close();
            return userId;
        }

        if (cursor != null) {
            cursor.close();
        }

        // User doesn't exist, create new user
        long newUserId = databaseHelper.insertUser(name, phone, matricule);
        return (int) newUserId;
    }

    /**
     * Save images from URIs to internal storage and database
     */
    private boolean saveImagesFromUri(int declarationId, List<Uri> imageUris) {
        // Create images directory in internal storage
        File imageDir = new File(getApplication().getFilesDir(), "images");
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }

        for (int i = 0; i < imageUris.size(); i++) {
            try {
                Uri uri = imageUris.get(i);
                String fileName = "user_declaration_" + declarationId + "_img_" + i + ".jpg";
                File imageFile = new File(imageDir, fileName);

                // Copy content from URI to file
                InputStream inputStream = getApplication()
                        .getContentResolver()
                        .openInputStream(uri);

                if (inputStream != null) {
                    FileOutputStream outputStream = new FileOutputStream(imageFile);
                    byte[] buffer = new byte[1024];
                    int length;

                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }

                    outputStream.close();
                    inputStream.close();

                    // Save image path to database for DECLARATION
                    boolean inserted = databaseHelper.insertImageForDeclaration(
                            declarationId,
                            imageFile.getAbsolutePath()
                    );

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
     * User information holder class
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
     * Submission result holder class
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
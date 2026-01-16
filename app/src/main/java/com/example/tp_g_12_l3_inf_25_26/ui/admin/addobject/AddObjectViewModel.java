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

    private static final int MAX_IMAGES = 5;
    private final DatabaseHelper databaseHelper;

    // Images sélectionnées
    private final MutableLiveData<List<Bitmap>> images =
            new MutableLiveData<>(new ArrayList<>());

    // État de validation du formulaire
    private final MutableLiveData<Boolean> formValid =
            new MutableLiveData<>(false);

    // État de soumission
    private final MutableLiveData<SubmitResult> submitResult =
            new MutableLiveData<>();

    public AddObjectViewModel(@NonNull Application application) {
        super(application);
        databaseHelper = new DatabaseHelper(application);
    }

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

    public LiveData<List<Bitmap>> getImages() {
        return images;
    }

    public LiveData<Boolean> isFormValid() {
        return formValid;
    }

    public LiveData<SubmitResult> getSubmitResult() {
        return submitResult;
    }

    public void addImage(Bitmap bitmap) {
        List<Bitmap> current = images.getValue();
        if (current != null) {
            if (current.size() < MAX_IMAGES) {
                current.add(bitmap);
                images.setValue(current);
            }
        }
    }

    public void removeImage(int position) {
        List<Bitmap> current = images.getValue();
        if (current != null && position >= 0 && position < current.size()) {
            current.remove(position);
            images.setValue(current);
        }
    }

    public int getImageCount() {
        List<Bitmap> current = images.getValue();
        return current != null ? current.size() : 0;
    }

    public boolean canAddMoreImages() {
        return getImageCount() < MAX_IMAGES;
    }

    public void validateForm(String name,
                             String phone,
                             String description,
                             String type) {

        boolean valid =
                !name.isEmpty()
                        && !phone.isEmpty()
                        && !description.isEmpty()
                        && !type.isEmpty()
                        && images.getValue() != null
                        && !images.getValue().isEmpty();

        formValid.setValue(valid);
    }

    public void submitDeclaration(String name,
                                  String phone,
                                  String description,
                                  String type) {

        new Thread(() -> {
            try {
                // Récupérer l'ID de l'admin connecté
                int adminId = LoginAdminFragment.getLoggedInAdminId(getApplication());
                if (adminId == -1) {
                    submitResult.postValue(new SubmitResult(false, "Admin non connecté"));
                    return;
                }

                // Récupérer l'ID du type d'objet
                int typeId = databaseHelper.getTypeIdByName(type);
                if (typeId == -1) {
                    submitResult.postValue(new SubmitResult(false, "Type d'objet invalide"));
                    return;
                }

                // Date actuelle
                String currentDate = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault()
                ).format(new Date());

                // Insérer l'objet dans la base de données
                long objetId = databaseHelper.insertObjet(
                        name,
                        phone,
                        description,
                        typeId,
                        currentDate,
                        adminId
                );

                if (objetId == -1) {
                    submitResult.postValue(new SubmitResult(false, "Erreur lors de l'enregistrement"));
                    return;
                }

                // Sauvegarder les images
                List<Bitmap> currentImages = images.getValue();
                if (currentImages != null && !currentImages.isEmpty()) {
                    boolean allImagesSaved = saveImages((int) objetId, currentImages);
                    if (!allImagesSaved) {
                        submitResult.postValue(new SubmitResult(false, "Erreur lors de la sauvegarde des images"));
                        return;
                    }
                }

                // Réinitialiser les images après soumission réussie
                images.postValue(new ArrayList<>());

                submitResult.postValue(new SubmitResult(true, "Déclaration enregistrée avec succès"));

            } catch (Exception e) {
                e.printStackTrace();
                submitResult.postValue(new SubmitResult(false, "Erreur: " + e.getMessage()));
            }
        }).start();
    }

    private boolean saveImages(int objetId, List<Bitmap> bitmaps) {
        File imageDir = new File(getApplication().getFilesDir(), "images");
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }

        for (int i = 0; i < bitmaps.size(); i++) {
            try {
                String fileName = "objet_" + objetId + "_img_" + i + ".jpg";
                File imageFile = new File(imageDir, fileName);

                FileOutputStream fos = new FileOutputStream(imageFile);
                bitmaps.get(i).compress(Bitmap.CompressFormat.JPEG, 85, fos);
                fos.close();

                // Enregistrer le chemin dans la base de données
                boolean inserted = databaseHelper.insertImage(objetId, imageFile.getAbsolutePath());
                if (!inserted) {
                    return false;
                }

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    public void clearImages() {
        images.setValue(new ArrayList<>());
    }

    // Classe pour le résultat de soumission
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
package com.example.tp_g_12_l3_inf_25_26.ui.admin.addobject;

import android.graphics.Bitmap;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class AddObjectViewModel extends ViewModel {


    // Images sélectionnées
    private final MutableLiveData<List<Bitmap>> images =
            new MutableLiveData<>(new ArrayList<>());

    // État de validation du formulaire
    private final MutableLiveData<Boolean> formValid =
            new MutableLiveData<>(false);

    public String[] getObjectTypes() {
        // Liste des types d’objets
        // TODO: charger ces types depuis la base de données
        String[] objectTypes = {
                "Portefeuille",
                "Téléphone",
                "Clé",
                "Sac",
                "Autre"
        };

        return objectTypes;
    }

    public LiveData<List<Bitmap>> getImages() {
        return images;
    }

    public LiveData<Boolean> isFormValid() {
        return formValid;
    }

    public void addImage(Bitmap bitmap) {
        List<Bitmap> current = images.getValue();
        if (current != null) {
            current.add(bitmap);
            images.setValue(current);
        }
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

        // TODO: sauvegarder l’objet dans la base de données
        // TODO: associer plusieurs images à l’objet
        // TODO: uploader les images vers le serveur

    }
}

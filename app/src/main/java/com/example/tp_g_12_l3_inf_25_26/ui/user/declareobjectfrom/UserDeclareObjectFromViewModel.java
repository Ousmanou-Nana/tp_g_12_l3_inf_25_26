package com.example.tp_g_12_l3_inf_25_26.ui.user.declareobjectfrom;

import android.net.Uri;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;

public class UserDeclareObjectFromViewModel extends ViewModel {

    private final MutableLiveData<List<Uri>> selectedImagesLiveData = new MutableLiveData<>(new ArrayList<>());

    // Ajoute une image à la liste des images sélectionnées
    public void addImage(Uri uri) {
        List<Uri> current = selectedImagesLiveData.getValue();
        if (current != null) {
            current.add(uri);
            selectedImagesLiveData.setValue(current);
        }
    }

    // Vérifie que tous les champs du formulaire sont remplis
    public boolean isFormValid(String name, String phone, String matricule, String description, String type) {
        return !name.isEmpty() && !phone.isEmpty() && !matricule.isEmpty()
                && !description.isEmpty() && !type.isEmpty();
    }

    // Retourne les types d'objets disponibles pour le spinner
    public String[] getObjectTypes() {
        // TODO: ajouter des méthodes pour récupérer les type d'objets
        return new String[]{"Portefeuille", "Téléphone", "Clé", "Sac", "Autre"};
    }

    // TODO: ajouter des méthodes pour récupérer et envoyer les objets vers la DB ou API
}

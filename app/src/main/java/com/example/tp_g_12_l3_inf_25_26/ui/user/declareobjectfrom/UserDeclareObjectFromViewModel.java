package com.example.tp_g_12_l3_inf_25_26.ui.user.declareobjectfrom;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;

public class UserDeclareObjectFromViewModel extends ViewModel {

    private final MutableLiveData<List<Uri>> selectedImagesLiveData = new MutableLiveData<>();

    public void setSelectedImages(List<Uri> images) {
        selectedImagesLiveData.setValue(images);
    }

    public LiveData<List<Uri>> getSelectedImages() {
        return selectedImagesLiveData;
    }

    /**
     * Vérifie le formulaire et retourne un message à afficher.
     * Renvoie null si des champs sont manquants.
     */
    public String submitForm(String name, String phone, String matricule, String description, String type) {
        if (name.isEmpty() || phone.isEmpty() || matricule.isEmpty() || description.isEmpty()) {
            return null;
        }

        String message = "Objet déclaré: " + type;
        List<Uri> images = selectedImagesLiveData.getValue();
        if (images != null && !images.isEmpty()) {
            message += " avec " + images.size() + " image(s)";
        }
        return message;
    }
}

package com.example.tp_g_12_l3_inf_25_26.ui.user.vmageverification;

import androidx.lifecycle.ViewModel;

import com.example.tp_g_12_l3_inf_25_26.R;

import java.util.Arrays;
import java.util.List;

public class ImageVerificationViewModel extends ViewModel {

    // Retourne une liste d'images pour vérifier l'objet
    public List<Integer> getVerificationImages(String objectId) {
        // TODO: remplacer ces images statiques par celles liées à l'objet réel depuis DB ou API
        return Arrays.asList(
                R.mipmap.verification_1,
                R.mipmap.verification_2,
                R.mipmap.verification_2,
                R.mipmap.verification_2,
                R.mipmap.verification_2,
                R.mipmap.verification_2,
                R.mipmap.verification_2,
                R.mipmap.verification_2,
                R.mipmap.verification_2,
                R.mipmap.verification_2,
                R.mipmap.verification_2,
                R.mipmap.verification_2,
                R.mipmap.verification_2,
                R.mipmap.verification_2,
                R.mipmap.verification_2,
                R.mipmap.verification_2
        );
    }

    // TODO: ajouter une méthode pour marquer l'image choisie comme vérifiée et notifier le serveur
}

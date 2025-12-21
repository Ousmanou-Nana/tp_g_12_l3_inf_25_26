package com.example.tp_g_12_l3_inf_25_26;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.example.tp_g_12_l3_inf_25_26.ui.admin.home.HomeAdminFragment;

public class MainAdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Charge le layout principal contenant le container admin
        setContentView(R.layout.activity_main_admin);

        // Affiche le HomeAdminFragment au premier lancement
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_admin_container, HomeAdminFragment.newInstance())
                    .commitNow();
        }

        // TODO: vérifier si l’admin est authentifié
        // TODO: rediriger vers LoginAdminFragment si non connecté
    }
}

package com.example.tp_g_12_l3_inf_25_26;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.example.tp_g_12_l3_inf_25_26.ui.admin.home.HomeAdminFragment;
import com.example.tp_g_12_l3_inf_25_26.ui.admin.loging.LoginAdminFragment;

public class MainAdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Charge le layout principal contenant le container admin
        setContentView(R.layout.activity_main_admin);

        // Affiche le fragment approprié au premier lancement
        if (savedInstanceState == null) {
            // Vérifier si l'admin est authentifié
            if (LoginAdminFragment.isAdminLoggedIn(this)) {
                // Admin connecté - afficher HomeAdminFragment
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_admin_container, HomeAdminFragment.newInstance())
                        .commitNow();
            } else {
                // Admin non connecté - rediriger vers LoginAdminFragment
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_admin_container, LoginAdminFragment.newInstance())
                        .commitNow();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Vérifier l'authentification à chaque fois que l'activité revient au premier plan
        if (!LoginAdminFragment.isAdminLoggedIn(this)) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_admin_container, LoginAdminFragment.newInstance())
                    .commit();
        }
    }
}
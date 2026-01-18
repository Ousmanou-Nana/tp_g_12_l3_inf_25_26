// ============================================================================
// MainAdminActivity.java
// ============================================================================
// Activité principale qui contient tous les fragments admin
// Gère la navigation initiale (login ou home selon l'état de connexion)
// ============================================================================

package com.example.tp_g_12_l3_inf_25_26;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.tp_g_12_l3_inf_25_26.ui.admin.home.HomeAdminFragment;
import com.example.tp_g_12_l3_inf_25_26.ui.admin.loging.LoginAdminFragment;

public class MainAdminActivity extends AppCompatActivity {

    /**
     * Appelé lors de la création de l'activité
     * Détermine quel fragment afficher (login ou home)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Définit le layout de l'activité
        setContentView(R.layout.activity_main_admin);

        // ===== NAVIGATION INITIALE =====
        // savedInstanceState est null uniquement au premier lancement
        // Cela évite de recréer le fragment lors d'une rotation d'écran
        if (savedInstanceState == null) {

            // Vérifie si un admin est déjà connecté
            if (LoginAdminFragment.isAdminLoggedIn(this)) {
                // Admin connecté -> Affiche directement la page d'accueil
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_admin_container, HomeAdminFragment.newInstance())
                        .commitNow();  // commitNow() exécute immédiatement
            } else {
                // Aucun admin connecté -> Affiche la page de connexion
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_admin_container, LoginAdminFragment.newInstance())
                        .commitNow();
            }
        }
    }

    /**
     * Appelé quand l'activité redevient visible
     * Vérifie que l'utilisateur est toujours connecté
     * Utile si la session a expiré pendant que l'app était en arrière-plan
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Vérification de sécurité
        // Si l'admin s'est déconnecté, le redirige vers la page de login
        if (!LoginAdminFragment.isAdminLoggedIn(this)) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_admin_container, LoginAdminFragment.newInstance())
                    .commit();
        }
    }
}
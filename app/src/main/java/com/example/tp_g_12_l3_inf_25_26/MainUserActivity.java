package com.example.tp_g_12_l3_inf_25_26;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tp_g_12_l3_inf_25_26.ui.user.home.HomeUserFragment;
import com.example.tp_g_12_l3_inf_25_26.ui.user.loging.LoginUserFragment;

public class MainUserActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        if (savedInstanceState == null) {
            // Vérifier si l'utilisateur est déjà connecté
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);

            if (isLoggedIn) {
                // L'utilisateur est connecté, charger HomeUserFragment
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_user_container, HomeUserFragment.newInstance())
                        .commitNow();
            } else {
                // L'utilisateur n'est pas connecté, charger LoginUserFragment
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_user_container, LoginUserFragment.newInstance())
                        .commitNow();
            }
        }
    }
}
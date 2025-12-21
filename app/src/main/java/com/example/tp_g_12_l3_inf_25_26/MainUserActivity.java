package com.example.tp_g_12_l3_inf_25_26;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.tp_g_12_l3_inf_25_26.ui.user.home.HomeUserFragment;


public class MainUserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);
        if (savedInstanceState == null ) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_user_container, HomeUserFragment.newInstance())
                    .commitNow();
        }
    }
}
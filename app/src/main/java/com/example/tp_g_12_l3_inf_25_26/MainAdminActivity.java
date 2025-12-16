package com.example.tp_g_12_l3_inf_25_26;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.tp_g_12_l3_inf_25_26.ui.admin.home.HomeAdminFragment;
import com.example.tp_g_12_l3_inf_25_26.ui.admin.loging.LoginAdminFragment;

public class MainAdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_admin);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_admin_container, HomeAdminFragment.newInstance())
                    .commitNow();
        }
    }
}
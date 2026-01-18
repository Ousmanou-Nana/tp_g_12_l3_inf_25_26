package com.example.tp_g_12_l3_inf_25_26;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tp_g_12_l3_inf_25_26.DB.DatabaseSeeder;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DatabaseSeeder seeder = new DatabaseSeeder(this);
        seeder.seedDatabase();
        seeder.printDatabaseStats();

        Button adminButton = findViewById(R.id.buttonAdmin);
        Button userButton = findViewById(R.id.buttonUser);

        adminButton.setOnClickListener(v ->
                openActivity(MainAdminActivity.class)
        );

        userButton.setOnClickListener(v ->
                openActivity(MainUserActivity.class)
        );
    }

    private void openActivity(Class<?> target) {
        Intent intent = new Intent(this, target);
        startActivity(intent);
    }

}
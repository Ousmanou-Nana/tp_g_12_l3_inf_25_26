package com.example.tp_g_12_l3_inf_25_26.ui.user.loging;

import android.app.Application;
import android.database.Cursor;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tp_g_12_l3_inf_25_26.DB.DatabaseHelper;
import com.example.tp_g_12_l3_inf_25_26.models.LoginResult;

public class LogingViewModel extends AndroidViewModel {
    private final DatabaseHelper dbHelper;
    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public LogingViewModel(Application application) {
        super(application);
        dbHelper = new DatabaseHelper(application);
    }

    public LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void login(String name, String phone, String matricule) {
        isLoading.setValue(true);

        // Vérifier si l'utilisateur existe avec le matricule
        Cursor cursor = dbHelper.getUserByMatricule(matricule);

        if (cursor != null && cursor.moveToFirst()) {
            // L'utilisateur existe, vérifier les autres informations
            int idUserIndex = cursor.getColumnIndex("id_user");
            int nameIndex = cursor.getColumnIndex("name");
            int phoneIndex = cursor.getColumnIndex("phone");
            int matriculeIndex = cursor.getColumnIndex("matricule");

            if (idUserIndex != -1 && nameIndex != -1 && phoneIndex != -1 && matriculeIndex != -1) {
                int userId = cursor.getInt(idUserIndex);
                String dbName = cursor.getString(nameIndex);
                String dbPhone = cursor.getString(phoneIndex);
                String dbMatricule = cursor.getString(matriculeIndex);

                cursor.close();

                // Vérifier que le nom et le téléphone correspondent
                if (dbName.equalsIgnoreCase(name.trim()) && dbPhone.equals(phone.trim())) {
                    loginResult.setValue(new LoginResult(
                            true,
                            "Connexion réussie",
                            userId,
                            dbName,
                            dbMatricule
                    ));
                } else {
                    loginResult.setValue(new LoginResult(
                            false,
                            "Nom ou téléphone incorrect"
                    ));
                }
            } else {
                cursor.close();
                loginResult.setValue(new LoginResult(false, "Erreur de lecture des données"));
            }
        } else {
            if (cursor != null) {
                cursor.close();
            }

            // L'utilisateur n'existe pas, créer un nouveau compte
            long result = dbHelper.insertUser(name.trim(), phone.trim(), matricule.trim());

            if (result != -1) {
                // Récupérer l'utilisateur nouvellement créé
                Cursor newUserCursor = dbHelper.getUserByMatricule(matricule);
                if (newUserCursor != null && newUserCursor.moveToFirst()) {
                    int idUserIndex = newUserCursor.getColumnIndex("id_user");
                    if (idUserIndex != -1) {
                        int userId = newUserCursor.getInt(idUserIndex);
                        newUserCursor.close();

                        loginResult.setValue(new LoginResult(
                                true,
                                "Compte créé et connecté avec succès",
                                userId,
                                name.trim(),
                                matricule.trim()
                        ));
                    } else {
                        newUserCursor.close();
                        loginResult.setValue(new LoginResult(false, "Erreur lors de la création du compte"));
                    }
                } else {
                    if (newUserCursor != null) {
                        newUserCursor.close();
                    }
                    loginResult.setValue(new LoginResult(false, "Erreur lors de la création du compte"));
                }
            } else {
                loginResult.setValue(new LoginResult(false, "Erreur lors de la création du compte"));
            }
        }

        isLoading.setValue(false);
    }
}
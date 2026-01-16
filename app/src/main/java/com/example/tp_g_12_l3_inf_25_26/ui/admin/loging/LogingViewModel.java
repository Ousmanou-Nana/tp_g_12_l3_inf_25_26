package com.example.tp_g_12_l3_inf_25_26.ui.admin.loging;

import android.app.Application;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tp_g_12_l3_inf_25_26.DB.DatabaseHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LogingViewModel extends AndroidViewModel {

    private final MutableLiveData<String> username = new MutableLiveData<>();
    private final MutableLiveData<String> password = new MutableLiveData<>();
    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final DatabaseHelper databaseHelper;

    public LogingViewModel(@NonNull Application application) {
        super(application);
        databaseHelper = new DatabaseHelper(application);

        // Créer l'admin par défaut et un admin supplémentaire si nécessaire
        createDefaultAdminIfNeeded();
    }

    private void createDefaultAdminIfNeeded() {
        new Thread(() -> {
            Cursor cursor = databaseHelper.getAllAdmins();
            if (cursor != null && cursor.getCount() == 0) {
                // Créer un admin par défaut: username = "admin", password = "admin123"
                String hashedPassword = hashPassword("admin123");
                databaseHelper.insertAdmin("admin", hashedPassword);

                // Créer un deuxième admin si vous le souhaitez
                String hashedPassword2 = hashPassword("admin123");
                databaseHelper.insertAdmin("admin", hashedPassword2);
            }
            if (cursor != null) {
                cursor.close();
            }
        }).start();
    }

    public void setUsername(String username) {
        this.username.setValue(username);
    }

    public void setPassword(String password) {
        this.password.setValue(password);
    }

    public LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void login() {
        String user = username.getValue();
        String pass = password.getValue();

        // Validation des champs
        if (user == null || user.trim().isEmpty()) {
            loginResult.setValue(new LoginResult(false, "Nom d'utilisateur requis", -1));
            return;
        }

        if (pass == null || pass.trim().isEmpty()) {
            loginResult.setValue(new LoginResult(false, "Mot de passe requis", -1));
            return;
        }

        isLoading.setValue(true);

        // Authentification avec la base de données
        new Thread(() -> {
            try {
                Thread.sleep(500); // Simule un léger délai

                Cursor cursor = databaseHelper.getAdminByName(user);

                if (cursor != null && cursor.moveToFirst()) {
                    int idIndex = cursor.getColumnIndex("id_admin");
                    int passwordIndex = cursor.getColumnIndex("hashed_password");

                    if (idIndex != -1 && passwordIndex != -1) {
                        int adminId = cursor.getInt(idIndex);
                        String storedHashedPassword = cursor.getString(passwordIndex);
                        String inputHashedPassword = hashPassword(pass);

                        cursor.close();

                        if (storedHashedPassword.equals(inputHashedPassword)) {
                            loginResult.postValue(new LoginResult(true, "Connexion réussie", adminId));
                        } else {
                            loginResult.postValue(new LoginResult(false, "Identifiants incorrects", -1));
                        }
                    } else {
                        cursor.close();
                        loginResult.postValue(new LoginResult(false, "Erreur de base de données", -1));
                    }
                } else {
                    if (cursor != null) {
                        cursor.close();
                    }
                    loginResult.postValue(new LoginResult(false, "Utilisateur introuvable", -1));
                }

            } catch (InterruptedException e) {
                loginResult.postValue(new LoginResult(false, "Erreur de connexion", -1));
            } finally {
                isLoading.postValue(false);
            }
        }).start();
    }

    // Méthode pour hacher le mot de passe avec SHA-256
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password; // Fallback (non sécurisé, uniquement pour éviter un crash)
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Le DatabaseHelper sera fermé automatiquement par le garbage collector
    }

    // Classe interne pour représenter le résultat de la connexion
    public static class LoginResult {
        private final boolean success;
        private final String message;
        private final int adminId;

        public LoginResult(boolean success, String message, int adminId) {
            this.success = success;
            this.message = message;
            this.adminId = adminId;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public int getAdminId() {
            return adminId;
        }
    }
}
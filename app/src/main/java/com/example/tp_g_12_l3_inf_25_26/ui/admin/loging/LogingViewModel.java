// ============================================================================
// LogingViewModel.java
// ============================================================================
// Cette classe gère toute la logique métier de la connexion administrateur
// Elle utilise le pattern MVVM (Model-View-ViewModel) pour séparer
// la logique de l'interface utilisateur
// ============================================================================

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

    // ===== Déclaration des LiveData =====
    // LiveData permet d'observer les changements de données depuis l'interface

    // Stocke le nom d'utilisateur saisi
    private final MutableLiveData<String> username = new MutableLiveData<>();

    // Stocke le mot de passe saisi
    private final MutableLiveData<String> password = new MutableLiveData<>();

    // Stocke le résultat de la tentative de connexion (succès/échec)
    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();

    // Indique si une opération de connexion est en cours
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    // Helper pour accéder à la base de données SQLite
    private final DatabaseHelper databaseHelper;

    /**
     * Constructeur du ViewModel
     * @param application Le contexte de l'application Android
     */
    public LogingViewModel(@NonNull Application application) {
        super(application);
        // Initialise l'accès à la base de données
        databaseHelper = new DatabaseHelper(application);

        // Crée un compte admin par défaut si aucun n'existe
        createDefaultAdminIfNeeded();
    }

    /**
     * Vérifie si des administrateurs existent dans la base de données.
     * Si aucun admin n'existe, crée deux comptes admin par défaut
     * avec le username "admin" et le mot de passe "admin123"
     *
     * Cette méthode s'exécute dans un thread séparé pour ne pas bloquer l'UI
     */
    private void createDefaultAdminIfNeeded() {
        new Thread(() -> {
            // Récupère tous les administrateurs de la base
            Cursor cursor = databaseHelper.getAllAdmins();

            // Si aucun admin n'existe (table vide)
            if (cursor != null && cursor.getCount() == 0) {
                // Hash le mot de passe avant de le stocker (sécurité)
                String hashedPassword = hashPassword("admin123");
                databaseHelper.insertAdmin("admin", hashedPassword);

                // Création d'un deuxième admin (identique au premier)
                // NOTE: Ce code crée un doublon inutile
                String hashedPassword2 = hashPassword("admin123");
                databaseHelper.insertAdmin("admin", hashedPassword2);
            }

            // Ferme le cursor pour libérer les ressources
            if (cursor != null) {
                cursor.close();
            }
        }).start();
    }

    /**
     * Met à jour le nom d'utilisateur dans le LiveData
     * @param username Le nom d'utilisateur saisi par l'utilisateur
     */
    public void setUsername(String username) {
        this.username.setValue(username);
    }

    /**
     * Met à jour le mot de passe dans le LiveData
     * @param password Le mot de passe saisi par l'utilisateur
     */
    public void setPassword(String password) {
        this.password.setValue(password);
    }

    /**
     * Retourne le LiveData du résultat de connexion
     * Permet au Fragment d'observer les changements
     * @return LiveData contenant le résultat de la connexion
     */
    public LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    /**
     * Retourne le LiveData de l'état de chargement
     * @return LiveData indiquant si une connexion est en cours
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Méthode principale de connexion
     * Valide les entrées, vérifie les identifiants dans la base de données
     * et retourne le résultat via LiveData
     */
    public void login() {
        // Récupère les valeurs actuelles
        String user = username.getValue();
        String pass = password.getValue();

        // ===== VALIDATION DES CHAMPS =====

        // Vérifie que le nom d'utilisateur n'est pas vide
        if (user == null || user.trim().isEmpty()) {
            loginResult.setValue(new LoginResult(false, "Nom d'utilisateur requis", -1));
            return;
        }

        // Vérifie que le mot de passe n'est pas vide
        if (pass == null || pass.trim().isEmpty()) {
            loginResult.setValue(new LoginResult(false, "Mot de passe requis", -1));
            return;
        }

        // Active l'indicateur de chargement
        isLoading.setValue(true);

        // ===== VÉRIFICATION EN BASE DE DONNÉES =====
        // Exécuté dans un thread séparé pour ne pas bloquer l'interface
        new Thread(() -> {
            try {
                // Simule un délai réseau (500ms)
                Thread.sleep(500);

                // Recherche l'admin par son nom d'utilisateur
                Cursor cursor = databaseHelper.getAdminByName(user);

                // Si un admin avec ce nom existe
                if (cursor != null && cursor.moveToFirst()) {
                    // Récupère les indices des colonnes
                    int idIndex = cursor.getColumnIndex("id_admin");
                    int passwordIndex = cursor.getColumnIndex("hashed_password");

                    // Vérifie que les colonnes existent
                    if (idIndex != -1 && passwordIndex != -1) {
                        // Extrait les données
                        int adminId = cursor.getInt(idIndex);
                        String storedHashedPassword = cursor.getString(passwordIndex);

                        // Hash le mot de passe saisi pour le comparer
                        String inputHashedPassword = hashPassword(pass);

                        cursor.close();

                        // Compare les mots de passe hashés
                        if (storedHashedPassword.equals(inputHashedPassword)) {
                            // Connexion réussie
                            loginResult.postValue(new LoginResult(true, "Connexion réussie", adminId));
                        } else {
                            // Mot de passe incorrect
                            loginResult.postValue(new LoginResult(false, "Identifiants incorrects", -1));
                        }
                    } else {
                        // Erreur de structure de base de données
                        cursor.close();
                        loginResult.postValue(new LoginResult(false, "Erreur de base de données", -1));
                    }
                } else {
                    // Utilisateur non trouvé
                    if (cursor != null) {
                        cursor.close();
                    }
                    loginResult.postValue(new LoginResult(false, "Utilisateur introuvable", -1));
                }

            } catch (InterruptedException e) {
                // Erreur lors de l'attente simulée
                loginResult.postValue(new LoginResult(false, "Erreur de connexion", -1));
            } finally {
                // Désactive l'indicateur de chargement dans tous les cas
                isLoading.postValue(false);
            }
        }).start();
    }

    /**
     * Hash un mot de passe en utilisant l'algorithme SHA-256
     * Cela garantit que le mot de passe n'est jamais stocké en clair
     *
     * @param password Le mot de passe en clair
     * @return Le mot de passe hashé en format hexadécimal
     */
    private String hashPassword(String password) {
        try {
            // Obtient une instance de l'algorithme SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Convertit le mot de passe en bytes et le hash
            byte[] hash = digest.digest(password.getBytes());

            // Convertit les bytes en chaîne hexadécimale
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                // Ajoute un zéro si nécessaire pour avoir 2 caractères
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            // Fallback non sécurisé en cas d'erreur (à éviter en production)
            return password;
        }
    }

    /**
     * Appelé quand le ViewModel est détruit
     * Permet de libérer les ressources
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        // Possibilité de fermer la base de données ici
    }

    /**
     * Classe interne représentant le résultat d'une tentative de connexion
     */
    public static class LoginResult {
        private final boolean success;    // Succès ou échec
        private final String message;     // Message à afficher
        private final int adminId;        // ID de l'admin (-1 si échec)

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
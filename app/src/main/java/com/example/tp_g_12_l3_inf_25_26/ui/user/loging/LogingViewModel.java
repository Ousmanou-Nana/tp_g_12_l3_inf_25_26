package com.example.tp_g_12_l3_inf_25_26.ui.user.loging;

import android.app.Application;
import android.database.Cursor;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tp_g_12_l3_inf_25_26.DB.DatabaseHelper;
import com.example.tp_g_12_l3_inf_25_26.models.LoginResult;

/**
 * ViewModel pour la gestion de l'authentification utilisateur.
 *
 * Ce ViewModel gère la logique d'authentification avec une approche hybride:
 * - Vérification des utilisateurs existants
 * - Création automatique de nouveaux comptes si l'utilisateur n'existe pas
 *
 * Fonctionnalités principales:
 * - Authentification par matricule (identifiant unique)
 * - Validation des informations (nom, téléphone)
 * - Création automatique de compte pour les nouveaux utilisateurs
 * - Gestion de l'état de chargement
 * - Communication réactive via LiveData
 *
 * Architecture:
 * - Suit le pattern MVVM
 * - Opérations synchrones (peut être amélioré avec coroutines/threads)
 * - Communication unidirectionnelle vers l'UI via LiveData
 *
 * @author Votre équipe
 * @version 1.0
 */
public class LogingViewModel extends AndroidViewModel {

    // ==================== ATTRIBUTS ====================

    /**
     * Helper pour accéder à la base de données SQLite.
     * Gère toutes les opérations CRUD sur les utilisateurs.
     */
    private final DatabaseHelper dbHelper;

    /**
     * LiveData contenant le résultat de la tentative de connexion.
     * <p>
     * Encapsule:
     * - Le statut de succès/échec
     * - Le message pour l'utilisateur
     * - Les informations de l'utilisateur si succès
     */
    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();

    /**
     * LiveData indiquant si une opération de connexion est en cours.
     * <p>
     * Utilisé pour:
     * - Afficher/masquer un ProgressBar
     * - Désactiver les boutons pendant le traitement
     * - Améliorer l'expérience utilisateur
     */
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    // ==================== CONSTRUCTEUR ====================

    /**
     * Constructeur du ViewModel.
     * <p>
     * Initialise le DatabaseHelper pour accéder à la base de données.
     *
     * @param application Contexte de l'application (survit aux changements de configuration)
     */
    public LogingViewModel(Application application) {
        super(application);
        dbHelper = new DatabaseHelper(application);
    }

    // ==================== GETTERS POUR LIVEDATA ====================

    /**
     * Retourne le LiveData du résultat de connexion.
     * <p>
     * Les fragments observent ce LiveData pour réagir aux tentatives de connexion:
     * - Afficher des messages Toast
     * - Naviguer vers l'écran d'accueil en cas de succès
     * - Afficher des erreurs en cas d'échec
     *
     * @return LiveData observable contenant le résultat de connexion
     */
    public LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    /**
     * Retourne le LiveData de l'état de chargement.
     * <p>
     * Permet à l'UI de:
     * - Afficher une ProgressBar pendant le traitement
     * - Désactiver les boutons pour éviter les doubles soumissions
     * - Fournir un feedback visuel à l'utilisateur
     *
     * @return LiveData observable de l'état de chargement
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    // ==================== MÉTHODE DE CONNEXION ====================

    /**
     * Authentifie un utilisateur ou crée un nouveau compte automatiquement.
     * <p>
     * LOGIQUE D'AUTHENTIFICATION:
     * <p>
     * 1. RECHERCHE PAR MATRICULE (identifiant unique):
     * - Si l'utilisateur existe: vérifier nom et téléphone
     * - Si les infos correspondent: connexion réussie
     * - Si les infos diffèrent: erreur d'authentification
     * <p>
     * 2. SI L'UTILISATEUR N'EXISTE PAS:
     * - Création automatique d'un nouveau compte
     * - Connexion immédiate du nouvel utilisateur
     * <p>
     * Cette approche permet une expérience "sans inscription":
     * - Les nouveaux utilisateurs sont créés à la volée
     * - Les utilisateurs existants sont authentifiés
     * - Le matricule sert d'identifiant unique
     * <p>
     * NOTE: Cette méthode effectue des opérations synchrones sur le thread principal.
     * Pour une meilleure performance, envisager d'utiliser un thread séparé ou des coroutines.
     *
     * @param name      Nom de l'utilisateur
     * @param phone     Numéro de téléphone
     * @param matricule Matricule (identifiant unique de l'utilisateur)
     */
    public void login(String name, String phone, String matricule) {
        // Activation de l'état de chargement
        isLoading.setValue(true);

        // ========== PHASE 1: RECHERCHE DE L'UTILISATEUR ==========

        // Recherche de l'utilisateur par matricule (clé unique)
        Cursor cursor = dbHelper.getUserByMatricule(matricule);

        if (cursor != null && cursor.moveToFirst()) {
            // ========== CAS 1: UTILISATEUR EXISTANT ==========

            // Récupération des indices de colonnes (sécurisé)
            int idUserIndex = cursor.getColumnIndex("id_user");
            int nameIndex = cursor.getColumnIndex("name");
            int phoneIndex = cursor.getColumnIndex("phone");
            int matriculeIndex = cursor.getColumnIndex("matricule");

            // Vérification que toutes les colonnes existent
            if (idUserIndex != -1 && nameIndex != -1 && phoneIndex != -1 && matriculeIndex != -1) {
                // Extraction des données de la base
                int userId = cursor.getInt(idUserIndex);
                String dbName = cursor.getString(nameIndex);
                String dbPhone = cursor.getString(phoneIndex);
                String dbMatricule = cursor.getString(matriculeIndex);

                cursor.close(); // Fermeture immédiate du cursor

                // ========== VALIDATION DES INFORMATIONS ==========

                // Vérification que le nom et le téléphone correspondent
                // equalsIgnoreCase pour le nom (insensible à la casse)
                // equals pour le téléphone (sensible à la casse)
                if (dbName.equalsIgnoreCase(name.trim()) && dbPhone.equals(phone.trim())) {
                    // ✅ SUCCÈS: Toutes les informations correspondent
                    loginResult.setValue(new LoginResult(
                            true,                    // Succès
                            "Connexion réussie",     // Message
                            userId,                  // ID de l'utilisateur
                            dbName,                  // Nom depuis la BD
                            dbMatricule,             // Matricule
                            dbPhone                  // Téléphone
                    ));
                } else {
                    // ❌ ÉCHEC: Le matricule existe mais les autres infos ne correspondent pas
                    // Cela empêche l'usurpation d'identité
                    loginResult.setValue(new LoginResult(
                            false,
                            "Nom ou téléphone incorrect"
                    ));
                }
            } else {
                // ❌ ERREUR: Problème de structure de base de données
                cursor.close();
                loginResult.setValue(new LoginResult(false, "Erreur de lecture des données"));
            }
        } else {
            // ========== CAS 2: NOUVEL UTILISATEUR ==========

            // Fermeture du cursor s'il existe
            if (cursor != null) {
                cursor.close();
            }

            // L'utilisateur n'existe pas dans la base de données
            // Création automatique d'un nouveau compte
            long result = dbHelper.insertUser(name.trim(), phone.trim(), matricule.trim());

            if (result != -1) {
                // ========== COMPTE CRÉÉ AVEC SUCCÈS ==========

                // Récupération de l'utilisateur nouvellement créé pour obtenir son ID
                Cursor newUserCursor = dbHelper.getUserByMatricule(matricule);

                if (newUserCursor != null && newUserCursor.moveToFirst()) {
                    int idUserIndex = newUserCursor.getColumnIndex("id_user");

                    if (idUserIndex != -1) {
                        int userId = newUserCursor.getInt(idUserIndex);
                        newUserCursor.close();

                        // ✅ SUCCÈS: Compte créé et utilisateur connecté
                        loginResult.setValue(new LoginResult(
                                true,
                                "Compte créé et connecté avec succès",
                                userId,
                                name.trim(),
                                matricule.trim(),
                                phone.trim()
                        ));
                    } else {
                        // ❌ ERREUR: Impossible de lire l'ID du nouvel utilisateur
                        newUserCursor.close();
                        loginResult.setValue(new LoginResult(false, "Erreur lors de la création du compte"));
                    }
                } else {
                    // ❌ ERREUR: Impossible de récupérer l'utilisateur créé
                    if (newUserCursor != null) {
                        newUserCursor.close();
                    }
                    loginResult.setValue(new LoginResult(false, "Erreur lors de la création du compte"));
                }
            } else {
                // ❌ ERREUR: L'insertion dans la base de données a échoué
                // Peut arriver si le matricule existe déjà (contrainte UNIQUE)
                // ou en cas d'erreur SQL
                loginResult.setValue(new LoginResult(false, "Erreur lors de la création du compte"));
            }
        }

        // Désactivation de l'état de chargement
        isLoading.setValue(false);
    }
}
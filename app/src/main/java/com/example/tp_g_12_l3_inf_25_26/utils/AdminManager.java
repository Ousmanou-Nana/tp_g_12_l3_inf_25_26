package com.example.tp_g_12_l3_inf_25_26.utils;

import android.content.Context;
import android.database.Cursor;

import com.example.tp_g_12_l3_inf_25_26.DB.DatabaseHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Classe utilitaire pour gérer les opérations admin
 */
public class AdminManager {

    private final DatabaseHelper databaseHelper;

    public AdminManager(Context context) {
        this.databaseHelper = new DatabaseHelper(context);
    }

    /**
     * Crée un nouvel administrateur
     * @param name Nom d'utilisateur
     * @param password Mot de passe en clair
     * @return true si la création réussit
     */
    public boolean createAdmin(String name, String password) {
        if (name == null || name.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return false;
        }

        // Vérifier si l'admin existe déjà
        if (adminExists(name)) {
            return false;
        }

        String hashedPassword = hashPassword(password);
        return databaseHelper.insertAdmin(name, hashedPassword);
    }

    /**
     * Vérifie si un admin existe
     * @param name Nom d'utilisateur
     * @return true si l'admin existe
     */
    public boolean adminExists(String name) {
        Cursor cursor = databaseHelper.getAdminByName(name);
        boolean exists = cursor != null && cursor.moveToFirst();
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }

    /**
     * Change le mot de passe d'un admin
     * @param adminId ID de l'admin
     * @param newPassword Nouveau mot de passe en clair
     * @return true si la mise à jour réussit
     */
    public boolean changePassword(int adminId, String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return false;
        }

        String hashedPassword = hashPassword(newPassword);
        return databaseHelper.updateAdminPassword(adminId, hashedPassword);
    }

    /**
     * Supprime un administrateur
     * @param adminId ID de l'admin à supprimer
     * @return true si la suppression réussit
     */
    public boolean deleteAdmin(int adminId) {
        return databaseHelper.deleteAdmin(adminId);
    }

    /**
     * Récupère le nom d'un admin par son ID
     * @param adminId ID de l'admin
     * @return Le nom de l'admin ou null
     */
    public String getAdminName(int adminId) {
        Cursor cursor = databaseHelper.getAllAdmins();
        String name = null;

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int idIndex = cursor.getColumnIndex("id_admin");
                int nameIndex = cursor.getColumnIndex("name");

                if (idIndex != -1 && nameIndex != -1) {
                    if (cursor.getInt(idIndex) == adminId) {
                        name = cursor.getString(nameIndex);
                        break;
                    }
                }
            }
            cursor.close();
        }

        return name;
    }

    /**
     * Hache un mot de passe avec SHA-256
     * @param password Mot de passe en clair
     * @return Mot de passe haché
     */
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
            return password;
        }
    }

    /**
     * Récupère tous les admins
     * @return Cursor contenant tous les admins
     */
    public Cursor getAllAdmins() {
        return databaseHelper.getAllAdmins();
    }
}
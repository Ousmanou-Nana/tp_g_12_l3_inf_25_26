package com.example.tp_g_12_l3_inf_25_26.models;

import android.content.Context;
import android.database.Cursor;

import com.example.tp_g_12_l3_inf_25_26.DB.DatabaseHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe modèle représentant un objet trouvé/perdu
 */
public class Objet {
    private int idObjet;
    private String nomDeclarant;
    private String telephone;
    private String description;
    private int idType;
    private String nomType;
    private String dateDeclaration;
    private String statut;
    private int idAdmin;
    private List<String> cheminImages;

    // Constructeur par défaut
    public Objet() {
        this.cheminImages = new ArrayList<>();
    }

    // Constructeur complet
    public Objet(int idObjet, String nomDeclarant, String telephone,
                 String description, int idType, String nomType,
                 String dateDeclaration, String statut, int idAdmin) {
        this.idObjet = idObjet;
        this.nomDeclarant = nomDeclarant;
        this.telephone = telephone;
        this.description = description;
        this.idType = idType;
        this.nomType = nomType;
        this.dateDeclaration = dateDeclaration;
        this.statut = statut;
        this.idAdmin = idAdmin;
        this.cheminImages = new ArrayList<>();
    }

    // ==================== GETTERS ====================

    public int getIdObjet() {
        return idObjet;
    }

    public String getNomDeclarant() {
        return nomDeclarant;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getDescription() {
        return description;
    }

    public int getIdType() {
        return idType;
    }

    public String getNomType() {
        return nomType;
    }

    public String getDateDeclaration() {
        return dateDeclaration;
    }

    public String getStatut() {
        return statut;
    }

    public int getIdAdmin() {
        return idAdmin;
    }

    public List<String> getCheminImages() {
        return cheminImages;
    }

    // ==================== SETTERS ====================

    public void setIdObjet(int idObjet) {
        this.idObjet = idObjet;
    }

    public void setNomDeclarant(String nomDeclarant) {
        this.nomDeclarant = nomDeclarant;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIdType(int idType) {
        this.idType = idType;
    }

    public void setNomType(String nomType) {
        this.nomType = nomType;
    }

    public void setDateDeclaration(String dateDeclaration) {
        this.dateDeclaration = dateDeclaration;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public void setIdAdmin(int idAdmin) {
        this.idAdmin = idAdmin;
    }

    public void setCheminImages(List<String> cheminImages) {
        this.cheminImages = cheminImages;
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Ajoute un chemin d'image à la liste
     */
    public void addCheminImage(String cheminImage) {
        if (this.cheminImages == null) {
            this.cheminImages = new ArrayList<>();
        }
        this.cheminImages.add(cheminImage);
    }

    /**
     * Supprime un chemin d'image de la liste
     */
    public void removeCheminImage(String cheminImage) {
        if (this.cheminImages != null) {
            this.cheminImages.remove(cheminImage);
        }
    }

    /**
     * Vérifie si l'objet a des images
     */
    public boolean hasImages() {
        return cheminImages != null && !cheminImages.isEmpty();
    }

    /**
     * Retourne le nombre d'images
     */
    public int getImageCount() {
        return cheminImages != null ? cheminImages.size() : 0;
    }

    /**
     * Vérifie si l'objet est en attente
     */
    public boolean isEnAttente() {
        return "En attente".equals(statut);
    }

    /**
     * Vérifie si l'objet est validé
     */
    public boolean isValide() {
        return "Validé".equals(statut);
    }

    /**
     * Vérifie si l'objet a été récupéré
     */
    public boolean isRecupere() {
        return "Récupéré".equals(statut);
    }

    /**
     * Vérifie si l'objet a été rejeté
     */
    public boolean isRejete() {
        return "Rejeté".equals(statut);
    }

    @Override
    public String toString() {
        return "Objet{" +
                "idObjet=" + idObjet +
                ", nomDeclarant='" + nomDeclarant + '\'' +
                ", telephone='" + telephone + '\'' +
                ", nomType='" + nomType + '\'' +
                ", statut='" + statut + '\'' +
                ", dateDeclaration='" + dateDeclaration + '\'' +
                ", nbImages=" + getImageCount() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Objet objet = (Objet) o;
        return idObjet == objet.idObjet;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idObjet);
    }

    /**
     * Classe utilitaire pour gérer les opérations admin
     */
    public static class AdminManager {

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
}
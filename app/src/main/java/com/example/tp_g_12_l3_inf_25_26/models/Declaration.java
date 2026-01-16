package com.example.tp_g_12_l3_inf_25_26.models;

import java.util.ArrayList;
import java.util.List;

public class Declaration {
    private int idDeclaration;
    private int idUser;
    private String userName;
    private String userPhone;
    private String userMatricule;
    private String description;
    private int idType;
    private String nomType;
    private String dateDeclaration;
    private String statut;
    private int idAdmin;
    private List<String> cheminImages;

    public Declaration() {
        this.cheminImages = new ArrayList<>();
    }

    // Getters and Setters
    public int getIdDeclaration() {
        return idDeclaration;
    }

    public void setIdDeclaration(int idDeclaration) {
        this.idDeclaration = idDeclaration;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserMatricule() {
        return userMatricule;
    }

    public void setUserMatricule(String userMatricule) {
        this.userMatricule = userMatricule;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getIdType() {
        return idType;
    }

    public void setIdType(int idType) {
        this.idType = idType;
    }

    public String getNomType() {
        return nomType;
    }

    public void setNomType(String nomType) {
        this.nomType = nomType;
    }

    public String getDateDeclaration() {
        return dateDeclaration;
    }

    public void setDateDeclaration(String dateDeclaration) {
        this.dateDeclaration = dateDeclaration;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public int getIdAdmin() {
        return idAdmin;
    }

    public void setIdAdmin(int idAdmin) {
        this.idAdmin = idAdmin;
    }

    public List<String> getCheminImages() {
        return cheminImages;
    }

    public void setCheminImages(List<String> cheminImages) {
        this.cheminImages = cheminImages;
    }

    public void addCheminImage(String cheminImage) {
        this.cheminImages.add(cheminImage);
    }
}
package com.example.tp_g_12_l3_inf_25_26.models;

public class User {
    private int idUser;
    private String name;
    private String phone;
    private String matricule;

    public User() {
    }

    public User(int idUser, String name, String phone, String matricule) {
        this.idUser = idUser;
        this.name = name;
        this.phone = phone;
        this.matricule = matricule;
    }

    // Getters and Setters
    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }
}
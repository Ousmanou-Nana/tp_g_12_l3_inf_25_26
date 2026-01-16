package com.example.tp_g_12_l3_inf_25_26.models;

public class LoginResult {
    private boolean success;
    private String message;
    private int userId;
    private String userName;
    private String matricule;

    public LoginResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public LoginResult(boolean success, String message, int userId, String userName, String matricule) {
        this.success = success;
        this.message = message;
        this.userId = userId;
        this.userName = userName;
        this.matricule = matricule;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getMatricule() {
        return matricule;
    }
}
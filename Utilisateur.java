package com.livres.model;

import java.time.LocalDateTime;

public class Utilisateur {
    private int id;
    private String login;
    private String motDePasseHash;
    private String salt;
    private String nom;
    private LocalDateTime dateCreation;

    public Utilisateur() {}

    public Utilisateur(String login, String nom) {
        this.login = login;
        this.nom = nom;
    }

    // ----- Getters / Setters -----
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getMotDePasseHash() { return motDePasseHash; }
    public void setMotDePasseHash(String h) { this.motDePasseHash = h; }

    public String getSalt() { return salt; }
    public void setSalt(String s) { this.salt = s; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime d) { this.dateCreation = d; }

    @Override
    public String toString() {
        return "Utilisateur{id=" + id + ", login='" + login + "', nom='" + nom + "'}";
    }
}
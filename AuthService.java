package com.livres.service;

import com.livres.dao.UtilisateurDAO;
import com.livres.model.Utilisateur;
import com.livres.security.PasswordHasher;

public class AuthService {

    private final UtilisateurDAO dao = new UtilisateurDAO();

    public Utilisateur connecter(String login, String motDePasse) {
        if (login == null || motDePasse == null) {
            throw new SecurityException("Login et mot de passe requis");
        }

        Utilisateur u = dao.findByLogin(login)
            .orElseThrow(() -> new SecurityException("Identifiants invalides"));

        if (!PasswordHasher.verifier(motDePasse, u.getSalt(), u.getMotDePasseHash())) {
            throw new SecurityException("Identifiants invalides");
        }

        return u;
    }

    public void creerAdmin(String login, String motDePasse, String nom) {
        if (dao.findByLogin(login).isPresent()) {
            throw new IllegalArgumentException("Login déjà utilisé");
        }
        String salt = PasswordHasher.genererSalt();
        String hash = PasswordHasher.hasher(motDePasse, salt);

        Utilisateur u = new Utilisateur(login, nom);
        u.setSalt(salt);
        u.setMotDePasseHash(hash);

        dao.save(u);
    }
}
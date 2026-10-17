package com.livres.security;

import com.livres.model.Utilisateur;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Gère les sessions HTTP (login/logout).
 */
public class SessionManager {

    private static final String SESSION_KEY = "utilisateur";

    public static void login(HttpServletRequest req, Utilisateur u) {
        // Invalide l'ancienne session si elle existe
        HttpSession old = req.getSession(false);
        if (old != null) old.invalidate();

        HttpSession session = req.getSession(true);
        session.setAttribute(SESSION_KEY, u);
        session.setMaxInactiveInterval(30 * 60); // 30 min
    }

    public static void logout(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) session.invalidate();
    }

    public static Utilisateur getUtilisateur(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return null;
        return (Utilisateur) session.getAttribute(SESSION_KEY);
    }

    public static boolean estConnecte(HttpServletRequest req) {
        return getUtilisateur(req) != null;
    }
}
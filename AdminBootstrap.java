package com.livres.servlet;

import com.livres.service.AuthService;
import com.livres.dao.UtilisateurDAO;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Crée le compte administrateur au démarrage s'il n'existe pas encore.
 *
 * Variables d'environnement :
 *   ADMIN_PASSWORD  mot de passe de l'admin (obligatoire pour que la création ait lieu)
 *   ADMIN_LOGIN     identifiant (défaut : admin)
 *   ADMIN_NOM       nom affiché (défaut : Administrateur)
 *
 * Le hash et le salt sont calculés par l'application avec PasswordHasher : rien à générer à la main.
 * Si le compte existe déjà, rien n'est modifié.
 */
@WebListener
public class AdminBootstrap implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String password = System.getenv("ADMIN_PASSWORD");
        if (password == null || password.isEmpty()) return;

        String login = valeur("ADMIN_LOGIN", "admin");
        String nom = valeur("ADMIN_NOM", "Administrateur");

        try {
            if (new UtilisateurDAO().findByLogin(login).isPresent()) {
                System.out.println("[AdminBootstrap] Le compte '" + login + "' existe déjà, rien à faire.");
                return;
            }
            new AuthService().creerAdmin(login, password, nom);
            System.out.println("[AdminBootstrap] Compte '" + login + "' créé.");
        } catch (Exception e) {
            // Ne bloque jamais le démarrage de l'application
            System.err.println("[AdminBootstrap] Impossible de créer le compte admin : " + e.getMessage());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {}

    private static String valeur(String nom, String defaut) {
        String v = System.getenv(nom);
        return (v == null || v.trim().isEmpty()) ? defaut : v.trim();
    }
}

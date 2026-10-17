package com.livres.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe utilitaire pour obtenir une connexion à la base MySQL de XAMPP.
 */
public class Database {

    private static final String URL =
        "jdbc:mysql://localhost:3306/livres_db" +
        "?useSSL=false" +
        "&serverTimezone=UTC" +
        "&characterEncoding=UTF-8" +
        "&allowPublicKeyRetrieval=true";

    private static final String USER = "root";
    private static final String PASSWORD = "";  // vide par défaut dans XAMPP

    static {
        try {
            // Charge le driver MySQL (nécessaire avec certains serveurs)
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver MySQL introuvable !", e);
        }
    }

    /**
     * Retourne une nouvelle connexion à la base.
     * Toujours fermer la connexion après usage (try-with-resources).
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Petit test de connexion (à lancer une fois pour vérifier).
     */
    public static void main(String[] args) {
        try (Connection c = getConnection()) {
            System.out.println("✅ Connexion MySQL réussie !");
            System.out.println("   Base : " + c.getCatalog());
        } catch (SQLException e) {
            System.err.println("❌ Échec connexion : " + e.getMessage());
        }
    }
}
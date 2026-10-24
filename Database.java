package com.livres.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Accès à la base PostgreSQL (Supabase) via un petit pool de connexions.
 *
 * Configuration par variables d'environnement (jamais de mot de passe dans le code) :
 *   DB_URL       ex. jdbc:postgresql://aws-0-eu-west-1.pooler.supabase.com:5432/postgres?sslmode=require
 *   DB_USER      ex. postgres.xxxxxxxxxxxx
 *   DB_PASSWORD  mot de passe de la base
 */
public class Database {

    private static final HikariDataSource DS = creerPool();

    private static HikariDataSource creerPool() {
        String url = System.getenv("DB_URL");
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalStateException(
                "Variable d'environnement DB_URL manquante (ex. jdbc:postgresql://hote:5432/postgres?sslmode=require)");
        }

        HikariConfig cfg = new HikariConfig();
        cfg.setDriverClassName("org.postgresql.Driver");
        cfg.setJdbcUrl(url.trim());

        String user = System.getenv("DB_USER");
        String pass = System.getenv("DB_PASSWORD");
        if (user != null && !user.isEmpty()) cfg.setUsername(user);
        if (pass != null && !pass.isEmpty()) cfg.setPassword(pass);

        // Pool volontairement petit : le Session pooler de Supabase (offre gratuite) limite les connexions.
        cfg.setMaximumPoolSize(5);
        cfg.setMinimumIdle(1);
        cfg.setConnectionTimeout(15_000);
        cfg.setIdleTimeout(60_000);
        cfg.setMaxLifetime(300_000);
        // Ne pas faire échouer le démarrage si la base est momentanément injoignable.
        cfg.setInitializationFailTimeout(-1);
        cfg.setPoolName("livres-pool");

        return new HikariDataSource(cfg);
    }

    /**
     * Retourne une connexion du pool.
     * Toujours la fermer après usage (try-with-resources) : close() la rend au pool.
     */
    public static Connection getConnection() throws SQLException {
        return DS.getConnection();
    }

    /** Petit test de connexion : mvn exec ou java -cp ... com.livres.dao.Database */
    public static void main(String[] args) {
        try (Connection c = getConnection()) {
            System.out.println("Connexion PostgreSQL réussie, base : " + c.getCatalog());
        } catch (SQLException e) {
            System.err.println("Échec connexion : " + e.getMessage());
        } finally {
            DS.close();
        }
    }
}

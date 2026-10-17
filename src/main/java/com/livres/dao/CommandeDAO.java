package com.livres.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CommandeDAO {

    public void enregistrer(String emailClient, String titreLivre, String source) {
        String sql = "INSERT INTO commandes (email_client, statut) VALUES (?, 'EN_ATTENTE')";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, emailClient);
            ps.executeUpdate();
            System.out.println("✅ Commande enregistrée pour " + titreLivre);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
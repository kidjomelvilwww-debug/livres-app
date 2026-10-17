package com.livres.dao;

import com.livres.model.Utilisateur;

import java.sql.*;
import java.util.Optional;

public class UtilisateurDAO {

    public Optional<Utilisateur> findByLogin(String login) {
        String sql = "SELECT * FROM utilisateurs WHERE login = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    public Optional<Utilisateur> findById(int id) {
        String sql = "SELECT * FROM utilisateurs WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    public void save(Utilisateur u) {
        String sql = "INSERT INTO utilisateurs (login, mot_de_passe_hash, salt, nom) VALUES (?, ?, ?, ?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getLogin());
            ps.setString(2, u.getMotDePasseHash());
            ps.setString(3, u.getSalt());
            ps.setString(4, u.getNom());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) u.setId(rs.getInt(1));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Utilisateur map(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setId(rs.getInt("id"));
        u.setLogin(rs.getString("login"));
        u.setMotDePasseHash(rs.getString("mot_de_passe_hash"));
        u.setSalt(rs.getString("salt"));
        u.setNom(rs.getString("nom"));
        Timestamp ts = rs.getTimestamp("date_creation");
        if (ts != null) u.setDateCreation(ts.toLocalDateTime());
        return u;
    }
}
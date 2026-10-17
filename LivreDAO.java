package com.livres.dao;

import com.livres.model.Livre;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CRUD sur la table "livres".
 */
public class LivreDAO {

    // ----- CREATE -----
    public void save(Livre l) {
        String sql = "INSERT INTO livres " +
            "(titre, auteur, domaine, annee, isbn, editeur, description, image_url, fichier_url, source) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, l.getTitre());
            ps.setString(2, l.getAuteur());
            ps.setString(3, l.getDomaine());
            if (l.getAnnee() != null) ps.setInt(4, l.getAnnee());
            else ps.setNull(4, Types.INTEGER);
            ps.setString(5, l.getIsbn());
            ps.setString(6, l.getEditeur());
            ps.setString(7, l.getDescription());
            ps.setString(8, l.getImageUrl());
            ps.setString(9, l.getFichierUrl());
            ps.setString(10, l.getSource() != null ? l.getSource() : "LOCAL");

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) l.setId(rs.getInt(1));

            System.out.println("✅ Livre inséré, id=" + l.getId());
        } catch (SQLException e) {
            throw new RuntimeException("Erreur save livre", e);
        }
    }

    // ----- READ (par id) -----
    public Optional<Livre> findById(int id) {
        String sql = "SELECT * FROM livres WHERE id = ?";
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

    // ----- READ (tous) -----
    public List<Livre> findAll() {
        String sql = "SELECT * FROM livres ORDER BY id DESC";
        List<Livre> liste = new ArrayList<>();

        try (Connection c = Database.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) liste.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return liste;
    }

    // ----- UPDATE -----
    public void update(Livre l) {
        String sql = "UPDATE livres SET titre=?, auteur=?, domaine=?, annee=?, " +
            "isbn=?, editeur=?, description=?, image_url=?, fichier_url=? WHERE id=?";

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, l.getTitre());
            ps.setString(2, l.getAuteur());
            ps.setString(3, l.getDomaine());
            if (l.getAnnee() != null) ps.setInt(4, l.getAnnee());
            else ps.setNull(4, Types.INTEGER);
            ps.setString(5, l.getIsbn());
            ps.setString(6, l.getEditeur());
            ps.setString(7, l.getDescription());
            ps.setString(8, l.getImageUrl());
            ps.setString(9, l.getFichierUrl());
            ps.setInt(10, l.getId());

            ps.executeUpdate();
            System.out.println("✅ Livre mis à jour, id=" + l.getId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ----- DELETE -----
    public void delete(int id) {
        String sql = "DELETE FROM livres WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Livre supprimé, id=" + id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ----- Utilitaire : ResultSet → Livre -----
    private Livre map(ResultSet rs) throws SQLException {
        Livre l = new Livre();
        l.setId(rs.getInt("id"));
        l.setTitre(rs.getString("titre"));
        l.setAuteur(rs.getString("auteur"));
        l.setDomaine(rs.getString("domaine"));

        int annee = rs.getInt("annee");
        l.setAnnee(rs.wasNull() ? null : annee);

        l.setIsbn(rs.getString("isbn"));
        l.setEditeur(rs.getString("editeur"));
        l.setDescription(rs.getString("description"));
        l.setImageUrl(rs.getString("image_url"));
        l.setFichierUrl(rs.getString("fichier_url"));
        l.setSource(rs.getString("source"));

        Timestamp ts = rs.getTimestamp("date_ajout");
        if (ts != null) l.setDateAjout(ts.toLocalDateTime());

        return l;
    }
}
package com.livres.dao;

import com.livres.model.Livre;

public class TestInsert {
    public static void main(String[] args) {
        System.out.println("=== Test de connexion MySQL ===\n");

        LivreDAO dao = new LivreDAO();

        // 1. Insérer un livre
        Livre l = new Livre("Le Petit Prince", "Antoine de Saint-Exupéry", "Roman", 1943);
        l.setEditeur("Gallimard");
        l.setDescription("Un classique de la littérature française.");
        l.setIsbn("978-2070612758");
        l.setImageUrl("https://covers.openlibrary.org/b/isbn/9782070612758-L.jpg");

        dao.save(l);

        // 2. Lire tous les livres
        System.out.println("\n📚 Tous les livres en base :");
        for (Livre livre : dao.findAll()) {
            System.out.println("  " + livre);
        }

        System.out.println("\n✅ Test terminé !");
    }
}
package com.livres.external;

import com.livres.model.SearchResult;
import java.util.List;

public class TestOpenLibrary {
    public static void main(String[] args) {
        System.out.println("=== Test de connexion à Open Library ===");
        OpenLibraryClient client = new OpenLibraryClient();
        List<SearchResult> results = client.rechercher("petit prince", 5);
        System.out.println("Résultats trouvés : " + results.size());
        for (SearchResult r : results) {
            System.out.println("  - " + r.getTitre() + " (" + r.getAuteur() + ")");
        }
        System.out.println("=== Fin du test ===");
    }
}
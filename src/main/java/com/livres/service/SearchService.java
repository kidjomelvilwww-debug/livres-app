package com.livres.service;

import com.livres.dao.LivreDAO;
import com.livres.external.GoogleBooksClient;
import com.livres.external.OpenLibraryClient;
import com.livres.model.Livre;
import com.livres.model.SearchResult;

import java.util.ArrayList;
import java.util.List;

public class SearchService {

    private final LivreDAO dao = new LivreDAO();
    private final OpenLibraryClient openLibraryClient = new OpenLibraryClient();
    private final GoogleBooksClient googleBooksClient = new GoogleBooksClient();

    public List<SearchResult> rechercher(String query, String domaine, Integer anneeMin, Integer anneeMax) {
        System.out.println("=== [SearchService] Recherche '" + query + "' ===");

        List<SearchResult> resultats = new ArrayList<>();

        // ----- 1. Recherche locale -----
        List<Livre> livresLocaux = dao.findAll();
        for (Livre l : livresLocaux) {
            if (correspond(l, query, domaine, anneeMin, anneeMax)) {
                resultats.add(convertirLocal(l));
            }
        }
        System.out.println("[SearchService] Local : " + resultats.size() + " résultat(s)");

        // ----- 2. Recherche externe (Open Library → fallback Google Books) -----
        if (query != null && !query.trim().isEmpty()) {
            List<SearchResult> externes = new ArrayList<>();

            // a) Open Library
            try {
                externes = openLibraryClient.rechercher(query, 30);
                System.out.println("[SearchService] Open Library : " + externes.size() + " résultat(s)");
            } catch (Exception e) {
                System.err.println("[SearchService] Open Library échoué : " + e.getMessage());
            }

            // b) Fallback Google Books
            if (externes.isEmpty()) {
                System.out.println("[SearchService] → Fallback Google Books...");
                try {
                    externes = googleBooksClient.rechercher(query, 30);
                    System.out.println("[SearchService] Google Books : " + externes.size() + " résultat(s)");
                } catch (Exception e) {
                    System.err.println("[SearchService] Google Books échoué : " + e.getMessage());
                }
            }

            // c) Ajout des résultats filtrés
            for (SearchResult r : externes) {
                if (correspondGoogle(r, domaine, anneeMin, anneeMax)) {
                    resultats.add(r);
                }
            }
        }

        System.out.println("=== [SearchService] TOTAL : " + resultats.size() + " ===\n");
        return resultats;
    }

    private boolean correspond(Livre l, String query, String domaine, Integer anneeMin, Integer anneeMax) {
        if (query != null && !query.trim().isEmpty()) {
            String q = query.toLowerCase();
            boolean match = false;
            if (l.getTitre() != null && l.getTitre().toLowerCase().contains(q)) match = true;
            if (l.getAuteur() != null && l.getAuteur().toLowerCase().contains(q)) match = true;
            if (l.getIsbn() != null && l.getIsbn().contains(q)) match = true;
            if (!match) return false;
        }
        if (domaine != null && !domaine.isEmpty() && !domaine.equals(l.getDomaine())) return false;
        if (anneeMin != null && l.getAnnee() != null && l.getAnnee() < anneeMin) return false;
        if (anneeMax != null && l.getAnnee() != null && l.getAnnee() > anneeMax) return false;
        return true;
    }

    private boolean correspondGoogle(SearchResult r, String domaine, Integer anneeMin, Integer anneeMax) {
        if (domaine != null && !domaine.isEmpty()) {
            if (r.getDomaine() == null || !r.getDomaine().toLowerCase().contains(domaine.toLowerCase())) return false;
        }
        if (anneeMin != null && r.getAnnee() != null && r.getAnnee() < anneeMin) return false;
        if (anneeMax != null && r.getAnnee() != null && r.getAnnee() > anneeMax) return false;
        return true;
    }

    private SearchResult convertirLocal(Livre l) {
        SearchResult r = new SearchResult();
        r.setSource("LOCAL");
        r.setLocalId(l.getId());
        r.setTitre(l.getTitre());
        r.setAuteur(l.getAuteur());
        r.setDomaine(l.getDomaine());
        r.setAnnee(l.getAnnee());
        r.setIsbn(l.getIsbn());
        r.setEditeur(l.getEditeur());
        r.setDescription(l.getDescription());
        r.setImageUrl(l.getImageUrl());
        r.setFichierUrl(l.getFichierUrl());
        if (l.getIsbn() != null && !l.getIsbn().isEmpty()) {
            r.setBuyLink("https://www.amazon.fr/s?k=" + l.getIsbn());
        }
        return r;
    }
}
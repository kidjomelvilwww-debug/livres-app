package com.livres.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livres.model.SearchResult;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Client HTTP pour Open Library API.
 * Endpoint : https://openlibrary.org/search.json?q=...
 * Gratuit, sans clé API, sans quota.
 */
public class OpenLibraryClient {

    private static final String BASE_URL = "https://openlibrary.org/search.json";
    private final ObjectMapper mapper = new ObjectMapper();

    public List<SearchResult> rechercher(String query, int maxResults) {
        List<SearchResult> results = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            return results;
        }

        try {
            String encoded = URLEncoder.encode(query, "UTF-8");
            String urlStr = BASE_URL + "?q=" + encoded + "&limit=" + maxResults;

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", "LivresApp/1.0 (Educational Project)");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);

            int status = conn.getResponseCode();
            if (status != 200) {
                System.err.println("Open Library API error: " + status);
                return results;
            }

            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            }

            JsonNode root = mapper.readTree(sb.toString());
            JsonNode docs = root.get("docs");

            if (docs != null && docs.isArray()) {
                for (JsonNode item : docs) {
                    SearchResult r = convertir(item);
                    if (r != null) results.add(r);
                }
            }

            System.out.println("[OpenLibrary] '" + query + "' → " + results.size() + " résultats");

        } catch (Exception e) {
            System.err.println("Erreur Open Library API: " + e.getMessage());
            e.printStackTrace();
        }

        return results;
    }

    private SearchResult convertir(JsonNode item) {
        try {
            SearchResult r = new SearchResult();
            r.setSource("OPEN_LIBRARY");

            // Clé Open Library
            if (item.has("key")) {
                r.setGoogleId(item.get("key").asText()); // réutilise le champ googleId
            }

            // Titre
            if (item.has("title")) {
                r.setTitre(item.get("title").asText());
            }

            // Auteurs
            if (item.has("author_name") && item.get("author_name").isArray()) {
                JsonNode authors = item.get("author_name");
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < Math.min(authors.size(), 3); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(authors.get(i).asText());
                }
                r.setAuteur(sb.toString());
            }

            // Année (première publication)
            if (item.has("first_publish_year")) {
                r.setAnnee(item.get("first_publish_year").asInt());
            }

            // Éditeur
            if (item.has("publisher") && item.get("publisher").isArray() && item.get("publisher").size() > 0) {
                r.setEditeur(item.get("publisher").get(0).asText());
            }

            // ISBN (pour construire le lien Amazon)
            String isbn = null;
            if (item.has("isbn") && item.get("isbn").isArray() && item.get("isbn").size() > 0) {
                isbn = item.get("isbn").get(0).asText();
                r.setIsbn(isbn);
            }

            // Couverture
            if (item.has("cover_i")) {
                long coverId = item.get("cover_i").asLong();
                r.setImageUrl("https://covers.openlibrary.org/b/id/" + coverId + "-L.jpg");
            } else if (isbn != null) {
                // Fallback : couverture via ISBN
                r.setImageUrl("https://covers.openlibrary.org/b/isbn/" + isbn + "-L.jpg");
            }

            // Lien Amazon via ISBN
            if (isbn != null) {
                r.setBuyLink("https://www.amazon.fr/s/" + isbn);
            }

            // Lien Open Library (aperçu)
            if (item.has("key")) {
                String key = item.get("key").asText();
                r.setFichierUrl("https://openlibrary.org" + key);
            }

            // Sujet (domaine) - premier sujet
            if (item.has("subject") && item.get("subject").isArray() && item.get("subject").size() > 0) {
                r.setDomaine(item.get("subject").get(0).asText());
            }

            return r;
        } catch (Exception e) {
            System.err.println("Erreur parsing: " + e.getMessage());
            return null;
        }
    }
}
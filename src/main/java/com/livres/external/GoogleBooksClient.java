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
 * Client HTTP pour Google Books API.
 * Endpoint : https://www.googleapis.com/books/v1/volumes?q=...
 * Pas besoin de clé API pour les recherches basiques (quota 1000/jour).
 */
public class GoogleBooksClient {

    private static final String BASE_URL = "https://www.googleapis.com/books/v1/volumes";
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Recherche des livres sur Google Books.
     * @param query Mot-clé (titre, auteur, ISBN...)
     * @param maxResults Nombre max de résultats (1-40)
     * @return Liste de SearchResult
     */
    public List<SearchResult> rechercher(String query, int maxResults) {
        List<SearchResult> results = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            return results;
        }

        try {
            String encoded = URLEncoder.encode(query, "UTF-8");
            String urlStr = BASE_URL + "?q=" + encoded + "&maxResults=" + maxResults + "&langRestrict=fr";

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int status = conn.getResponseCode();
            if (status != 200) {
                System.err.println("Google Books API error: " + status);
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
            JsonNode items = root.get("items");

            if (items != null && items.isArray()) {
                for (JsonNode item : items) {
                    SearchResult r = convertir(item);
                    if (r != null) results.add(r);
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur Google Books API: " + e.getMessage());
        }

        return results;
    }

    private SearchResult convertir(JsonNode item) {
        try {
            SearchResult r = new SearchResult();
            r.setSource("GOOGLE_BOOKS");
            r.setGoogleId(item.get("id").asText());

            JsonNode volumeInfo = item.get("volumeInfo");
            if (volumeInfo == null) return null;

            // Titre
            if (volumeInfo.has("title")) {
                r.setTitre(volumeInfo.get("title").asText());
            }

            // Auteurs
            if (volumeInfo.has("authors") && volumeInfo.get("authors").isArray()) {
                JsonNode authors = volumeInfo.get("authors");
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < authors.size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(authors.get(i).asText());
                }
                r.setAuteur(sb.toString());
            }

            // Année
            if (volumeInfo.has("publishedDate")) {
                String date = volumeInfo.get("publishedDate").asText();
                if (date.length() >= 4) {
                    try {
                        r.setAnnee(Integer.parseInt(date.substring(0, 4)));
                    } catch (NumberFormatException ignored) {}
                }
            }

            // Éditeur
            if (volumeInfo.has("publisher")) {
                r.setEditeur(volumeInfo.get("publisher").asText());
            }

            // Description
            if (volumeInfo.has("description")) {
                r.setDescription(volumeInfo.get("description").asText());
            }

            // Image de couverture
            if (volumeInfo.has("imageLinks")) {
                JsonNode img = volumeInfo.get("imageLinks");
                if (img.has("thumbnail")) {
                    // Force HTTPS
                    String thumb = img.get("thumbnail").asText().replace("http://", "https://");
                    r.setImageUrl(thumb);
                }
            }

            // Catégories (domaine)
            if (volumeInfo.has("categories") && volumeInfo.get("categories").isArray()) {
                JsonNode cats = volumeInfo.get("categories");
                if (cats.size() > 0) {
                    r.setDomaine(cats.get(0).asText());
                }
            }

            // ISBN
            if (volumeInfo.has("industryIdentifiers")) {
                JsonNode ids = volumeInfo.get("industryIdentifiers");
                for (JsonNode id : ids) {
                    if ("ISBN_13".equals(id.get("type").asText())) {
                        r.setIsbn(id.get("identifier").asText());
                        break;
                    }
                }
            }

            // Lien d'achat
            if (item.has("saleInfo")) {
                JsonNode saleInfo = item.get("saleInfo");
                if (saleInfo.has("buyLink")) {
                    r.setBuyLink(saleInfo.get("buyLink").asText());
                }
            }

            // Lien direct Google Books
            if (volumeInfo.has("infoLink")) {
                r.setFichierUrl(volumeInfo.get("infoLink").asText());
            }

            return r;
        } catch (Exception e) {
            return null;
        }
    }
}
package com.livres.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.livres.model.SearchResult;
import com.livres.service.SearchService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * API de recherche fusionnée.
 *
 *   GET /api/search?q=harry&domaine=Roman&anneeMin=2000&anneeMax=2024
 */
@WebServlet("/api/search")
public class SearchServlet extends HttpServlet {

    private final SearchService service = new SearchService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");

        try {
            String q = req.getParameter("q");
            String domaine = req.getParameter("domaine");
            String anneeMinStr = req.getParameter("anneeMin");
            String anneeMaxStr = req.getParameter("anneeMax");

            Integer anneeMin = parseOrNull(anneeMinStr);
            Integer anneeMax = parseOrNull(anneeMaxStr);

            long start = System.currentTimeMillis();
            List<SearchResult> resultats = service.rechercher(q, domaine, anneeMin, anneeMax);
            long duree = System.currentTimeMillis() - start;

            System.out.println("[SearchServlet] '" + q + "' → " + resultats.size() + " résultats en " + duree + " ms");

            mapper.writeValue(resp.getWriter(), resultats);

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private Integer parseOrNull(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
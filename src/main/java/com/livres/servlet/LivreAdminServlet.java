package com.livres.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.livres.dao.LivreDAO;
import com.livres.model.Livre;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * CRUD admin pour les livres.
 * Protégé par AuthFilter (@WebFilter sur /api/livres-admin/*)
 *
 *   POST   /api/livres-admin          → créer
 *   PUT    /api/livres-admin/{id}     → modifier
 *   DELETE /api/livres-admin/{id}     → supprimer
 */
@WebServlet("/api/livres-admin/*")
public class LivreAdminServlet extends HttpServlet {

    private final LivreDAO dao = new LivreDAO();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");

        try {
            Map<String, Object> body = mapper.readValue(req.getInputStream(), Map.class);
            Livre livre = extraireLivre(body, null);
            dao.save(livre);

            resp.setStatus(201);
            mapper.writeValue(resp.getWriter(), livre);
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");

        String path = req.getPathInfo();
        if (path == null || path.equals("/")) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"ID manquant\"}");
            return;
        }

        try {
            int id = Integer.parseInt(path.substring(1));
            Map<String, Object> body = mapper.readValue(req.getInputStream(), Map.class);

            Optional<Livre> existing = dao.findById(id);
            if (!existing.isPresent()) {
                resp.setStatus(404);
                resp.getWriter().write("{\"error\":\"Livre introuvable\"}");
                return;
            }

            Livre livre = extraireLivre(body, id);
            dao.update(livre);

            mapper.writeValue(resp.getWriter(), livre);
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");

        String path = req.getPathInfo();
        if (path == null || path.equals("/")) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"ID manquant\"}");
            return;
        }

        try {
            int id = Integer.parseInt(path.substring(1));
            dao.delete(id);
            resp.getWriter().write("{\"message\":\"Livre supprimé\",\"id\":" + id + "}");
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private Livre extraireLivre(Map<String, Object> body, Integer id) {
        Livre livre = new Livre();
        if (id != null) livre.setId(id);

        livre.setTitre(str(body.get("titre")));
        livre.setAuteur(str(body.get("auteur")));
        livre.setDomaine(str(body.get("domaine")));
        livre.setIsbn(str(body.get("isbn")));
        livre.setEditeur(str(body.get("editeur")));
        livre.setDescription(str(body.get("description")));
        livre.setImageUrl(str(body.get("imageUrl")));
        livre.setFichierUrl(str(body.get("fichierUrl")));
        livre.setSource("LOCAL");

        Object anneeObj = body.get("annee");
        if (anneeObj != null && !"".equals(anneeObj.toString())) {
            livre.setAnnee(Integer.parseInt(anneeObj.toString()));
        }

        return livre;
    }

    private String str(Object o) {
        return o == null ? null : o.toString();
    }
}
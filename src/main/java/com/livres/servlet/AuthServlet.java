package com.livres.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.livres.model.Utilisateur;
import com.livres.security.SessionManager;
import com.livres.service.AuthService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {

    private final AuthService authService = new AuthService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");
        String path = req.getPathInfo();

        try {
            if ("/login".equals(path)) {
                handleLogin(req, resp);
            } else if ("/logout".equals(path)) {
                handleLogout(req, resp);
            } else {
                resp.setStatus(404);
                ecrire(resp, "error", "Route inconnue");
            }
        } catch (SecurityException e) {
            resp.setStatus(401);
            ecrire(resp, "error", e.getMessage());
        } catch (Exception e) {
            resp.setStatus(500);
            ecrire(resp, "error", e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String path = req.getPathInfo();

        if ("/me".equals(path)) {
            Utilisateur u = SessionManager.getUtilisateur(req);
            if (u == null) {
                resp.setStatus(401);
                ecrire(resp, "error", "Non connecté");
            } else {
                Map<String, Object> result = new HashMap<>();
                result.put("id", u.getId());
                result.put("login", u.getLogin());
                result.put("nom", u.getNom());
                mapper.writeValue(resp.getWriter(), result);
            }
        } else {
            resp.setStatus(404);
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Map<String, String> body = mapper.readValue(req.getInputStream(), Map.class);
        Utilisateur u = authService.connecter(body.get("login"), body.get("motDePasse"));

        SessionManager.login(req, u);

        Map<String, Object> result = new HashMap<>();
        result.put("id", u.getId());
        result.put("login", u.getLogin());
        result.put("nom", u.getNom());
        result.put("message", "Connexion réussie");
        mapper.writeValue(resp.getWriter(), result);
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        SessionManager.logout(req);
        ecrire(resp, "message", "Déconnecté");
    }

    private void ecrire(HttpServletResponse resp, String key, String value)
            throws IOException {
        Map<String, String> m = new HashMap<>();
        m.put(key, value);
        mapper.writeValue(resp.getWriter(), m);
    }
}
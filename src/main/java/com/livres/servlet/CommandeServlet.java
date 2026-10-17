package com.livres.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.livres.dao.CommandeDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@WebServlet("/api/commande")
public class CommandeServlet extends HttpServlet {

    private final CommandeDAO dao = new CommandeDAO();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");

        try {
            Map<String, String> body = mapper.readValue(req.getInputStream(), Map.class);
            String email = body.get("email");
            String titre = body.get("titre");
            String source = body.get("source");

            dao.enregistrer(email, titre, source);

            resp.getWriter().write("{\"message\":\"Commande enregistrée\",\"email\":\"" + email + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
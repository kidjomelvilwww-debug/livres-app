package com.livres.servlet;

import com.livres.security.SessionManager;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Protège les routes admin : /api/livres-admin/*, /api/upload et /admin.html
 */
@WebFilter(urlPatterns = {"/api/livres-admin/*", "/api/upload", "/admin.html"})
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        if (!SessionManager.estConnecte(req)) {
            // Si c'est une requête API → 401 JSON
            if (req.getRequestURI().startsWith(req.getContextPath() + "/api/")) {
                resp.setStatus(401);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"error\":\"Non authentifié\"}");
            } else {
                // Si c'est une page HTML → redirection
                resp.sendRedirect(req.getContextPath() + "/login.html");
            }
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}
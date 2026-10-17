package com.livres.servlet;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Force l'encodage UTF-8 sur toutes les réponses HTTP.
 * Indispensable pour les fichiers statiques (HTML, CSS, JS)
 * que Tomcat sert sinon en ISO-8859-1.
 */
@WebFilter("/*")
public class Utf8ContentTypeFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // Force l'UTF-8 sur les entrées et sorties
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // Enveloppe la réponse pour intercepter le Content-Type
        ContentTypeResponseWrapper wrapper = new ContentTypeResponseWrapper(resp);

        chain.doFilter(request, wrapper);

        // Après le traitement : on force UTF-8 dans le Content-Type
        wrapper.forceUtf8ContentType();
    }

    @Override
    public void destroy() {}
}
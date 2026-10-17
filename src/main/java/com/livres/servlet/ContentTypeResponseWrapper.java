package com.livres.servlet;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Enveloppe la réponse HTTP pour ajouter charset=UTF-8
 * automatiquement au Content-Type.
 */
public class ContentTypeResponseWrapper extends HttpServletResponseWrapper {

    private String contentType;

    public ContentTypeResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public void setContentType(String type) {
        // Ajoute charset=UTF-8 si absent
        if (type != null && !type.toLowerCase().contains("charset")) {
            if (type.startsWith("text/") || type.contains("javascript") || type.contains("json")) {
                type = type + ";charset=UTF-8";
            }
        }
        this.contentType = type;
        super.setContentType(type);
    }

    @Override
    public String getContentType() {
        return contentType != null ? contentType : super.getContentType();
    }

    public void forceUtf8ContentType() {
        String current = super.getContentType();
        if (current != null && !current.toLowerCase().contains("charset")) {
            if (current.startsWith("text/") ||
                current.contains("javascript") ||
                current.contains("json")) {
                super.setContentType(current + ";charset=UTF-8");
            }
        }
    }
}
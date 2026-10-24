package com.livres.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Upload d'images et de PDF (réservé aux admins, voir AuthFilter).
 *
 * - Si SUPABASE_URL et SUPABASE_SERVICE_KEY sont définies : le fichier est envoyé dans
 *   Supabase Storage (bucket SUPABASE_BUCKET, "uploads" par défaut) et l'URL publique complète est renvoyée.
 *   C'est le mode à utiliser en production, car le disque de Render est éphémère.
 * - Sinon : le fichier est écrit dans le dossier /uploads de l'application (mode local XAMPP/Tomcat).
 */
@WebServlet("/api/upload")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,   // 2 MB
    maxFileSize = 1024 * 1024 * 10,        // 10 MB
    maxRequestSize = 1024 * 1024 * 50      // 50 MB
)
public class UploadServlet extends HttpServlet {

    private static final Set<String> EXTENSIONS_AUTORISEES = new HashSet<>(
        Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp", ".pdf"));

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");

        try {
            Part filePart = req.getPart("file");
            if (filePart == null || filePart.getSize() == 0) {
                erreur(resp, 400, "Aucun fichier fourni");
                return;
            }

            String originalName = nomOriginal(filePart);
            String extension = "";
            int dot = originalName.lastIndexOf('.');
            if (dot > 0) extension = originalName.substring(dot).toLowerCase(Locale.ROOT);

            if (!EXTENSIONS_AUTORISEES.contains(extension)) {
                erreur(resp, 400, "Type de fichier non autorisé (images ou PDF uniquement)");
                return;
            }

            // Nom unique pour éviter les conflits
            String newFileName = UUID.randomUUID().toString() + extension;

            String url;
            if (storageConfigure()) {
                url = envoyerVersSupabase(filePart, newFileName, typeMime(extension));
            } else {
                url = ecrireSurDisque(filePart, newFileName);
            }

            resp.getWriter().write("{\"url\":\"" + url + "\",\"filename\":\"" + newFileName + "\"}");

        } catch (Exception e) {
            e.printStackTrace();
            erreur(resp, 500, e.getMessage());
        }
    }

    // ----- Mode production : Supabase Storage -----

    private boolean storageConfigure() {
        return notEmpty(System.getenv("SUPABASE_URL")) && notEmpty(System.getenv("SUPABASE_SERVICE_KEY"));
    }

    private String envoyerVersSupabase(Part part, String fileName, String mime) throws IOException {
        String base = System.getenv("SUPABASE_URL").trim().replaceAll("/+$", "");
        String key = System.getenv("SUPABASE_SERVICE_KEY").trim();
        String bucket = notEmpty(System.getenv("SUPABASE_BUCKET")) ? System.getenv("SUPABASE_BUCKET").trim() : "uploads";

        URL target = new URL(base + "/storage/v1/object/" + bucket + "/" + fileName);
        HttpURLConnection conn = (HttpURLConnection) target.openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(10_000);
        conn.setReadTimeout(60_000);
        conn.setDoOutput(true);
        conn.setRequestProperty("apikey", key);
        // Les anciennes clés service_role sont des JWT ; les nouvelles clés (sb_secret_...) vont dans apikey seulement.
        if (key.startsWith("eyJ")) conn.setRequestProperty("Authorization", "Bearer " + key);
        conn.setRequestProperty("Content-Type", mime);
        conn.setRequestProperty("x-upsert", "false");
        conn.setFixedLengthStreamingMode(part.getSize());

        try (InputStream in = part.getInputStream(); OutputStream out = conn.getOutputStream()) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
        }

        int status = conn.getResponseCode();
        if (status < 200 || status >= 300) {
            String detail = lireFlux(status >= 400 ? conn.getErrorStream() : conn.getInputStream());
            throw new IOException("Supabase Storage a refusé le fichier (HTTP " + status + ") " + detail);
        }

        String url = base + "/storage/v1/object/public/" + bucket + "/" + fileName;
        System.out.println("[Upload] Fichier envoyé vers Supabase Storage : " + url);
        return url;
    }

    // ----- Mode local : disque -----

    private String ecrireSurDisque(Part part, String fileName) throws IOException {
        String uploadDir = getServletContext().getRealPath("/uploads");
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        File file = new File(dir, fileName);
        part.write(file.getAbsolutePath());
        System.out.println("[Upload] Fichier sauvegardé : " + file.getAbsolutePath());
        return "uploads/" + fileName;
    }

    // ----- Utilitaires -----

    private String nomOriginal(Part part) {
        String name = part.getSubmittedFileName();
        if (name == null) return "unknown";
        // Retire un éventuel chemin (certains navigateurs envoient C:\dossier\fichier.pdf)
        name = name.replace('\\', '/');
        return name.substring(name.lastIndexOf('/') + 1);
    }

    private String typeMime(String extension) {
        switch (extension) {
            case ".jpg":
            case ".jpeg": return "image/jpeg";
            case ".png":  return "image/png";
            case ".gif":  return "image/gif";
            case ".webp": return "image/webp";
            case ".pdf":  return "application/pdf";
            default:      return "application/octet-stream";
        }
    }

    private String lireFlux(InputStream in) throws IOException {
        if (in == null) return "";
        StringBuilder sb = new StringBuilder();
        try (InputStream is = in) {
            byte[] buf = new byte[1024];
            int n;
            while ((n = is.read(buf)) != -1 && sb.length() < 500) sb.append(new String(buf, 0, n, "UTF-8"));
        }
        return sb.toString();
    }

    private boolean notEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private void erreur(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        String safe = message == null ? "Erreur" : message.replace("\\", "\\\\").replace("\"", "'").replace("\n", " ");
        resp.getWriter().write("{\"error\":\"" + safe + "\"}");
    }
}

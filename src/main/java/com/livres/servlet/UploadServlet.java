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
import java.util.UUID;

@WebServlet("/api/upload")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,   // 2 MB
    maxFileSize = 1024 * 1024 * 10,        // 10 MB
    maxRequestSize = 1024 * 1024 * 50      // 50 MB
)
public class UploadServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");

        try {
            // Dossier de destination
            String uploadDir = getServletContext().getRealPath("/uploads");
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            // Récupère le fichier
            Part filePart = req.getPart("file");
            if (filePart == null || filePart.getSize() == 0) {
                resp.setStatus(400);
                resp.getWriter().write("{\"error\":\"Aucun fichier fourni\"}");
                return;
            }

            // Nom du fichier
            String originalName = getFileName(filePart);
            String extension = "";
            int dot = originalName.lastIndexOf('.');
            if (dot > 0) extension = originalName.substring(dot);

            // Nom unique pour éviter les conflits
            String newFileName = UUID.randomUUID().toString() + extension;

            // Sauvegarde
            File file = new File(dir, newFileName);
            filePart.write(file.getAbsolutePath());

            // Renvoie l'URL relative
            String url = "uploads/" + newFileName;

            System.out.println("[Upload] Fichier sauvegardé : " + file.getAbsolutePath());
            System.out.println("[Upload] URL : " + url);

            resp.getWriter().write("{\"url\":\"" + url + "\",\"filename\":\"" + newFileName + "\"}");

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private String getFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        for (String token : contentDisposition.split(";")) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 2, token.length() - 1);
            }
        }
        return "unknown";
    }
}
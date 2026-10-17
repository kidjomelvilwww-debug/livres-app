package com.livres.model;

import java.time.LocalDateTime;

/**
 * Représente un livre dans la base.
 */
public class Livre {

    private int id;
    private String titre;
    private String auteur;
    private String domaine;      // Roman, Science, Histoire...
    private Integer annee;       // Integer (nullable), pas int
    private String isbn;
    private String editeur;
    private String description;
    private String imageUrl;
    private String fichierUrl;   // PDF/EPUB à télécharger (optionnel)
    private String source;       // "LOCAL" ou "GOOGLE_BOOKS"
    private LocalDateTime dateAjout;

    public Livre() {}

    public Livre(String titre, String auteur, String domaine, Integer annee) {
        this.titre = titre;
        this.auteur = auteur;
        this.domaine = domaine;
        this.annee = annee;
        this.source = "LOCAL";
    }

    // ----- Getters / Setters -----

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getAuteur() { return auteur; }
    public void setAuteur(String auteur) { this.auteur = auteur; }

    public String getDomaine() { return domaine; }
    public void setDomaine(String domaine) { this.domaine = domaine; }

    public Integer getAnnee() { return annee; }
    public void setAnnee(Integer annee) { this.annee = annee; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getEditeur() { return editeur; }
    public void setEditeur(String editeur) { this.editeur = editeur; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getFichierUrl() { return fichierUrl; }
    public void setFichierUrl(String fichierUrl) { this.fichierUrl = fichierUrl; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public LocalDateTime getDateAjout() { return dateAjout; }
    public void setDateAjout(LocalDateTime dateAjout) { this.dateAjout = dateAjout; }

    @Override
    public String toString() {
        return "Livre{" +
            "id=" + id +
            ", titre='" + titre + '\'' +
            ", auteur='" + auteur + '\'' +
            ", domaine='" + domaine + '\'' +
            ", annee=" + annee +
            '}';
    }
}

package com.livres.model;

/**
 * Résultat de recherche unifié (local ou Google Books).
 */
public class SearchResult {

    private String source;       // "LOCAL" ou "GOOGLE_BOOKS"
    private int localId;          // id en base (si LOCAL)
    private String googleId;      // id Google Books (si GOOGLE_BOOKS)

    private String titre;
    private String auteur;
    private String domaine;
    private Integer annee;
    private String isbn;
    private String editeur;
    private String description;
    private String imageUrl;
    private String fichierUrl;
    private String buyLink;       // lien d'achat (Google Books)

    public SearchResult() {}

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public int getLocalId() { return localId; }
    public void setLocalId(int localId) { this.localId = localId; }

    public String getGoogleId() { return googleId; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }

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

    public String getBuyLink() { return buyLink; }
    public void setBuyLink(String buyLink) { this.buyLink = buyLink; }
}
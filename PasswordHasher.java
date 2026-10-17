package com.livres.security;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Hash de mot de passe avec SHA-256 + salt aléatoire.
 */
public class PasswordHasher {

    private static final SecureRandom RANDOM = new SecureRandom();

    public static String genererSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hasher(String motDePasse, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Base64.getDecoder().decode(salt));
            byte[] hash = md.digest(motDePasse.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Erreur hash", e);
        }
    }

    public static boolean verifier(String motDePasse, String salt, String hashAttendu) {
        String hash = hasher(motDePasse, salt);
        return MessageDigest.isEqual(hash.getBytes(), hashAttendu.getBytes());
    }

    // Pour tester : génère un hash à mettre en base
    public static void main(String[] args) {
        String salt = genererSalt();
        String hash = hasher("admin123", salt);
        System.out.println("Salt : " + salt);
        System.out.println("Hash : " + hash);
        System.out.println("SQL  : UPDATE utilisateurs SET salt='" + salt + "', mot_de_passe_hash='" + hash + "' WHERE login='admin';");
    }
}
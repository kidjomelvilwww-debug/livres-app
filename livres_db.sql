-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le : sam. 17 oct. 2026 à 10:37
-- Version du serveur : 10.4.32-MariaDB
-- Version de PHP : 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `livres_db`
--

-- --------------------------------------------------------

--
-- Structure de la table `commandes`
--

CREATE TABLE `commandes` (
  `id` int(11) NOT NULL,
  `livre_id` int(11) DEFAULT NULL,
  `email_client` varchar(150) DEFAULT NULL,
  `statut` varchar(30) DEFAULT 'EN_ATTENTE',
  `date_commande` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `commandes`
--

INSERT INTO `commandes` (`id`, `livre_id`, `email_client`, `statut`, `date_commande`) VALUES
(1, NULL, 'nessaamss@gmail.com', 'EN_ATTENTE', '2026-10-17 01:50:23');

-- --------------------------------------------------------

--
-- Structure de la table `livres`
--

CREATE TABLE `livres` (
  `id` int(11) NOT NULL,
  `titre` varchar(255) NOT NULL,
  `auteur` varchar(255) NOT NULL,
  `domaine` varchar(100) DEFAULT NULL,
  `annee` int(11) DEFAULT NULL,
  `isbn` varchar(20) DEFAULT NULL,
  `editeur` varchar(150) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `image_url` varchar(500) DEFAULT NULL,
  `fichier_url` varchar(500) DEFAULT NULL,
  `source` varchar(50) DEFAULT 'LOCAL',
  `date_ajout` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `livres`
--

INSERT INTO `livres` (`id`, `titre`, `auteur`, `domaine`, `annee`, `isbn`, `editeur`, `description`, `image_url`, `fichier_url`, `source`, `date_ajout`) VALUES
(1, 'Le Petit Prince', 'Antoine de Saint-Exupéry', 'Roman', 1943, '978-2070612758', 'Gallimard', 'Un classique de la littérature française.', 'https://covers.openlibrary.org/b/isbn/9782070612758-L.jpg', NULL, 'LOCAL', '2026-10-16 15:32:57'),
(2, 'Cursor', 'Hackathon', 'Science', 2026, '', 'Salam', 'Des innovations informatique', 'uploads/a8297527-c59a-4066-baff-4eeb8cbe1152.jpeg', 'uploads/67a77bbd-bee0-45be-88da-9a924e9f43ef.pdf', 'LOCAL', '2026-10-17 02:25:18');

-- --------------------------------------------------------

--
-- Structure de la table `recherches`
--

CREATE TABLE `recherches` (
  `id` int(11) NOT NULL,
  `terme` varchar(255) DEFAULT NULL,
  `filtre_domaine` varchar(100) DEFAULT NULL,
  `filtre_annee_debut` int(11) DEFAULT NULL,
  `filtre_annee_fin` int(11) DEFAULT NULL,
  `nb_resultats` int(11) DEFAULT NULL,
  `date_recherche` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `utilisateurs`
--

CREATE TABLE `utilisateurs` (
  `id` int(11) NOT NULL,
  `login` varchar(50) NOT NULL,
  `mot_de_passe_hash` varchar(255) NOT NULL,
  `salt` varchar(50) NOT NULL,
  `nom` varchar(100) DEFAULT NULL,
  `email` varchar(150) DEFAULT NULL,
  `role` varchar(20) NOT NULL DEFAULT 'ADMIN',
  `date_creation` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `utilisateurs`
--

INSERT INTO `utilisateurs` (`id`, `login`, `mot_de_passe_hash`, `salt`, `nom`, `email`, `role`, `date_creation`) VALUES
(1, 'admin', 'xHfbYKa4QsW4yAEn19nel5qx6XboCDLcdd5i4SU2QcU=', '1HfE1LuK4q72gzvbVrX+Xg==', 'Administrateur', NULL, 'ADMIN', '2026-10-16 14:50:24');

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `commandes`
--
ALTER TABLE `commandes`
  ADD PRIMARY KEY (`id`),
  ADD KEY `livre_id` (`livre_id`);

--
-- Index pour la table `livres`
--
ALTER TABLE `livres`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_titre` (`titre`),
  ADD KEY `idx_auteur` (`auteur`),
  ADD KEY `idx_domaine` (`domaine`),
  ADD KEY `idx_annee` (`annee`);

--
-- Index pour la table `recherches`
--
ALTER TABLE `recherches`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `utilisateurs`
--
ALTER TABLE `utilisateurs`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `login` (`login`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `commandes`
--
ALTER TABLE `commandes`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT pour la table `livres`
--
ALTER TABLE `livres`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT pour la table `recherches`
--
ALTER TABLE `recherches`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `utilisateurs`
--
ALTER TABLE `utilisateurs`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `commandes`
--
ALTER TABLE `commandes`
  ADD CONSTRAINT `commandes_ibfk_1` FOREIGN KEY (`livre_id`) REFERENCES `livres` (`id`) ON DELETE SET NULL;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

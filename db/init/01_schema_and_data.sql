SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";

DROP TABLE IF EXISTS `Compte`;
DROP TABLE IF EXISTS `Utilisateur`;

CREATE TABLE `Utilisateur` (
  `userId` varchar(50) NOT NULL,
  `nom` varchar(45) NOT NULL,
  `prenom` varchar(45) NOT NULL,
  `adresse` varchar(100) NOT NULL,
  `userPwd` varchar(255) DEFAULT NULL,
  `male` bit(1) NOT NULL,
  `type` varchar(10) NOT NULL,
  `numClient` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`userId`),
  UNIQUE KEY `numClient_UNIQUE` (`numClient`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `Compte` (
  `numeroCompte` varchar(50) NOT NULL,
  `userId` varchar(50) NOT NULL,
  `solde` double NOT NULL,
  `avecDecouvert` varchar(5) NOT NULL,
  `decouvertAutorise` double DEFAULT NULL,
  PRIMARY KEY (`numeroCompte`),
  KEY `index_userClient` (`userId`),
  CONSTRAINT `fk_Compte_userId`
    FOREIGN KEY (`userId`) REFERENCES `Utilisateur` (`userId`)
    ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
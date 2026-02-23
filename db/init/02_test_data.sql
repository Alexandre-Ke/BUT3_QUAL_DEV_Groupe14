SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";

INSERT INTO `Utilisateur`
(`userId`, `nom`, `prenom`, `adresse`, `userPwd`, `male`, `type`, `numClient`)
VALUES
    ('a.lidell1', 'Lidell', 'Alice', '789, grande rue, Metz', SHA2('UserBan123!', 256), b'1', 'CLIENT', '9865432100'),
    ('admin', 'Smith', 'Joe', '123, grande rue, Metz', SHA2('Admin123!', 256), b'1', 'MANAGER', NULL),
    ('c.exist', 'TEST NOM', 'TEST PRENOM', 'TEST ADRESSE', SHA2('TEST PASS', 256), b'1', 'CLIENT', '0101010101'),
    ('g.descomptes', 'TEST NOM', 'TEST PRENOM', 'TEST ADRESSE', SHA2('TEST PASS', 256), b'1', 'CLIENT', '1000000001'),
    ('g.descomptesvides', 'TEST NOM', 'TEST PRENOM', 'TEST ADRESSE', SHA2('TEST PASS', 256), b'1', 'CLIENT', '0000000002'),
    ('g.exist', 'TEST NOM', 'TEST PRENOM', 'TEST ADRESSE', SHA2('TEST PASS', 256), b'1', 'CLIENT', '1010101010'),
    ('g.pasdecompte', 'TEST NOM', 'TEST PRENOM', 'TEST ADRESSE', SHA2('TEST PASS', 256), b'1', 'CLIENT', '5544554455'),
    ('j.doe1', 'Doe', 'Jane', '456, grand boulevard, Brest', SHA2('UserBan123!', 256), b'1', 'CLIENT', '1234567890'),
    ('j.doe2', 'Doe', 'John', '457, grand boulevard, Perpignan', SHA2('toto', 256), b'1', 'CLIENT', '0000000001');

INSERT INTO `Compte` (`numeroCompte`, `userId`, `solde`, `avecDecouvert`, `decouvertAutorise`) VALUES
('AB7328887341', 'j.doe2', 4242, 'AVEC', 123),
('AV1011011011', 'g.descomptes', 5, 'AVEC', 100),
('BD4242424242', 'j.doe1', 100, 'SANS', NULL),
('CADNV00000', 'j.doe1', 42, 'AVEC', 42),
('CADV000000', 'j.doe1', 0, 'AVEC', 42),
('CSDNV00000', 'j.doe1', 42, 'SANS', NULL),
('CSDV000000', 'j.doe1', 0, 'SANS', NULL),
('IO1010010001', 'j.doe2', 6868, 'SANS', NULL),
('KL4589219196', 'g.descomptesvides', 0, 'AVEC', 150),
('KO7845154956', 'g.descomptesvides', 0, 'SANS', NULL),
('LA1021931215', 'j.doe1', 100, 'SANS', NULL),
('MD8694030938', 'j.doe1', 500, 'SANS', NULL),
('PP1285735733', 'a.lidell1', 37, 'SANS', NULL),
('SA1011011011', 'g.descomptes', 10, 'SANS', 0),
('TD0398455576', 'j.doe1', 23, 'AVEC', 500),
('XD1829451029', 'j.doe1', -48, 'AVEC', 100);
DROP DATABASE IF EXISTS MapDB;
DROP USER IF EXISTS 'MapUser'@'localhost';

CREATE DATABASE MapDB;
CREATE USER 'MapUser'@'localhost' IDENTIFIED BY 'map';
GRANT ALL PRIVILEGES ON MapDB.* TO 'MapUser'@'localhost'; -- è stato esteso il privileggio in modo da poter anche inserire/eliminare tabelle

FLUSH PRIVILEGES;

CREATE TABLE mapdb.exampleTab(
 X1 float,
 X2 float,
 X3 float
);

INSERT INTO mapdb.exampleTab VALUES (1,2,0);
INSERT INTO mapdb.exampleTab VALUES (0,1,-1);
INSERT INTO mapdb.exampleTab VALUES (1,3,5);
INSERT INTO mapdb.exampleTab VALUES (1,3,4);
INSERT INTO mapdb.exampleTab VALUES (2,2,0);

CREATE TABLE mapdb.exampleTab2(
 X1 int,
 X2 float,
 X3 int,
 X4 float,
 X5 float
);

INSERT INTO mapdb.exampleTab2 (X1, X2, X3, X4, X5) VALUES (1, 2.0, 0, 10.0, 3.14);
INSERT INTO mapdb.exampleTab2 (X1, X2, X3, X4, X5) VALUES (0, 1.5, -1, 150.0, 2.71);
INSERT INTO mapdb.exampleTab2 (X1, X2, X3, X4, X5) VALUES (1, 3.7, 5, 1.2, 1.61);
INSERT INTO mapdb.exampleTab2 (X1, X2, X3, X4, X5) VALUES (1, 3.0, 4, 0.9, 0.99);
INSERT INTO mapdb.exampleTab2 (X1, X2, X3, X4, X5) VALUES (2, 2.2, 0, 56.1, 1.41);


CREATE TABLE mapdb.exampleTab3(
 X1 int,
 X2 float
);

INSERT INTO mapdb.exampleTab3 (X1, X2) VALUES (1, 2.0);
INSERT INTO mapdb.exampleTab3 (X1, X2) VALUES (0, 1.5);

-- esempio tabella con attributo non numerico
CREATE TABLE mapdb.exampleTab4(
 X1 float,
 X2 varchar(19),
 X3 char,
 X4 int
);

INSERT INTO mapdb.exampleTab4 (X1, X2, X3, X4) VALUES (2.0, 'stringa1', 'a', 10);
INSERT INTO mapdb.exampleTab4 (X1, X2, X3, X4) VALUES (3.0, 'stringa2', 'b', 2);
INSERT INTO mapdb.exampleTab4 (X1, X2, X3, X4) VALUES (1.0, 'stringa3', 'c', 4);
INSERT INTO mapdb.exampleTab4 (X1, X2, X3, X4) VALUES (5.0, 'stringa4', 'd', 3);
INSERT INTO mapdb.exampleTab4 (X1, X2, X3, X4) VALUES (0.0, 'stringa5', 'e', 5);

-- esempio tabella vuota
CREATE TABLE mapdb.exampleTab5(
 X1 float
);

COMMIT;

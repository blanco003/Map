DROP DATABASE IF EXISTS  MapDB ;
DROP USER IF EXISTS MapUser@localhost;
CREATE DATABASE MapDB;
CREATE USER 'MapUser'@'localhost' IDENTIFIED BY 'map';
GRANT SELECT ON MapDB.* TO 'MapUser'@'localhost';

CREATE TABLE mapdb.exampleTab(
 X1 float,
 X2 float,
 X3 float
);

insert into mapdb.exampleTab values(1,2,0);
insert into mapdb.exampleTab values(0,1,-1);
insert into mapdb.exampleTab values(1,3,5);
insert into mapdb.exampleTab values(1,3,4);
insert into mapdb.exampleTab values(2,2,0);

commit;

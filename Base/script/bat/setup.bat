
:: solo per la prima esecuzione, esegue il file db.sql per creare il database sulla propria macchina, avvia il server, ed avvia un client

@echo off
echo 'Esecuzione script SQL...'
mysql --verbose --host=localhost --user=root --password=root < ../sql/db.sql

echo 'Avvio Server...'
start cmd /K java -jar ../jar/Server.jar
echo 'Server Avviato!'

echo 'Avvio Client...'
start cmd /K java -jar ../jar/Client.jar localhost 8080
echo 'Client Avviato!'
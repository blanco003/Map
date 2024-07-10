
:: solo per la prima esecuzione, esegue il file db.sql per creare il database sulla propria macchina, avvia il server, e rende attivo il bot su telegram

echo 'Running SQL script ...'
mysql --verbose --host=localhost --user=root --password=root < ../sql/db.sql
echo 'Starting server...'
start cmd /K java -jar ../jar/Server.jar
echo 'Server Started!'
echo 'Starting Client...'
start cmd /K java -jar ../jar/Bot.jar localhost 8080
echo 'Client Started!'

:: libera la porta 8080 da altri processi in esecuzione

@echo off
echo 'Libero la porta 8080'
for /f "tokens=5" %%a in ('netstat -aon ^| find ":8080" ^| find "LISTENING"') do taskkill /f /pid %%a
echo 'Porta 8080 libera'
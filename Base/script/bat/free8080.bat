
:: libera la porta 8080 se ci sono gia altri processi in esecuzione

for /f "tokens=5" %%a in ('netstat -aon ^| find ":8080" ^| find "LISTENING"') do taskkill /f /pid %%a
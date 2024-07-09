package telegram.bot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import static java.lang.Math.toIntExact;

import java.io.IOException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


    //TODO : si potrebbe modificare serveroneclient e mapbot in modo che se la profondita inserita non è corretta viene richiesta anziche terminare l'esecuzione

    //TODO: se il formato non è valido si potrebbe richiedere di inserire un numero anziche terminare(controlla_pronfondita,, serveroneclient)

    //TODO: entrambi i server scrivere meglio il sollevamento di DATABASEXCEPTION

    //TODO: anziche fare diverse funzioni invia_sclelta fare una che prende come parametro le scelte

/**
 * Classe per configurare il bot telegram.
 */
public class MapBot extends TelegramLongPollingBot {

    /** Contenitore dei diversi utenti che stanno interagendo con il bot, identificati dall'id della chat */
    private HashMap<Long, Utente> utenti = new HashMap<>();

    /**
     * Costruttore del bot
     * @param token Token del bot
     * @param name Nome utente del bot
     */
    public MapBot(String token, String name) {
        super(token);    
        System.out.println("\n" +data_corrente()+" - Il bot è ora attivo per rispondere ai messaggi");
    }


    /*  Lista comandi
     *  /start   -  l'utente ha iniziato la conversazione, viene stampato il messagio di benvenuto e viene chiesto di connetersi al server
     *  /connect   -  inizializza la connessione dell'utente (client) al server
     *  /data   - usato per caricare il dataset, viene chiesto all'utente di inserire il nome della tabella da cui ricavare il dataset
     *  /scelta  - viene chiesto di effettuare la scelta tra il caricamento da file o apprendimento dal db
     *  /distanza - viene chiesto di effettuare la scelta della distanza tra single link o average link
     *  /restart  - viene chiusa la connessione al server e riaperta automanticamente, in questo modo viene scartato il dataset caricato
     *  /aggiungi - aggiunge un nuovo dataset al db
     */
    
    @Override
    public void onUpdateReceived(Update update) {


        if (update.hasCallbackQuery()) {
            String utente = update.getCallbackQuery().getFrom().getUserName();
            System.out.println(data_corrente()+" - Utente :("+utente+") - Ricevuta callback query: " + update.getCallbackQuery().getData());
            gestisci_chiamata_di_ritorno(update);

        } else if (update.hasMessage() && update.getMessage().hasText()) {

            String message_text = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();
            Utente utente = null;
    
            if( utenti.containsKey(chat_id)){
                utente= utenti.get(chat_id);
            }else{
                String nome_utente = update.getMessage().getFrom().getUserName();
                utente = new Utente(chat_id, nome_utente, null, "null", false, false);
                utenti.put(chat_id,utente);
            }
    
            System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , stato : (" + utente.getStato() + "), chat id : (" + chat_id + ") ha inviato : " + message_text);
            
            if (message_text.equals("/start")) {        // l'utente ha appena inziato la conversazione

                invia_messaggio("Benvenuto su map, per favore collegati al server tramite il comando /connect", utente);
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , stato : (" + utente.getStato()+ ") --> (default)");
                utente.setStato("default");
                

            } else if(utente.getStato().equals("attesa_risposta")){
                invia_messaggio("Prima di procedere effettua una scelta al messaggio precedente.", utente);
            }else if(message_text.equals("/connect")){      

                if(utente.getConnesso()){
                    invia_messaggio("Sei già connesso con il server, se vuoi riavviare la connessione esegui il comando /restart", utente);
                    return;
                }

                try{
                    collega(utente,"127.0.0.1", 8080);
                    invia_messaggio("Connessione con il server andata a buon fine", utente);
                    invia_scelta_dataset(utente, "Cosa desideri fare ?");
                    System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , stato : (" + utente.getStato()+ ") --> (attesa_risposta)");
                    utente.setStato("attesa_risposta");

                }catch(IOException e){
                    System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+" , Eccezione : "+e.getMessage());
                    invia_messaggio("La connessione al server non è andata a buon fine, per favore verifica il server sia online e riprova /connect", utente);
                }

                
            }else if (message_text.equals("/data")) {

                if(!utente.getConnesso()){
                    invia_messaggio("Non sei ancora collegato ancora al server, per favore connettiti eseguendo il comando /connect", utente);
                    return;
                }

                if(!utente.getDataTrovati()){
                    System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , stato : (" + utente.getStato()+ ") --> (carica_dati)");              
                    utente.setStato("carica_dati");
                    invia_messaggio("Hai eseguito il comando per caricare i dati dal db", utente);
                    invia_messaggio("Inserisci il nome della tabella:", utente);
                }else if (utente.getDataTrovati() && utente.getStato().equals("scelta")) {
                    invia_messaggio("Hai già caricato i dati puoi proseguire con la /scelta, se invece vuoi scartarli e rinserirli riconnetti tramite il comando /restart", utente);
                } else {
                    invia_messaggio("Hai già caricato i dati ed effettuato una scelta di caricamento, se desideri scartare il dataset caricato e reinserirlo riconnettiti tramite il comando /connect", utente);
                }

             } else if (message_text.equals("/scelta")) { 
                if(!utente.getConnesso()){
                    invia_messaggio("Non sei ancora collegato ancora al server, per favore connettiti eseguendo il comando /connect", utente);
                    return;
                }
                    if (!utente.getDataTrovati()) {
                    invia_messaggio("Prima di scegliere il tipo di caricamento devi caricare il dataset tramite il comando /data", utente);
                } else {
                    invia_scelta_caricamento(utente, "Come desideri caricare il Dendrogramma ?");
                    utente.setStato("attesa_risposta");
                }

            } else if(message_text.equals("/distanza")){

                if(!utente.getConnesso()){
                    invia_messaggio("Non sei ancora collegato ancora al server, per favore connettiti eseguendo il comando /connect", utente);
                    return;
                }

                if (!utente.getDataTrovati()) {
                    invia_messaggio("Non hai ancora caricato il dataset, puoi farlo tramite il comando /data", utente);
                } else if(utente.getStato().equals("scelta")){
                    invia_messaggio("Prima di scegliere il tipo di distanza devi scegliere il tipo di caricamento tramite il comando /scelta", utente);
               }else{
                    invia_scelta_distanza(utente, "Scegli il tipo di distanza");
                    utente.setStato("attesa_risposta");
                }
                
            }else if(message_text.equals("/restart")){  

                if(!utente.getConnesso()){
                    invia_messaggio("Non sei ancora collegato ancora al server, per favore connettiti eseguendo il comando /connect", utente);
                    return;
                }
                
                try{
                    scollega(utente);
                    collega(utente,"127.0.0.1", 8080);
                    invia_messaggio("La connessione è stata riavviata con successo.", utente);
                    invia_scelta_dataset(utente, "Cosa desideri fare ?");
                    System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , stato : (" + utente.getStato()+ ") --> (attesa_risposta)");
                    utente.setStato("attesa_risposta");

                }catch(IOException e){
                    System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+") , Eccezione : "+e.getMessage());
                    invia_messaggio(e.getMessage(), utente);
                }

               
            } else if(message_text.equals("/aggiungi")){
                    
                if(!utente.getConnesso()){
                    invia_messaggio("Non sei ancora collegato ancora al server, per favore connettiti eseguendo il comando /connect", utente);
                    return;
                }

                if(utente.getDataTrovati()){
                    invia_messaggio("Hai già caricato un dataset, se vuoi scartarlo e ricominciare l'esecuzione eseugi il comando /restart", utente);
                    return;
                }

                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , stato : (" + utente.getStato()+ ") --> (aggiungi_nome_tabella)");              
                utente.setStato("aggiungi_nome_tabella");
                invia_messaggio("Hai eseguito il comando per caricare un nuovo dataset nel db", utente);
                invia_messaggio("Inserisci il nome della tabella che vuoi aggiungere:", utente);
            
            }else{   
                // se non stati eseguiti comandi, ma è stato ricevuto del semplice testo dobbiamo controllare 
                // in quale stato si trova l'utente ed effettuare la gestione corrispondente
                gestisci_input(utente, message_text);
            }
        }
    }

    /**
     * Gestisce l'input ricevuto dall'utente, quando esso non è un comando telegram prestabilito ma è semplice testo.
     * @param utente Utente che sta interagendo con il bot
     * @param message_text Messaggio inviato dall'utente
     */
    private void gestisci_input(Utente utente, String message_text) {
        

        if (utente.getStato().equals("carica_dati")) {

            /* se l'utente ha eseguito il comando per caricare un dataset già presente sul db allora spediamo al server uno 0 per far capire che l'utente vuole
             * caricare un dataset già presente
             */
            try {
                utente.getConnessione().getObjectOutputStream().writeObject(0);
            } catch (IOException e) {
                e.printStackTrace();
            }

            loadDataOnServer(utente, message_text);

        } else if(utente.getStato().equals("inserimento_nome_tabella")){

            /* se l'utente aveva gia eseguito il comando per scegliere di caricare un dataset già presente sul db il server sa già che l'utente 
             *  vuole caricare un dataset esistetente dunque non dobbiamo rispedire un altro 0 ma viene chiesto all'utente ripetutamente il nome della 
             * tabella della quale caricare il dataset finchè non inserisce un nome di tabella esistente */

             loadDataOnServer(utente, message_text);

        }else if(utente.getStato().equals("caricamento_file")){

            loadDedrogramFromFileOnServer(utente,message_text);

        }  else if(utente.getStato().equals("file_caricato") || utente.getStato().equals("file_salvato")){

            invia_messaggio("Hai già caricato il dataset e stampato il Dendrogramma correttamente, se vuoi ricominciare l'esecuzione esegui il comando /restart", utente);
        
        } else if(utente.getStato().equals("inserisci_profondita")){

            if(controlla_profondita(utente,message_text)){  
                invia_scelta_distanza(utente, "Scegli il tipo di distanza");
                utente.setStato("attesa_risposta");
            }

            // altrimenti rimane nello stesso stato e viene richiesto di inserire la profondita
            
        } else if(utente.getStato().equals("salvataggio")){
            salva_file(utente,message_text);


        } else if(utente.getStato().equals("aggiungi_nome_tabella")){ 

            /* se l'utente ha eseguito il comando per aggiungere un nuovo dataset al db allora spediamo al server un 3 per far capire che l'utente
               vuole inserire un nuovo dataset , e chiediamo di farlgli inserire un nome di tabella per il dataset, il quale non deve essere già presente nel db */
            
            try {
                utente.getConnessione().getObjectOutputStream().writeObject(1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            controlla_univocita_nome_tabella(utente, message_text);

        }else if(utente.getStato().equals("inserimento_nome_nuova_tabella")){

            /* se l'utente aveva gia eseguito il comando per scegliere di inserire un nuovo dataset il server sa già che l'utente vuole inserire un nuovo dataset al db
             * dunque non dobbiamo rispedire un altro 3 ma viene chiesto all'utente ripetutamente il nome della nuova tabella che vuole inserire fino a quando
             * non viene trovato uno che non è già presente */
             
            controlla_univocita_nome_tabella(utente, message_text);

        }else if(utente.getStato().equals("inserimento_numero_esempi")){

            invia_numero_esempi(utente,message_text);

        }else if(utente.getStato().equals("inserimento_dataset")){

            invia_transizione(utente,message_text);

        }else if(utente.getStato().equals("continua_inserimento")){

            try {
                utente.getConnessione().getObjectOutputStream().writeObject(message_text);
            } catch (IOException e) {
               // TODO : stampa bene eccezione
            }

            if(message_text.equalsIgnoreCase("si")){

                // se l'utente risponde si continua l'inserimento di transizioni

                invia_messaggio("Inserisci la prossima transizione, rispettando sempre il formato specificato precedentemente", utente);
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , stato : (" + utente.getStato()+ ") --> (inserimento_dataset)");
                utente.setStato("inserimento_dataset");


            }else if (message_text.equalsIgnoreCase("no")){

                // se l'utente risponde no, il server risponde con un messaggio OK DATASET per far capire
                // che il dataset è stato costruito correttamente sul database

                String risposta ="";
                try {
                     risposta= (String) (utente.getConnessione().getObjectInputStream().readObject());
                } catch (IOException| ClassNotFoundException e) {
                    // TODO : stampa bene eccezione
                    e.printStackTrace();
                    return;
                } 

                if(risposta.equals("OK DATASET")){  // il dataset è stato caricato correttamente sul db
                    invia_messaggio("Il dataset è stato caricato correttamente, puoi procedere con la scelta del tipo di caricamento", utente);
                    invia_scelta_caricamento(utente, "Esegui una scelta di caricamento");
                    System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , stato : (" + utente.getStato()+ ") --> (attesa_risposta)");
                    utente.setStato("attesa_risposta");
                }else{
                    try {
                        scollega(utente);
                    } catch (IOException e) {
                        // TODO : stampa bene eccezione
                    }
                    invia_messaggio("Si sono verificati degli errori durante l'inserimento del dataset, sei stato disconneso per favore riconnettiti tramite il comando /connect", utente);
                }

                
            }else{

                // se l'utente risponde con un messaggio che non è nè si nè no, il server richiede di inserire una risposta valida

                String risposta ="";
                try {
                     risposta= (String) (utente.getConnessione().getObjectInputStream().readObject());
                } catch (IOException| ClassNotFoundException e) {
                    // TODO : stampa bene eccezione
                    e.printStackTrace();
                    return;
                } 
                invia_messaggio(risposta, utente);
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , stato : (" + utente.getStato()+ ") -->  rimane ("+ utente.getStato()+")");
            }

           

        } else if(utente.getStato().equals("elimina_dataset")){
            
            /* se l'utente ha eseguito il comando per eliminare un dataset dal db allora spediamo al server un 5 */
            try {
                utente.getConnessione().getObjectOutputStream().writeObject(5);
            } catch (IOException e) {
                e.printStackTrace();
            }

            deleteDataOnServer(utente, message_text);

        } else if(utente.getStato().equals("inserimento_elimina_dataset")){
            // abbiamo gia inviato al server un 5 per far capire che l'utente vuole eliminare un daset
            deleteDataOnServer(utente, message_text);
        }
     
        else{ // in tutti gli altri casi rispediamo quello che ha inserito specificando che il comando non è riconosciuto
            invia_messaggio("Comando non riconosciuto : "+message_text, utente);
           
        }
    }


    /**
     * Gestisce la chiamata di ritorno intercettata quando l'utente effettua una scelta durante la visualizzazione del menu a bottoni.
     * @param update
     */
    private void gestisci_chiamata_di_ritorno(Update update) {
        
        Utente utente = utenti.get(update.getCallbackQuery().getMessage().getChatId());
        String call_data = update.getCallbackQuery().getData();
        long message_id = update.getCallbackQuery().getMessage().getMessageId();


        // caso in cui l'utente non è ancora connesso al server ma ha semplicemente aperto un vecchio messaggio in cui è presente una scelta ed ha premuto un bottone
        if(utente==null){
            long chat_id = update.getCallbackQuery().getMessage().getChatId();
            Utente temp_utente = new Utente(chat_id, "", null, "", false, false);
            modifica_messagio(temp_utente, message_id, "Non puoi piu considerare questo messaggio !");
            invia_messaggio("Non puoi rispondere a vecchi messaggi ! per favore inzia connettendoti al server tramite il comando /connect", temp_utente);
            return;
        }

        
        if(utente.getStato().equals("attesa_risposta")){ 
        
            if (call_data.equals("call_back_carica_da_file")) {
            
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , stato : (" + utente.getStato()+ ") --> (caricamento_file)");
                utente.setStato("caricamento_file");
                modifica_messagio(utente, message_id, "Hai scelto di caricare il dendrogramma da file");
                invia_messaggio("Inserisci il nome dell'archivio (compreso di estensione)", utente);
           
            
            } else if (call_data.equals("call_back_apprendi_da_db")) {

                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , stato : (" + utente.getStato()+ ") --> (inserisci_profondita)");
                utente.setStato("inserisci_profondita");
                modifica_messagio(utente, message_id, "Hai scelto di apprendere il dendrogramma dal db");
                invia_messaggio("Inserisci la profondità del dendrogramma : ", utente);

            }  else if (call_data.equals("call_back_single_link") || call_data.equals("call_back_average_link")) {

                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , stato : (" + utente.getStato()+ ") --> (controlla_profondita)");
                utente.setStato("controlla_profondita");

                int scelta; // il server specifica (1) per SingleLinkDistance e (2) per AverageLinkDistance

                if(call_data.equals("call_back_single_link")){
                    scelta = 1;
                    modifica_messagio(utente, message_id, "Hai scelto la distanza single link");
                }else{
                    scelta = 2;
                    modifica_messagio(utente, message_id, "Hai scelto la distanza average link");
                }   

                stampa_dendrogramma_distanza_scelta(utente, scelta);
            
            } else if (call_data.equals("call_back_carica_dataset")){

                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , stato : (" + utente.getStato()+ ") --> (carica_dati)");              
                utente.setStato("carica_dati");
                modifica_messagio(utente, message_id, "Hai scelto di caricare un dataset già presente sul database.");
                invia_messaggio("Inserisci il nome della tabella del database da cui ricavare il dataset :", utente);

            } else if (call_data.equals("call_back_crea_nuovo_dataset")){

                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , stato : (" + utente.getStato()+ ") --> (aggiungi_nome_tabella)");              
                utente.setStato("aggiungi_nome_tabella");
                modifica_messagio(utente, message_id, "Hai scelto di creare un nuovo dataset sul database.");
                invia_messaggio("Inserisci il nome della tabella, la quale rappresenta il dataset, che vuoi aggiungere sul database :", utente);
            
            } else if (call_data.equals("call_back_elimina_dataset")){

                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , stato : (" + utente.getStato()+ ") --> (elimina_dataset)");              
                utente.setStato("elimina_dataset");
                modifica_messagio(utente, message_id, "Hai scelto di eliminare un dataset dal database.");
                invia_messaggio("Inserisci il nome della tabella, la quale rappresenta il dataset , che vuoi eliminare dal database :", utente);
            
            }

        }else if(utente.getConnesso()){

            /* altrimenti se l'utente attualmente non dovrebbe inserire una scelta, ad esempio nel caso in cui
             * aveva terminato la connessione e successivamente si riconnette e preme sul menu che era stato precedentemente inviato
             * dobbiamo scartare tale scelta
             */
            modifica_messagio(utente, message_id, "Non puoi piu considerare questo messaggio !");
            invia_messaggio("Non puoi rispondere a vecchi messaggi !", utente); 

            if(!utente.getDataTrovati()){
                invia_messaggio("Puoi procedere iniziando a caricare il dataset tramite il comando /data", utente); 
            }else{
                invia_scelta_caricamento(utente,"Puoi procedere ora effetuando la scelta del tipo di caricamento che vuoi effettuare");
            }
                     
            //TODO: da vedere meglio quando preme su bottoni vecchi

        }
      
    }

    /**
     * Viene caricato il clustering presente sul file, il cui nome è specificato in input, se il caricamento va a buon fine viene inviato all'utente un messaggio contenete il 
     * clustering, altrimenti la connessione viene interrotta e l'utente dovrà ricomcinciare riconnetendosi nuovamente.
     *
     * @param utente Utente che sta interagendo con il bot
     * @param fileName Nome del file su cui è presente il clustering che si vuole caricare
     */
    private void loadDedrogramFromFileOnServer(Utente utente, String fileName) {

        invia_messaggio("Processo di caricamento file in corso...", utente);
    
        /*
         * Per come è scritto il server se l'utente inserisce un archivio non valido il server termina la comunicazione, si potrebbe cambiare il server
         */

        try {
            
            utente.getConnessione().getObjectOutputStream().writeObject(3);      
            utente.getConnessione().getObjectOutputStream().writeObject(fileName);   // spediamo al server il nome del file
    
            String risposta = (String) utente.getConnessione().getObjectInputStream().readObject();

            if (risposta.equals("OK")) {    

                invia_messaggio("Dendrogramma caricato con successo : ", utente);
    
                String dendrogramma = (String) utente.getConnessione().getObjectInputStream().readObject();
                invia_messaggio(dendrogramma, utente);  // inviamo all'utente un messaggio contenete il Dendrogramma caricato dal file
    
                
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") , stato : (" + utente.getStato()+ ") --> (file_caricato)"); 
                utente.setStato("file_caricato");

                invia_messaggio("Se vuoi ricominciare l'esecuzione esegui il comando /restart", utente);
    
            } else {

                // se il server risponde con un messaggio di errore, ovvero il file non è stato trovato

                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") , stato : (" + utente.getStato()+ ") --> rimane ("+utente.getStato()+")"); 
                
                System.out.println(risposta); // stampo il messaggio di errore sul terminale
                invia_messaggio(risposta, utente);    // invio all'utente l'errore generato

                scollega(utente);
                
                invia_messaggio("Sei stato disconneso dal server, se vuoi continuare prima di tutto dovrai riconnetterti tramite il comando /connect", utente);
            }
            
        } catch (IOException|ClassNotFoundException e) {
            System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+") , Eccezione : "+e.getMessage());
            invia_messaggio(e.getMessage(), utente);

            //TODO : scollegare ?
        }
    }
    

    /**
     * Viene salvato sul file, di nome specificato in input, il clustering che è stato costruito, e viene cambiato lo stato dell'utente in file_salvato.
     * 
     * @param utente Utente che sta interagendo con il bot
     * @param message_text Nome del file su cui effettuare il salvataggio
     */
    private void salva_file(Utente utente, String message_text) {

        invia_messaggio("Processo di salvataggio file in corso...", utente);

        try {
            utente.getConnessione().getObjectOutputStream().writeObject(message_text);  // inviamo al server il nome del file su cui l'utente vuole effettuare il salvataggio
        } catch (IOException e) {
            invia_messaggio(e.getMessage(), utente);
            System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+") , Eccezione : "+e.getMessage());

            // TODO :scollegare ?

        }

        invia_messaggio("Il Dendrogramma è stato salvato con successo nel file : "+message_text, utente);
        System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") , stato : (" + utente.getStato()+ ") --> (file_salvato)");  
        utente.setStato("file_salvato");
        invia_messaggio("Se vuoi ricominciare l'esecuzione esegui il comando /restart", utente);
    }



    /**
     * Invia al server il nome della tabella di cui si vuole ricavare il dataset, successivamente viene cambiato lo stato dell'utente in scelta, 
     * in modo da permettergli di  effettuare la scelta tra caricamento da File o apprendimento da Db, se la ricerca della tabella sul db
     * non va a buon fine viene inviato all'utente il messaggio di errore generato e gli si viene chiesto di inserire un nuovo nome di tabella.
     * 
     * @param utente Utente che sta interagendo con il bot
     * @param tableName Nome della tabella del db da cui ricavare il dataset
     */
    private void loadDataOnServer(Utente utente, String tableName) {

        invia_messaggio("Processo caricamento dataset in corso...", utente);

        try {

            
		    utente.getConnessione().getObjectOutputStream().writeObject(tableName); // inviamo al server il nome della tabella di cui ricavare il dataset

		    String risposta= (String) (utente.getConnessione().getObjectInputStream().readObject());

		    if(risposta.equals("OK")){	
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") : caricato correttamente il dataset");
                utente.setDataTrovati(true);
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") , stato : (" + utente.getStato()+ ") --> (scelta)"); 
                utente.setStato("scelta");
                invia_messaggio("Il dataset è stato caricato correttamente.", utente);
                invia_scelta_caricamento(utente, "Come desideri caricare il Dendrogramma ? ");
                utente.setStato("attesa_risposta");

            }else{
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") , stato : (" + utente.getStato()+ ") -->  (inserimento_nome_tabella)");
                utente.setStato("inserimento_nome_tabella"); 
                System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+") , risposta dal sever : "+risposta);
                invia_messaggio(risposta, utente);
                invia_messaggio("Inserisci nuovamente il nome della tabella :",utente);  // chiediamo di reinserire un altro nome di tabella
            }

       } catch (Exception e) {

            System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+") , Eccezione : "+e.getMessage());
            invia_messaggio(e.getMessage(), utente);

            //TODO : scollegare ?
        }
    }


    /**
     * Modifica il messaggio già inviato, di id specificato, con il nuovo messaggio in input.
     * 
     * @param utente Utente che sta interagendo con il bot
     * @param message_id Id del messaggio da modificare
     * @param text Nuovo testo del messaggio
     * 
     */
    private void modifica_messagio(Utente utente, long message_id, String text) {
        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(utente.getChatId());
        editMessage.setMessageId(toIntExact(message_id));
        editMessage.setText(text);
        try {
            execute(editMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    /**
     * Controlla se la profondita inserita dall'utente è valida, ovvero se non supera il numero di esempi del dataset caricato.
     * 
     * @param utente Utente che sta interagendo con il bot
     * @param message_text Messaggio inviato dall'utente
     * @return true se la profondità è valida, false altrimenti
     */
    public boolean controlla_profondita(Utente utente,String message_text){
        
        boolean profonditacorretta = false;

        try {
            utente.getConnessione().getObjectOutputStream().writeObject(2);  // il client invia prima un 2 per scegliere di apprendere il Dendrogramma dal Db
		    utente.getConnessione().getObjectOutputStream().writeObject(message_text); // il client invia al server la profondita

		    String risposta= (String) (utente.getConnessione().getObjectInputStream().readObject());

		    if(risposta.equals("OK")){	

                System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+" ; profondita inserita correttamente");
                profonditacorretta = true;
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") , stato : (" + utente.getStato()+ ") --> (scelta_distanza)");  
                utente.setStato("scelta_distanza");

            }else{

                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") , stato : (" + utente.getStato()+ ") --> rimane  ("+utente.getStato()+")"); 
                System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+" , risposta dal server : "+risposta); // stampiamo sul terminale il messaggio di errore
                invia_messaggio(risposta, utente); // inviamo all'utente il messaggio di errore

                scollega(utente);
                invia_messaggio("Sei stato disconneso dal server, se vuoi continuare prima di tutto dovrai riconnetterti tramite il comando /connect", utente);
            }

       } catch (Exception e) {
        System.out.println(data_corrente()+" - Utente : ("+utente.getNomeUtente()+") , Eccezione : " + e.getMessage());
            try{
                scollega(utente);
                invia_messaggio("Qualcosa è andato storto durante la comunicazione con il server, per favore riconnettiti tramite il comando /connect", utente);
            }catch(IOException e1){
                System.out.println(data_corrente()+" - Utente : ("+utente.getNomeUtente()+") , Eccezione : " + e1.getMessage());
            }
        }

        return profonditacorretta;

    } 

    /**
     * Invia all'utente un messaggio contenente il Dendrogramma costruito, a seconda della distanza specificata in input.
     * @param utente Utente che sta interagendo con il bot
     * @param scelta Scelta della distanza tra (1) SingleLinkDistance e (2) AverageLinkDistance
     */
    void stampa_dendrogramma_distanza_scelta(Utente utente,int scelta){

        invia_messaggio("Processo di recupero e stampa del Dendrogramma....", utente);

        try{

            utente.getConnessione().getObjectOutputStream().writeObject(scelta);  // inviamo il server il tipo di distanza scelta

		    String risposta = (String) (utente.getConnessione().getObjectInputStream().readObject());

		    if (risposta.equals("OK")) {    

                invia_messaggio("Dendrogramma caricato con successo : ", utente);
                String dendrogramma = (String) utente.getConnessione().getObjectInputStream().readObject();  // inviamo all'utente un messagio contente il clustering
                invia_messaggio(dendrogramma, utente);
    
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") , stato : (" + utente.getStato()+ ") --> (salvataggio)");  
                utente.setStato("salvataggio");

                invia_messaggio("Inserisci il nome dell'archivio su cui salvare il Dendrogramma (compreso di estensione)",utente);
    
            } else {

                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") , stato : (" + utente.getStato()+ ") --> rimane  ("+utente.getStato()+")");
                System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+") , risposta dal sever : "+risposta); // stampo il messaggio di errore sul terminale
                invia_messaggio(risposta, utente);    // invio all'utente l'errore generato
            }

        }catch(IOException|ClassNotFoundException e){
            System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+") , Eccezione : "+e.getMessage());

            //TODO: scollegare ? 
        }
    }


    /**
     * Invia un messaggio all'utente della chat specificata in input.
     * @param msg Messaggio da inviare all'utente
     * @param utente Utente che sta interagendo con il bot
     */
    private void invia_messaggio(String msg, Utente utente) {
        SendMessage message = new SendMessage();
        message.setChatId(utente.getChatId());
        message.setText(msg);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    /** Invia all'utente un messaggio composto da 2 bottoni, uno per scegliere di caricare un dataset già presente sul databse e l'atro per inserire un nuovo dataset nel database.
     * 
     * @param utente Utente che sta interagendo con il bot
     * @param text Testo del messaggio prima dei bottoni
     */
    private void invia_scelta_dataset(Utente utente, String text) {  
        SendMessage message = new SendMessage();
        message.setChatId(utente.getChatId());
        message.setText(text);
        
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
    
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton in_btn1 = new InlineKeyboardButton();
        in_btn1.setText("Carica dataset esistente");
        in_btn1.setCallbackData("call_back_carica_dataset");
    
        InlineKeyboardButton in_btn2 = new InlineKeyboardButton();
        in_btn2.setText("Crea nuovo dataset");
        in_btn2.setCallbackData("call_back_crea_nuovo_dataset");
    
        InlineKeyboardButton in_btn3 = new InlineKeyboardButton();
        in_btn3.setText("Elimina dataset");
        in_btn3.setCallbackData("call_back_elimina_dataset");
    
        rowInline.add(in_btn1);
        rowsInline.add(new ArrayList<>(rowInline));
    
        rowInline.clear();
        rowInline.add(in_btn2);
        rowsInline.add(new ArrayList<>(rowInline));  
    
        rowInline.clear();
        rowInline.add(in_btn3);
        rowsInline.add(new ArrayList<>(rowInline));
    
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
    
        try {
            execute(message);
        } catch (TelegramApiException e) {
            //TODO : messaggio all'utente?
            System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+") , Eccezione : "+e.getMessage());
        }
    }

    /** Invia all'utente un messaggio composto da 2 bottoni, uno per scegliere di caricare il Dendrogramma da file e l'altro per apprendere il Dendrogramma
     * da database.
     * 
     * @param utente Utente che sta interagendo con il bot
     * @param text Testo del messaggio prima dei bottoni
     */
    private void invia_scelta_caricamento(Utente utente, String text) {    
        SendMessage message = new SendMessage();
        message.setChatId(utente.getChatId());
        message.setText(text);
  

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton in_btn1 = new InlineKeyboardButton();
        in_btn1.setText("Carica da file");
        in_btn1.setCallbackData("call_back_carica_da_file");
        

        InlineKeyboardButton in_btn2 = new InlineKeyboardButton();
        in_btn2.setText("Apprendi da db");
        in_btn2.setCallbackData("call_back_apprendi_da_db");

        rowInline.add(in_btn1);
        rowsInline.add(new ArrayList<>(rowInline));
        rowInline.clear();
        rowInline.add(in_btn2);
        rowsInline.add(rowInline);

        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        
        try {
            execute(message);
        } catch (TelegramApiException e) {
            //TODO : messaggio all'utente?
            System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+") , Eccezione : "+e.getMessage());
        }
    }


    /** Invia all'utente un messaggio composto da 2 bottoni, uno per scegliere la distanza SingleLink e l'altro per AverageLink
     * 
     * @param utente Utente che sta interagendo con il bot
     * @param text Testo del messaggio prima dei bottoni
     */
    private void invia_scelta_distanza(Utente utente, String text) {  
        SendMessage message = new SendMessage();
        message.setChatId(utente.getChatId());
        message.setText(text);
        
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton in_btn1 = new InlineKeyboardButton();
        in_btn1.setText("Single Link Distance");
        in_btn1.setCallbackData("call_back_single_link");

        InlineKeyboardButton in_btn2 = new InlineKeyboardButton();
        in_btn2.setText("Average Link Distance");
        in_btn2.setCallbackData("call_back_average_link");

        rowInline.add(in_btn1);
        rowsInline.add(new ArrayList<>(rowInline));
        rowInline.clear();
        rowInline.add(in_btn2);
        rowsInline.add(rowInline);   

        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            //TODO : messagio all'utente?
            System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+") , Eccezione : "+e.getMessage());
        }
    }


    /**
     * Inizializza la connessione dell'utente che sta interagendo con il bot al server.
     * @param utente Utente che sta interagendo con il bot
     * @param ip Ip dove è attivo il server
     * @param PORT Porta sulla quale è in ascolto il server
     * @throws IOException Eccezione che si potrebbe verificare durante il collegamento al Sever
     */
    public void collega(Utente utente,String ip,int PORT) throws IOException{

        System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+") collegamento con il server in corso...");

        utente.setConnessione(new Connessione(ip, PORT));
        utente.setConnesso(true);
        utente.setDataTrovati(false);
        utente.setStato("default");

        System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+") collegato correttamente con il server");
    }

    /**
     * Scollega l'utente che sta interagendo con il bot dal server, azzerando il suo stato.
     * @param utente Utente che sta interagendo con il server
     * @throws IOException Eccezione che si potrebbe verificare durante lo scollgamento dal Sever
     */
    public void scollega(Utente utente) throws IOException{

        System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+") scollegamento con il server in corso");

        utente.getConnessione().scollega();
        utente.setConnesso(false);
        utente.setDataTrovati(false);        
        utente.setStato("default");

        System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+") scollegato correttamente dal il server");
    }

    /** Interagisce con il server per stabilire se il nome della nuova tabella scelto dall'utente è un nome valido, ovvero non esiste nessuna tabella
     * nel database con lo stesso nome. Se il nome è valido viene aggiornato lo stato dell'utente, altrimenti si richiede all'utente di inserire un nuovo
     * nome di tabella fino a quando non inserisce uno valido.
     * 
     * @param utente Utente che ha inviato il nome della nuova tabella da inserire nel database.
     * @param tableName Nome della tabella che l'utente desidera inserire nel databse.
     */
    private void controlla_univocita_nome_tabella(Utente utente, String tableName) {

        invia_messaggio("Processo di inserimento nuovo dataset in corso...", utente);
    
        try {

            
		    utente.getConnessione().getObjectOutputStream().writeObject(tableName); // inviamo al server il nome della tabella di cui ricavare il dataset

		    String risposta= (String) (utente.getConnessione().getObjectInputStream().readObject());

		    if(risposta.equals("OK")){	
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") : nuovo nome trovato con successo");
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") , stato : (" + utente.getStato()+ ") --> (inserimento_numero_esempi)"); 
                utente.setStato("inserimento_numero_esempi");
                invia_messaggio("Il nome del dataset inserito è disponibile, puoi procedere", utente);
                invia_messaggio("Inserisci il numero di esempi per ogni transizione del dataset", utente);

            }else{ 
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") , stato : (" + utente.getStato()+ ") --> rimane (inserimento_nome_nuova_tabella)"); 
                utente.setStato("inserimento_nome_nuova_tabella");
                System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+") , risposta dal sever : "+risposta);
                invia_messaggio(risposta, utente);
                invia_messaggio("Inserisci nuovamente il nome della nuova tabella :",utente);  // chiediamo di reinserire un altro nome di tabella
            }

       } catch (Exception e) {

            System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+") , Eccezione : "+e.getMessage());
            invia_messaggio(e.getMessage(), utente);

            //TODO : scollegare ?
        }
    }


    /**
     * Invia al server il numero di esempi che saranno contenuti in ogni transizione nel dataset del database che l'utente ha scelto di creare.
     * @param utente Utente che sta inviando al server il numero di esempi
     * @param message_txt Messaggio inviato dall'utente, rappresenta il numero di esempi
     */
    private void invia_numero_esempi(Utente utente,String message_txt){

        try {
            utente.getConnessione().getObjectOutputStream().writeObject(message_txt);
            String risposta = (String) (utente.getConnessione().getObjectInputStream().readObject());

            if(risposta.equals("OK")){	

                invia_messaggio("Numero di esempi per ogni transizioni inviato correttamente al server.", utente);    
                String risposta_creazione_tabella_db = (String) (utente.getConnessione().getObjectInputStream().readObject());

                if(risposta_creazione_tabella_db.equals("OK")){	
                    System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") : creata correttamente tabella sul db");
                    System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") , stato : (" + utente.getStato()+ ") --> (inserimento_dataset)"); 
                    utente.setStato("inserimento_dataset");
                    invia_messaggio("La tabella è stata creata correttamente sul database.", utente);

                    // costruiamo il formato x1,x2,..xn considerando il numero corretto di esempi scelti dall'utente
                    String formato = "";
                    int num = Integer.parseInt(message_txt);
                    for(int i=1;i<=num;i++){
                        formato += "x"+i;
                        if(i < num) {
                            formato += ",";
                        }  
                    }

                    invia_messaggio("Inizia ad inserire la prima transizione da inserire nel dataset, ricorda che ogni transizione deve essere nel formato : "+formato, utente);
                }else{
                    invia_messaggio(risposta_creazione_tabella_db, utente); 
                    scollega(utente);
                    invia_messaggio("Sei stato disconneso dal server, se vuoi continuare prima di tutto dovrai riconnetterti tramite il comando /connect", utente);
                }

            }else{
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") , stato : (" + utente.getStato()+ ") --> rimane  ("+utente.getStato()+")"); 
                System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+" , risposta dal server : "+risposta); 
                invia_messaggio(risposta, utente); 
                invia_messaggio("Inserisci un nuovo numero di esempi", utente);
            }

        } catch (IOException|ClassNotFoundException e) {
            System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+") , Eccezione : "+e.getMessage());
            invia_messaggio(e.getMessage(), utente);

            //TODO: scollegare?
        }
    }

    /** Invia al server una stringa rappresentante una transizione da inserire nella tabella che si sta creando nel database.
     * @param utente Utente che sta inserendo la transizione
     * @param transizione Messaggio inviato dall'utente, stringa rappresentate la transizione
     */
    private void invia_transizione(Utente utente,String transizione){
        //TODO: verifica formato e numero di esempi nella transizione lato server
        try{

            utente.getConnessione().getObjectOutputStream().writeObject(transizione);
            String risposta = (String) (utente.getConnessione().getObjectInputStream().readObject());

            if(risposta.equals("OK")){
                invia_messaggio("La transizione è stata aggiunta correttamente al dateset", utente);
                invia_messaggio("Vuoi continuare ad inserire altre transizioni ? (Si/No)", utente);
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") , stato : (" + utente.getStato()+ ") --> (continua_inserimento)"); 
                utente.setStato("continua_inserimento");
            }else{
                invia_messaggio(risposta, utente);
                invia_messaggio("per favore inserisci una nuova transizione rispettando il formato specificato", utente);
                
            }

        } catch (IOException|ClassNotFoundException e) {
            System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+") , Eccezione : "+e.getMessage());
            try {
                scollega(utente);
            } catch (IOException e1) {
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") , Eccezione : (" +e1.getMessage());
            }
            invia_messaggio("Si sono verificato degli errori durante la trasmissione della transizione al server, sei stato disconesso per favore ricconnettiti tramite il comando /connect", utente);

        }
    }

    /**
     * Invia al server il nome della tabella di cui l'utente vuole eliminare il dataset.
     * @param utente Utente che sta eliminando il dataset
     * @param tableName Nome della tabella che l'utente ha scelto di eliminare
     * 
     */
    private void deleteDataOnServer(Utente utente, String tableName) {

        invia_messaggio("Processo eliminazione dataset in corso...", utente);

        try {

            
		    utente.getConnessione().getObjectOutputStream().writeObject(tableName); // inviamo al server il nome della tabella che vuole eliminare

		    String risposta= (String) (utente.getConnessione().getObjectInputStream().readObject());

		    if(risposta.equals("OK")){	

                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") : eliminato correttamente il dataset");
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") , stato : (" + utente.getStato()+ ") --> (default)"); 
                utente.setStato("default");
                invia_messaggio("Il dataset è stato eliminato con successo dal database.", utente);
                scollega(utente);
                invia_messaggio("Se desideri continuare esgui il comando /restart", utente);

            }else if(risposta.equals("NON ESISTE")){
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") , stato : (" + utente.getStato()+ ") -->  (inserimento_elimina_dataset)");
                utente.setStato("inserimento_elimina_dataset"); 
                System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+") , risposta dal sever : "+risposta);
                invia_messaggio("Il nome della tabella che hai inserito non esiste nel database.", utente);
                invia_messaggio("Inserisci nuovamente il nome della tabella da eliminare :",utente);  // chiediamo di reinserire un altro nome di tabella
            
            }else{
                System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+") , risposta dal sever : "+risposta);
                invia_messaggio(risposta, utente);
                scollega(utente);
                invia_messaggio("Sei stato disconnesso dal sever, se vuoi continuare prima di tutto riconnettiti tramite il comando /connect", utente);
            }

       } catch (Exception e) {

            System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+") , Eccezione : "+e.getMessage());
            invia_messaggio(e.getMessage(), utente);

            //TODO : scollegare ?
        }
    }


    /**
     * Restituisce la data e l'ora corrente.
     * @return Stringa che rappresenta la data e l'ora corrente.
     */
    private String data_corrente(){
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
        return currentDateTime.format(formatter);
    }


    /**
     * Restituisce lo username del bot.
     */
    @Override
    public String getBotUsername() {
        return "spring_map_boot_bot";
    }

    /**
     * Restituisce il token del bot.
     */
    @Override
    public String getBotToken() {
        return "7405128432:AAEYKtB8eXdS_Dt6EmJpI-hxoz4afcdUJ24";
    }


    

}
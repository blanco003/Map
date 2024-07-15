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

/**
 * Classe per configurare il bot telegram.
 */
public class MapBot extends TelegramLongPollingBot {

    /** Token del bot telegram */
    private String Token;

    /** Username con il quale è possible ritrovare il bot su telegram */
    private String Username;

    /** Contenitore dei diversi utenti che stanno interagendo con il bot, identificati dall'id della chat */
    private HashMap<Long, Utente> utenti = new HashMap<>();

    /**
     * Collezione di Emoji, rappresentate attraverso il loro codice unicode, che il bot usa nell'interagire con l'utente.
     */
    private enum Emoji{

        CARICAMENTO ("\u231B"),
        SUCCESSO("\u2705"),
        ERRORE("\u274C"),
        CONNESSIONE("\uD83C\uDF10"),
        RESTART("\uD83D\uDD01"),
        STOP("\uD83D\uDED1"),
        CARTELLA("\uD83D\uDCC1"),
        NUOVO("\u2795"),
        CESTINO("\uD83D\uDDD1️"),
        DB("\uD83D\uDCBE"),
        FILE("\uD83D\uDCC4"),
        CATENA("\uD83D\uDD17"),
        MEDIA("\u2696");
        
        /** Codice unicode dell'Emoji*/
        private final String unicode;

        /**
         * Costruttore.
         * @param unicode codice unico usato per rappresentare l'Emoji.
         */
        Emoji(String unicode){
            this.unicode = unicode;
        }

        /**
         * Restitusce il codice unicode corrispondente all'Emoji.
         * @return il codice unicode dell'Emoji.
         */
        public String getUnicode(){
            return unicode;
        }
    }

    /**
     * Costruttore del bot
     * @param token Token del bot
     * @param Username Nome utente del bot
     */
    public MapBot(String token, String Username) {
        super(token);    
        this.Token = token;
        this.Username = Username;
        System.out.println("\n" +data_corrente()+" - Il bot è ora disponibile su telegram con nome utente : @"+ Username);
    }

    /*  Lista comandi
     *  /start   -  l'utente ha iniziato la conversazione, viene stampato il messagio di benvenuto e viene chiesto di connetersi al server
     *  /connect   -  inizializza la connessione dell'utente (client) al server
     *  /restart  - viene chiusa la connessione al server e riaperta automanticamente, in questo modo viene scartato il dataset caricato
     */
    
    // ereditato da TelegramLongPollingBot
    /**
     * Gestisce l'aggiornamento rilevato, ovvero quando l'utente interagisce con il bot in qualsiasi modo.
     * @param update Oggetto contenente l'aggiornamento rilevato.
     */
    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasCallbackQuery()) {

            long chat_id = update.getCallbackQuery().getMessage().getChatId();
            // se l'utente ha premuto un bottone, allora aveva già interagito con il bot dunque lo recuperiamo direttamente dall'hashmap
            Utente utente = utenti.get(chat_id); 
            System.out.println(data_corrente()+" - Utente : ("+utente.getNomeUtente()+") - Ricevuta callback query: " + update.getCallbackQuery().getData());
            
            try {
                gestisci_chiamata_di_ritorno(utente,update);
            } catch (IOException e) {
                System.out.println(data_corrente()+" - Utente : ("+ utente+" , Eccezione durante la comunicazione con il server : "+e.getMessage());
                e.printStackTrace();
            }

        } else if (update.hasMessage() && update.getMessage().hasText()) {

            String message_text = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();
            Utente utente = null;
    
            // se l'utente ha già comunicato con il bot lo recuperiamo dall'hashmap, altrimenti inzializziamo un nuovo utente e lo inseriamo nell'hashmap

            if( utenti.containsKey(chat_id)){
                utente = utenti.get(chat_id);
            }else{
                String nome_utente = update.getMessage().getFrom().getUserName();
                utente = new Utente(chat_id, nome_utente, null, "null");
                utenti.put(chat_id,utente);
            }
    
            System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , stato : (" + utente.getStato() + "), chat id : (" + chat_id + ") ha inviato : " + message_text);
            
            if (message_text.equals("/start")) {        // l'utente ha appena inziato la conversazione

            if(utente.getConnessione()==null){
                invia_messaggio("Benvenuto su map, per favore collegati al server tramite il comando /connect", utente);
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , stato : (" + utente.getStato()+ ") --> (default)");
                utente.setStato("default");
            
            }else{
                invia_messaggio(Emoji.ERRORE.getUnicode() + " Hai già avviato la conversazione e sei già collegato al server, se vuoi riavviare la conversazione e la connessione esegui il comando /restart "+ Emoji.RESTART.getUnicode(), utente);
            }
                
                

            } else if(message_text.equals("/restart")){   // l'utente desidera ristabilire la connessione scartando le scelte eseguite fino ad un determinato momento

                if(utente.getConnessione()==null){
                    invia_messaggio(Emoji.ERRORE.getUnicode() + " Non sei ancora collegato ancora al server, puoi iniziare direttamente connettendoti tramite il comando /connect", utente);
                    return;
                }
                
                try{
                    utente.scollega();
                    utente.collega("127.0.0.1", 8080);
                    System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , scollegato e ricollegato correttamente al server");
                    invia_messaggio(Emoji.CONNESSIONE.getUnicode() + " La connessione è stata riavviata con successo.", utente);
                    invia_scelta_dataset(utente, "Cosa desideri fare ?");
                    System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , stato : (" + utente.getStato()+ ") --> (attesa_risposta)");
                    utente.setStato("attesa_risposta");

                }catch(IOException e){
                    System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+") , Eccezione : "+e.getMessage());
                    invia_messaggio(e.getMessage(), utente);
                }

               
            }else if(message_text.equals("/connect")){      

                if(utente.getConnessione()!=null){
                    invia_messaggio(Emoji.STOP.getUnicode() + " Sei già connesso con il server, se vuoi riavviare la connessione esegui il comando /restart "+ Emoji.RESTART.getUnicode(), utente);
                    return;
                }

                try{
                    utente.collega("127.0.0.1", 8080);
                    System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , collegato correttamente al server");
                    invia_messaggio(Emoji.CONNESSIONE.getUnicode() + " Connessione con il server andata a buon fine", utente);
                    invia_scelta_dataset(utente, "Cosa desideri fare ?");
                    System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , stato : (" + utente.getStato()+ ") --> (attesa_risposta)");
                    utente.setStato("attesa_risposta");

                }catch(IOException e){
                    System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+" , Eccezione : "+e.getMessage());
                    invia_messaggio(Emoji.ERRORE.getUnicode() +" La connessione al server non è andata a buon fine, per favore verifica il server sia online e riprova /connect", utente);
                }

                
            }else{   
                // se non stati eseguiti comandi, ma è stato ricevuto del semplice testo dobbiamo controllare 
                // in quale stato si trova l'utente ed effettuare la gestione corrispondente
                try {
                    gestisci_input(utente, message_text);
                } catch (IOException|ClassNotFoundException e) {
                    System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+" , Eccezione durante la comunicazione con il server : "+e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    
    /**
     * Gestisce la chiamata di ritorno, intercettata quando l'utente effettua una scelta premendo un bottone di un menu a scelta.
     * @param utente Utente che ha interagito con il bot.
     * @param update Oggetto contenente l'aggioramento rilevato.
     */
    private void gestisci_chiamata_di_ritorno(Utente utente, Update update) throws IOException{
        
        String call_data = update.getCallbackQuery().getData();
        long message_id = update.getCallbackQuery().getMessage().getMessageId();

        System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , stato : (" + utente.getStato()+")");
        
        if(utente.getStato().equals("attesa_risposta")){ 
        
            if (call_data.equals("call_back_carica_dataset")){

                // se l'utente ha premuto il bottone per caricare un dataset già presente sul db allora spediamo al server uno 0 
                utente.getConnessione().getObjectOutputStream().writeObject(0);

                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , stato : (" + utente.getStato()+ ") --> (carica_dati)");              
                utente.setStato("carica_dati");
                modifica_messagio(utente, message_id, "Hai scelto di caricare un dataset già presente sul database.");
                invia_messaggio("Inserisci il nome della tabella del database da cui ricavare il dataset :", utente);

            } else if (call_data.equals("call_back_crea_nuovo_dataset")){

                // se l'utente ha eseguito il comando per inserire un nuovo dataset sul db allora spediamo al server un 1
                utente.getConnessione().getObjectOutputStream().writeObject(1);

                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , stato : (" + utente.getStato()+ ") --> (inserimento_nome_nuova_tabella)");              
                utente.setStato("inserimento_nome_nuova_tabella");
                modifica_messagio(utente, message_id, "Hai scelto di creare un nuovo dataset sul database.");
                invia_messaggio("Inserisci il nome della tabella, la quale rappresenta il dataset, che vuoi aggiungere sul database :", utente);
            
            }  else if (call_data.equals("call_back_elimina_dataset")){

                // se l'utente ha premuto il bottone per eliminare un dataset dal db allora spediamo al server un 5 
                utente.getConnessione().getObjectOutputStream().writeObject(2);

                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , stato : (" + utente.getStato()+ ") --> (elimina_dataset)");              
                utente.setStato("elimina_dataset");
                modifica_messagio(utente, message_id, "Hai scelto di eliminare un dataset dal database.");
                invia_messaggio("Inserisci il nome della tabella, la quale rappresenta il dataset , che vuoi eliminare dal database :", utente);
            
            }  else if (call_data.equals("call_back_apprendi_da_db")) {

                // se l'utente ha premuto il bottone per apprendere il dendrogramma dal db allora spediamo al server un 2
                utente.getConnessione().getObjectOutputStream().writeObject(3);

                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , stato : (" + utente.getStato()+ ") --> (inserisci_profondita)");
                utente.setStato("inserisci_profondita");
                modifica_messagio(utente, message_id, "Hai scelto di apprendere il dendrogramma dal db");
                invia_messaggio("Inserisci la profondità del dendrogramma : ", utente);  

            } else if (call_data.equals("call_back_carica_da_file")) {

                // se l'utente ha premuto il bottone per caricare il dendrogramma da file allora spediamo al server un 3 
                utente.getConnessione().getObjectOutputStream().writeObject(4);
            
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , stato : (" + utente.getStato()+ ") --> (caricamento_file)");
                utente.setStato("caricamento_file");
                modifica_messagio(utente, message_id, "Hai scelto di caricare il dendrogramma da file");
                invia_messaggio("Inserisci il nome dell'archivio (compreso di estensione)", utente);
           
            
            } else if (call_data.equals("call_back_single_link") || call_data.equals("call_back_average_link")) {

                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , stato : (" + utente.getStato()+ ") --> (scelta_distanza)");
                utente.setStato("scelta_distanza");

                int scelta; // il server specifica (1) per SingleLinkDistance e (2) per AverageLinkDistance

                if(call_data.equals("call_back_single_link")){
                    scelta = 1;
                    modifica_messagio(utente, message_id, "Hai scelto la distanza single link");
                }else{
                    scelta = 2;
                    modifica_messagio(utente, message_id, "Hai scelto la distanza average link");
                }   

                stampa_dendrogramma_distanza_scelta(utente, scelta);
            
            }

        }else{
            // è stato rilevato un callback, ma l'utente non era nello stato di attessa_risposta ovvero doveva rispondedere ad un menu in quel momento
            modifica_messagio(utente, message_id, Emoji.ERRORE.getUnicode() + " Non puoi piu considerare questo messaggio !");
            invia_messaggio(Emoji.ERRORE.getUnicode() + " Non puoi rispondere a vecchi messaggi !", utente);    
        }
      
    }

    /**
     * Gestisce l'input ricevuto dall'utente, quando esso non è un comando telegram prestabilito ma è semplice testo.
     * @param utente Utente che sta interagendo con il bot
     * @param message_text Messaggio inviato dall'utente
     */
    private void gestisci_input(Utente utente, String message_text) throws IOException, ClassNotFoundException{
        
        if (utente.getStato().equals("carica_dati")) {

            /* l'utente aveva gia eseguito il comando per scegliere di caricare un dataset già presente sul db dunque il server è ancora in attesa che l'utente
             * inserisca un nome di tabella valido quindi non dobbiamo rispedire un altro 0 ma viene richiesto all'utente ripetutamente il nome della 
             * tabella della quale caricare il dataset finchè non inserisce un nome di tabella esistente */

            loadDataOnServer(utente, message_text);

        }else if(utente.getStato().equals("caricamento_file")){

            /* l'utente aveva gia eseguito il comando per scegliere di caricare un dataset da file dunque il server è ancora in attesa che l'utente
             * inserisca un nome di file valido quindi non dobbiamo rispedire un altro 3 ma viene richiesto all'utente ripetutamente il nome  
             * di un file finchè non inserisce un file valido (esistente e con un dendrogramma salvato) */
            loadDedrogramFromFileOnServer(utente,message_text);

        } else if(utente.getStato().equals("inserisci_profondita")){

           /* l'utente aveva gia eseguito il comando per apprendere il dendrogramma dal db ed inserire la profondita dunque il server è ancora in attesa che l'utente
             * inserisca una profondita valida quindi non dobbiamo rispedire un altro 2 ma viene richiesto all'utente ripetutamente  
             * la profondita finchè non inserisce una valida */

            inserisci_profondita(utente, message_text);

        } else if(utente.getStato().equals("salvataggio")){

            // l'utente ha inviato il nome del file sul quale eseguire il salvataggio

            saveFile(utente, message_text);


        } else if(utente.getStato().equals("inserimento_nome_nuova_tabella")){

            /* l'utente aveva già inserito il comando per inserire un nuovo dataset quindi il server è ancora in attesa di un nome del dataset
             * dunque non dobbiamo rispedire un altro 1 ma viene richiesto all'utente di inserire un nome di tabella nuovo finche
             * non inserisce uno non ancora esistente.
             */
             
            controlla_univocita_nome_tabella(utente, message_text); // non restituisce true o false, ma se il nome è valido aggiorna lo stato dell'utente

        }else if(utente.getStato().equals("inserimento_numero_esempi")){

            // l'utente inserisce il numero di esempi che desidera avere in ogni transizione nel nuovo dataset

            invia_numero_esempi(utente,message_text);

        }else if(utente.getStato().equals("inserimento_dataset")){

            // l'utente invia la transizione da inserire nel nuovo dataset

            invia_transizione(utente,message_text);

        }else if(utente.getStato().equals("continua_inserimento")){

            // l'utente continua ad inserire transizioni nel nuovo dataset

            utente.getConnessione().getObjectOutputStream().writeObject(message_text);
            // il messaggio ricevuto dall'utente contiene la risposta alla domanda se l'utente vuole continuare ad inserire un'altra transizione   

            if(message_text.equalsIgnoreCase("si")){

                // se l'utente risponde si continua l'inserimento di transizioni

                invia_messaggio("Inserisci la prossima transizione, rispettando sempre il formato specificato precedentemente", utente);
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , stato : (" + utente.getStato()+ ") --> (inserimento_dataset)");
                utente.setStato("inserimento_dataset");


            }else if (message_text.equalsIgnoreCase("no")){

                // se l'utente risponde no, il server risponde con un messaggio OK DATASET per far capire
                // che il dataset è stato costruito correttamente sul database

                String risposta= (String) (utente.getConnessione().getObjectInputStream().readObject());
 

                if(risposta.equals("OK DATASET")){  // il dataset è stato caricato correttamente sul db
                    invia_messaggio(Emoji.SUCCESSO.getUnicode() + " Il dataset è stato creato correttamente sul database ed è stato caricato, puoi procedere con la scelta del tipo di caricamento", utente);
                    invia_scelta_caricamento(utente, "Esegui una scelta di caricamento");
                    System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , stato : (" + utente.getStato()+ ") --> (attesa_risposta)");
                    utente.setStato("attesa_risposta");
                }else{
                    utente.scollega();
                    System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , scollegato correttamente dal server");
                    invia_messaggio(Emoji.ERRORE.getUnicode()+" Si sono verificati degli errori durante l'inserimento del dataset, sei stato disconnesso per favore riconnettiti tramite il comando /connect", utente);
                }

                
            }else{

                // se l'utente risponde con un messaggio che non è nè si nè no, il server richiede di inserire una risposta finche l'utente non inserisce si o no
                String risposta = (String) (utente.getConnessione().getObjectInputStream().readObject());
                invia_messaggio(Emoji.ERRORE.getUnicode() + risposta, utente);
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , stato : (" + utente.getStato()+ ") -->  rimane ("+ utente.getStato()+")");
            }


        } else if(utente.getStato().equals("elimina_dataset")){

            /* l'utente aveva gia eseguito il comando per eliminare un dataset dal db dunque il server è ancora in attesa che l'utente
             * inserisca un nome di tabella valido da eliminare quindi non dobbiamo rispedire un altro 5 ma viene richiesto all'utente ripetutamente il nome della 
             * tabella da eliminare finchè non inserisce un nome di tabella esistente */
            deleteDataOnServer(utente, message_text);

        } else if(utente.getStato().equals("file_caricato") || utente.getStato().equals("file_salvato")){

            invia_messaggio("Hai già caricato il dataset e stampato il Dendrogramma correttamente, se vuoi ricominciare l'esecuzione esegui il comando /restart " + Emoji.RESTART.getUnicode(), utente);
        
        } else if(utente.getStato().equals("attesa_risposta")){   // è presente un menu a bottoni in cui l'utente non ha ancora effettuato una scelta
            
          
            invia_messaggio(Emoji.STOP.getUnicode() + " Prima di procedere effettua una scelta al messaggio precedente.", utente);

        } else{ 
            // in tutti gli altri casi rispediamo quello che ha inserito specificando che il comando non è riconosciuto
            invia_messaggio(Emoji.STOP.getUnicode() + " Comando non riconosciuto : "+message_text, utente);
           
        }
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

        invia_messaggio(Emoji.CARICAMENTO.getUnicode() + " Processo caricamento dataset in corso...", utente);

        try {
		    utente.getConnessione().getObjectOutputStream().writeObject(tableName); // inviamo al server il nome della tabella di cui ricavare il dataset

		    String risposta= (String) (utente.getConnessione().getObjectInputStream().readObject());

		    if(risposta.equals("OK")){	
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") : caricato correttamente il dataset");
                invia_messaggio(Emoji.SUCCESSO.getUnicode() + " Il dataset è stato caricato correttamente.", utente);
                utente.setStato("attesa_risposta");
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") , stato : (" + utente.getStato()+ ") --> (attesa_risposta)"); 
                invia_scelta_caricamento(utente, "Come desideri caricare il Dendrogramma ? ");
                

            }else{
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") , stato : (" + utente.getStato()+ ") -- > rimane ("+utente.getStato()+")");
                System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+") , risposta dal sever : "+risposta);
                invia_messaggio(Emoji.ERRORE.getUnicode() + " "+risposta, utente);
                invia_messaggio("Inserisci nuovamente il nome della tabella :",utente);  // chiediamo di reinserire un altro nome di tabella
            }

        }catch(IOException|ClassNotFoundException e){ // errori durante la comunicazione dell'utente con il server
            try {
                utente.scollega();
            } catch (IOException e1) {
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") eccezione durante lo scollegamento dal server , "+e.getMessage());
            }
            System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , si sono verificati degli errori durante la comunicazione con il server, utente scollegato correttamente dal server");
            invia_messaggio(Emoji.ERRORE.getUnicode() + " Si sono verificati degli errori durante la comunicazione al server e sei stato disconnesso", utente);
            invia_messaggio("Se desideri riconnetterti esegui il comando /connect "+Emoji.RESTART.getUnicode(), utente);
        }   
    }

    
    /**
     * Interagisce con il server in modo da caricare ed inviare all'utente il dendrogramma caricato nel file il cui nome è specificato dal
     * messaggio inviato dall'utente.
     *
     * @param utente Utente che sta interagendo con il bot
     * @param fileName Nome del file su cui è presente il clustering che si vuole caricare
     */
    private void loadDedrogramFromFileOnServer(Utente utente, String fileName) {
        invia_messaggio(Emoji.CARICAMENTO.getUnicode() +" Processo di caricamento file in corso...", utente);

        try {
              
            utente.getConnessione().getObjectOutputStream().writeObject(fileName);   // spediamo al server il nome del file
            String risposta = (String) utente.getConnessione().getObjectInputStream().readObject();

            if (risposta.equals("OK")) {    
                invia_messaggio(Emoji.SUCCESSO.getUnicode() +" Dendrogramma caricato con successo : ", utente);
    
                String dendrogramma = (String) utente.getConnessione().getObjectInputStream().readObject();
                invia_messaggio(dendrogramma, utente);  // inviamo all'utente un messaggio contenete il Dendrogramma caricato dal file
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") , stato : (" + utente.getStato()+ ") --> (file_caricato)"); 
                utente.setStato("file_caricato");

                invia_messaggio("Se vuoi ricominciare l'esecuzione esegui il comando /restart "+ Emoji.RESTART.getUnicode(), utente);
    
            } else {
                // se il server risponde con un messaggio di errore
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") , stato : (" + utente.getStato()+ ") --> rimane ("+ utente.getNomeUtente()+")"); 
                System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+") , risposta dal sever : "+risposta); // stampo il messaggio di errore sul terminale
                invia_messaggio(Emoji.ERRORE.getUnicode() + " " +risposta, utente);    // invio all'utente l'errore generato
                invia_messaggio("Per favore inserisci un file valido.", utente);
            }
            
        }catch(IOException|ClassNotFoundException e){ // errori durante la comunicazione dell'utente con il server
            try {
                utente.scollega();
            } catch (IOException e1) {
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") eccezione durante lo scollegamento dal server , "+e.getMessage());
            }
            System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , si sono verificati degli errori durante la comunicazione con il server, utente scollegato correttamente dal server");
            invia_messaggio(Emoji.ERRORE.getUnicode() +" Si sono verificati degli errori durante la comunicazione al server e sei stato disconnesso", utente);
            invia_messaggio("Se desideri riconnetterti esegui il comando /connect "+Emoji.RESTART.getUnicode(), utente);
        }   
    }
    

    /**
     * Viene salvato sul file, di nome specificato in input, il clustering che è stato costruito, e viene cambiato lo stato dell'utente in file_salvato.
     * 
     * @param utente Utente che sta interagendo con il bot
     * @param message_text Nome del file su cui effettuare il salvataggio
     */
    private void saveFile(Utente utente, String message_text) {
        
        invia_messaggio(Emoji.CARICAMENTO.getUnicode() + " Processo di salvataggio file in corso...", utente);

        try {
            utente.getConnessione().getObjectOutputStream().writeObject(message_text);  // inviamo al server il nome del file su cui l'utente vuole effettuare il salvataggio

        } catch (IOException e) { // errori durante la comunicazione dell'utente con il server

            try {
                utente.scollega();
            } catch (IOException e1) {
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") eccezione durante lo scollegamento dal server , "+e.getMessage());
            }
            System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , si sono verificati degli errori durante la comunicazione con il server, utente scollegato correttamente dal server");
            invia_messaggio(Emoji.ERRORE.getUnicode()+" Si sono verificati degli errori durante la comunicazione al server e sei stato disconnesso", utente);
            invia_messaggio("Se desideri riconnetterti esegui il comando /connect "+ Emoji.RESTART.getUnicode(), utente);

        }

        
        invia_messaggio(Emoji.SUCCESSO.getUnicode() +" Il Dendrogramma è stato salvato con successo nel file : "+message_text, utente);
        System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") , stato : (" + utente.getStato()+ ") --> (file_salvato)");  
        utente.setStato("file_salvato");
        invia_messaggio("Se vuoi ricominciare l'esecuzione esegui il comando /restart "+ Emoji.RESTART.getUnicode(), utente);
    }

    /**
     * Modifica il testo di un messaggio già inviato, di id specificato, con il nuovo testo in input.
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
            System.out.println("Errore durante l'invio di un messaggio all'utente ("+utente.getNomeUtente()+") : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Interagisce con il server, inviando ad esso la profondita del dendrogramma che l'utente ha inviato, se va la costruzione del Dendrogramma va a buon fine
     * viene chiesto all'utente di scegliere il tipo di distanza, altrimenti verrà richiesto all'utente di inserire una nuova profondita finche non inserisce una valida.
     * 
     * @param utente Utente che sta interagendo con il bot
     * @param message_text Messaggio inviato dall'utente
     */
    private void inserisci_profondita(Utente utente,String message_text){
        
        try {
            
		    utente.getConnessione().getObjectOutputStream().writeObject(message_text); // il client invia al server la profondita

		    String risposta= (String) (utente.getConnessione().getObjectInputStream().readObject());

		    if(risposta.equals("OK")){	

                System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+" : profondita inserita correttamente");
                invia_scelta_distanza(utente, "Scegli il tipo di distanza");
                utente.setStato("attesa_risposta");

            }else{

                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") , stato : (" + utente.getStato()+ ") -->  rimane ("+utente.getStato()+")"); 
                System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+" , risposta dal server : "+risposta); // stampiamo sul terminale il messaggio di errore
                invia_messaggio(Emoji.ERRORE.getUnicode() + " " +risposta, utente); // inviamo all'utente il messaggio di errore
                invia_messaggio("Per favore inserisci una profondita valida.", utente);
            }

        }catch(IOException|ClassNotFoundException e){ // errori durante la comunicazione dell'utente con il server
            
            try {
                utente.scollega();
            } catch (IOException e1) {
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") eccezione durante lo scollegamento dal server , "+e.getMessage());
            }
            System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , si sono verificati degli errori durante la comunicazione con il server, utente scollegato correttamente dal server");
            invia_messaggio(Emoji.ERRORE.getUnicode() +" Si sono verificati degli errori durante la comunicazione al server e sei stato disconnesso", utente);
            invia_messaggio("Se desideri riconnetterti esegui il comando /connect "+ Emoji.RESTART.getUnicode(), utente);
        } 

    } 

    /**
     * Invia all'utente un messaggio contenente il Dendrogramma costruito, a seconda della distanza specificata in input.
     * @param utente Utente che sta interagendo con il bot
     * @param scelta Scelta della distanza tra (1) SingleLinkDistance e (2) AverageLinkDistance
     */
    private void stampa_dendrogramma_distanza_scelta(Utente utente,int scelta){
        invia_messaggio(Emoji.CARICAMENTO.getUnicode() +" Processo di recupero e stampa del Dendrogramma....", utente);

        try{

            utente.getConnessione().getObjectOutputStream().writeObject(scelta);  // inviamo il server il tipo di distanza scelta

		    String risposta = (String) (utente.getConnessione().getObjectInputStream().readObject());

		    if (risposta.equals("OK")) {    
                invia_messaggio(Emoji.SUCCESSO.getUnicode() + " Dendrogramma caricato con successo : ", utente);
                String dendrogramma = (String) utente.getConnessione().getObjectInputStream().readObject();  // inviamo all'utente un messagio contente il clustering
                invia_messaggio(dendrogramma, utente);
    
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") , stato : (" + utente.getStato()+ ") --> (salvataggio)");  
                utente.setStato("salvataggio");

                invia_messaggio(Emoji.DB.getUnicode() + " Inserisci il nome dell'archivio su cui salvare il Dendrogramma (compreso di estensione)",utente);
    
            } else {

                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") , stato : (" + utente.getStato()+ ") --> rimane  ("+utente.getStato()+")");
                System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+") , risposta dal sever : "+risposta); // stampo il messaggio di errore sul terminale
                invia_messaggio(Emoji.ERRORE.getUnicode() + " "+risposta, utente);    // invio all'utente l'errore generato
            }

        }catch(IOException|ClassNotFoundException e){ // errori durante la comunicazione dell'utente con il server
            try {
                utente.scollega();
            } catch (IOException e1) {
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") eccezione durante lo scollegamento dal server , "+e.getMessage());
            }
            System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , si sono verificati degli errori durante la comunicazione con il server, utente scollegato correttamente dal server");
            invia_messaggio(Emoji.ERRORE.getUnicode() + " Si sono verificati degli errori durante la comunicazione al server e sei stato disconnesso", utente);
            invia_messaggio("Se desideri riconnetterti esegui il comando /connect " + Emoji.RESTART.getUnicode(), utente);
        }   
    }


    /**
     * Invia un messaggio all'utente.
     * @param msg Testo del messaggio da inviare
     * @param utente Utente che sta interagendo con il bot
     */
    private void invia_messaggio(String msg, Utente utente) {
        SendMessage message = new SendMessage();
        message.setChatId(utente.getChatId());
        message.setText(msg);
        try {
            execute(message);
        }catch (TelegramApiException e) {
            System.out.println("Errore durante l'invio di un messaggio all'utente ("+utente.getNomeUtente()+") : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /** Invia all'utente un messaggio ed un menu composto da 3 bottoni, uno per scegliere di caricare un dataset già presente sul databse, uno per inserire un nuovo dataset nel database,
     * ed uno per eliminare un dataset esistente.
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
        in_btn1.setText("Carica dataset esistente " + Emoji.CARTELLA.getUnicode());  
        in_btn1.setCallbackData("call_back_carica_dataset");
        
        InlineKeyboardButton in_btn2 = new InlineKeyboardButton();
  
        in_btn2.setText("Crea nuovo dataset "+ Emoji.NUOVO.getUnicode());  
        in_btn2.setCallbackData("call_back_crea_nuovo_dataset");
        
        InlineKeyboardButton in_btn3 = new InlineKeyboardButton();
        in_btn3.setText("Elimina dataset "+ Emoji.CESTINO.getUnicode());  
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
            System.out.println("Errore durante l'invio di un messaggio all'utente (" + utente.getNomeUtente() + ") : " + e.getMessage());
            e.printStackTrace();
        }
    }
    

    /** Invia all'utente un messaggio ed un menu composto da 2 bottoni, uno per scegliere di caricare il Dendrogramma da file e l'altro per apprendere il Dendrogramma
     * da database.
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
        in_btn1.setText("Carica da file "+ Emoji.FILE.getUnicode());
        in_btn1.setCallbackData("call_back_carica_da_file");
        

        InlineKeyboardButton in_btn2 = new InlineKeyboardButton();
        in_btn2.setText("Apprendi da db "+ Emoji.DB.getUnicode());
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
        }catch (TelegramApiException e) {
            System.out.println("Errore durante l'invio di un messaggio all'utente ("+utente.getNomeUtente()+") : " + e.getMessage());
            e.printStackTrace();
        }
    }


    /** Invia all'utente un messaggio ed un menu composto da 2 bottoni, uno per scegliere la distanza SingleLink e l'altro per AverageLink
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
        in_btn1.setText("Single Link Distance " + Emoji.CATENA.getUnicode());
        in_btn1.setCallbackData("call_back_single_link");

        InlineKeyboardButton in_btn2 = new InlineKeyboardButton();
        in_btn2.setText("Average Link Distance " + Emoji.MEDIA.getUnicode());
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
        }catch (TelegramApiException e) {
            System.out.println("Errore durante l'invio di un messaggio all'utente ("+utente.getNomeUtente()+") : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Interagisce con il server per stabilire se il nome della nuova tabella scelto dall'utente è un nome valido, ovvero non esiste nessuna tabella
     * nel database con lo stesso nome. Se il nome è valido viene aggiornato lo stato dell'utente, altrimenti si richiede all'utente di inserire un nuovo
     * nome di tabella fino a quando non inserisce uno valido.
     * 
     * @param utente Utente che ha inviato il nome della nuova tabella da inserire nel database.
     * @param tableName Nome della tabella che l'utente desidera inserire nel databse.
     */
    private void controlla_univocita_nome_tabella(Utente utente, String tableName) {
        invia_messaggio(Emoji.CARICAMENTO.getUnicode() + " Processo di inserimento nuovo dataset in corso...", utente);
    
        try {

		    utente.getConnessione().getObjectOutputStream().writeObject(tableName); // inviamo al server il nome della tabella di cui ricavare il dataset

		    String risposta= (String) (utente.getConnessione().getObjectInputStream().readObject());

		    if(risposta.equals("OK")){	
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") : nuovo nome trovato con successo");
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") , stato : (" + utente.getStato()+ ") --> (inserimento_numero_esempi)"); 
                utente.setStato("inserimento_numero_esempi");
                invia_messaggio(Emoji.SUCCESSO.getUnicode() + " Il nome del dataset inserito è disponibile, puoi procedere", utente);
                invia_messaggio("Inserisci il numero di esempi per ogni transizione del dataset", utente);

            }else{ 
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") , stato : (" + utente.getStato()+ ") --> rimane ("+utente.getStato()+")"); 
                System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+") , risposta dal sever : "+risposta);
                invia_messaggio(Emoji.ERRORE.getUnicode() + " " + risposta, utente);
                invia_messaggio("Inserisci nuovamente il nome della nuova tabella :",utente);  // chiediamo di reinserire un altro nome di tabella
            }

        }catch(IOException|ClassNotFoundException e){ // errori durante la comunicazione dell'utente con il server
            try {
                utente.scollega();
            } catch (IOException e1) {
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") eccezione durante lo scollegamento dal server , "+e.getMessage());
            }
            System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , si sono verificati degli errori durante la comunicazione con il server, utente scollegato correttamente dal server");
            invia_messaggio(Emoji.ERRORE.getUnicode() + " Si sono verificati degli errori durante la comunicazione al server e sei stato disconnesso", utente);
            invia_messaggio("Se desideri riconnetterti esegui il comando /connect "+ Emoji.RESTART.getUnicode(), utente);
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
                    invia_messaggio(Emoji.SUCCESSO.getUnicode() + " La tabella è stata creata correttamente sul database.", utente);

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
                    invia_messaggio(Emoji.ERRORE.getUnicode() + " "+risposta_creazione_tabella_db, utente); 
                    utente.scollega();
                    System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , scollegato correttamente dal server");
                    invia_messaggio("Sei stato disconnesso dal server, se vuoi continuare prima di tutto dovrai riconnetterti tramite il comando /connect", utente);
                }

            }else{
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") , stato : (" + utente.getStato()+ ") --> rimane  ("+utente.getStato()+")"); 
                System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+" , risposta dal server : "+risposta); 
                invia_messaggio(Emoji.ERRORE.getUnicode() + " "+risposta, utente); 
                invia_messaggio("Inserisci un nuovo numero di esempi", utente);
            }

        }catch(IOException|ClassNotFoundException e){ // errori durante la comunicazione dell'utente con il server
            try {
                utente.scollega();
            } catch (IOException e1) {
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") eccezione durante lo scollegamento dal server , "+e.getMessage());
            }
            System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , si sono verificati degli errori durante la comunicazione con il server, utente scollegato correttamente dal server");
            invia_messaggio(Emoji.ERRORE.getUnicode()+" Si sono verificati degli errori durante la comunicazione al server e sei stato disconnesso", utente);
            invia_messaggio("Se desideri riconnetterti esegui il comando /connect " + Emoji.RESTART.getUnicode(), utente);
        }   
    }

    /** Invia al server una stringa rappresentante una transizione da inserire nella tabella che si sta creando nel database.
     * @param utente Utente che sta inserendo la transizione
     * @param transizione Messaggio inviato dall'utente, stringa rappresentate la transizione
     */
    private void invia_transizione(Utente utente,String transizione){
      
        try{

            utente.getConnessione().getObjectOutputStream().writeObject(transizione);
            String risposta = (String) (utente.getConnessione().getObjectInputStream().readObject());

            if(risposta.equals("OK")){
                invia_messaggio(Emoji.SUCCESSO.getUnicode() + " La transizione è stata aggiunta correttamente al dateset", utente);
                invia_messaggio("Vuoi continuare ad inserire altre transizioni ? (Si/No)", utente);
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") , stato : (" + utente.getStato()+ ") --> (continua_inserimento)"); 
                utente.setStato("continua_inserimento");
            }else{
                invia_messaggio(Emoji.ERRORE.getUnicode() +" "+risposta, utente);
                invia_messaggio("Per favore inserisci una nuova transizione rispettando il formato specificato", utente);
                
            }

        } catch (IOException|ClassNotFoundException e) {
            System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+") , Eccezione : "+e.getMessage());
            try {
                utente.scollega();
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , scollegato correttamente dal server");
            } catch (IOException e1) {
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") , Eccezione : (" +e1.getMessage());
            }
            invia_messaggio(Emoji.ERRORE.getUnicode()+" Si sono verificato degli errori durante la trasmissione della transizione al server, sei stato disconesso per favore ricconnettiti tramite il comando /connect", utente);

        }
    }

    /**
     * Invia al server il nome della tabella di cui l'utente vuole eliminare il dataset.
     * @param utente Utente che sta eliminando il dataset
     * @param tableName Nome della tabella che l'utente ha scelto di eliminare
     * 
     */
    private void deleteDataOnServer(Utente utente, String tableName) {
        invia_messaggio(Emoji.CARICAMENTO.getUnicode() +" Processo eliminazione dataset in corso...", utente);

        try{

		    utente.getConnessione().getObjectOutputStream().writeObject(tableName); // inviamo al server il nome della tabella che vuole eliminare

		    String risposta= (String) (utente.getConnessione().getObjectInputStream().readObject());

		    if(risposta.equals("OK")){	

                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") : eliminato correttamente il dataset");
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") , stato : (" + utente.getStato()+ ") --> (default)"); 
                utente.setStato("default");
                invia_messaggio(Emoji.SUCCESSO.getUnicode() +" Il dataset è stato eliminato con successo dal database.", utente);
                invia_messaggio("Se desideri ricominciare l'esecuzione esegui il comando /restart "+ Emoji.RESTART.getUnicode(), utente);

            }else if(risposta.equals("NON ESISTE") || risposta.equals("NUMERO")){

                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") , stato : (" + utente.getStato()+ ") -->  rimane ("+utente.getStato()+")");
                System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+") , risposta dal sever : "+risposta);

                if(risposta.equals("NON ESISTE")){
                    invia_messaggio(Emoji.ERRORE.getUnicode() + " Il nome della tabella che hai inserito non esiste nel database.", utente);
                }else{
                    invia_messaggio(Emoji.ERRORE.getUnicode() + " In sql non può esistere un nome di tabella che sia composto solo da numeri", utente);
                }


                invia_messaggio("Inserisci nuovamente il nome della tabella da eliminare :",utente);  // chiediamo di reinserire un altro nome di tabella

            }else{
                System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+") , risposta dal sever : "+risposta);
                invia_messaggio(Emoji.ERRORE.getUnicode() +" "+risposta, utente);
                utente.scollega();
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , scollegato correttamente dal server");
                invia_messaggio("Sei stato disconnesso dal sever, se vuoi continuare prima di tutto riconnettiti tramite il comando /connect", utente);
            }

        }catch(IOException|ClassNotFoundException e){ // errori durante la comunicazione dell'utente con il server
            try {
                utente.scollega();
            } catch (IOException e1) {
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") eccezione durante lo scollegamento dal server , "+e.getMessage());
            }
            System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente() + ") , si sono verificati degli errori durante la comunicazione con il server, utente scollegato correttamente dal server");
            invia_messaggio(Emoji.ERRORE.getUnicode()+" Si sono verificati degli errori durante la comunicazione al server e sei stato disconnesso", utente);
            invia_messaggio("Se desideri riconnetterti esegui il comando /connect "+Emoji.RESTART.getUnicode(), utente);
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

    
    // ereditato da TelegramLongPollingBot.
    /**
     * Restituisce lo username del bot con il quale è visibile su telegram.
     */
    @Override
    public String getBotUsername() {
        return this.Username;
    }

    // ereditato da TelegramLongPollingBot
    /**
     * Restituisce il token del bot.
     */
    @Override
    public String getBotToken() {
        return this.Token;
    }


}
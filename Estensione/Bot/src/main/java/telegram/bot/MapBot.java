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

        LOADING("\u231B"),
        SUCCESS("\u2705"),
        ERROR("\u274C"),
        CONNECTION("\uD83C\uDF10"),
        RESTART("\uD83D\uDD01"),
        STOP("\uD83D\uDED1"),
        FOLDER("\uD83D\uDCC1"),
        NEW("\u2795"),
        BIN("\uD83D\uDDD1️"),
        DB("\uD83D\uDCBE"),
        FILE("\uD83D\uDCC4"),
        CHAIN("\uD83D\uDD17"),
        AVERAGE("\u2696");
        
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
        System.out.println("\n" +currentDate()+" - Il bot è ora disponibile su telegram con nome utente : @"+ Username);
    }


    /*  Lista comandi che l'utente può eseguire.

     *  /start  - l'utente ha iniziato la conversazione (preme sul pulsante avvia), 
     *            viene stampato il messagio di benvenuto e viene chiesto di connetersi al server
     * 
     *  /connect -  l'utente (client) si collega la server 
     * 
     *  /restart  - l'utente può riavviare la conversazione in qualsiasi momento, 
     *              viene chiusa la connessione al server e riaperta automanticamente 
     *              (in questo modo viene scartato il dataset caricato)
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

            // se l'utente ha premuto un bottone di un menu, allora aveva già interagito con il bot dunque lo recuperiamo direttamente dall'hashmap
            Utente utente = utenti.get(chat_id); 
            
            System.out.println(currentDate()+" - Utente : ("+utente.getUserName()+") - Ricevuta callback query: " + update.getCallbackQuery().getData());
            
            try {

                handleCallBackQuery(utente, update);

            } catch(IOException|ClassNotFoundException e){
                
                try {
                    utente.disconnect();
                } catch (IOException e2) {
                    System.out.println(currentDate()+" - Utente : (" + utente.getUserName() + ") eccezione durante lo scollegamento dal server , "+e.getMessage());
                }
            
                System.out.println(currentDate()+" - Utente : (" + utente.getUserName() + ") , si sono verificati degli errori durante la comunicazione con il server, utente scollegato correttamente dal server");
                sendMessage(utente, Emoji.ERROR.getUnicode() + " Si sono verificati degli errori durante la comunicazione al server e sei stato disconnesso");
                sendMessage(utente, "Se desideri riconnetterti esegui il comando /connect "+Emoji.RESTART.getUnicode());
            } 

        } else if (update.hasMessage() && update.getMessage().hasText()) {

            String message_text = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();
            Utente utente = null;
    
            // se l'utente ha già comunicato con il bot lo recuperiamo dall'hashmap
            // altrimenti inzializziamo un nuovo utente e lo inseriamo nell'hashmap

            if( utenti.containsKey(chat_id)){

                utente = utenti.get(chat_id);

            }else{

                String nome_utente = update.getMessage().getFrom().getUserName();
                utente = new Utente(chat_id, nome_utente, null, "null");
                utenti.put(chat_id,utente);
            }
    
            System.out.println(currentDate()+" - Utente : (" + utente.getUserName() + ") , stato : (" + utente.getUserState() + "), chat id : (" + chat_id + ") ha inviato : " + message_text);
            
            if (message_text.equals("/start")) {        // l'utente ha appena inziato la conversazione

                if(utente.getConnection()==null){
                    sendMessage(utente, "Benvenuto su map, per favore collegati al server tramite il comando /connect");
                    System.out.println(currentDate()+" - Utente : (" + utente.getUserName() + ") , stato : (" + utente.getUserState()+ ") --> (default)");
                    utente.setUserState("default");
            
                }else{
                    sendMessage(utente, Emoji.ERROR.getUnicode() + " Hai già avviato la conversazione e sei già collegato al server, se vuoi riavviare la conversazione e la connessione esegui il comando /restart "+ Emoji.RESTART.getUnicode());
                }
                
                

            } else if(message_text.equals("/restart")){   // l'utente desidera ristabilire la connessione scartando le scelte eseguite fino ad un determinato momento

                if(utente.getConnection()==null){
                    sendMessage(utente, Emoji.ERROR.getUnicode() + " Non sei ancora collegato ancora al server, puoi iniziare direttamente connettendoti tramite il comando /connect");
                    return;
                }
                
                try{
                    utente.disconnect();
                    utente.connect("127.0.0.1", 8080);

                    System.out.println(currentDate()+" - Utente : (" + utente.getUserName() + ") , scollegato e ricollegato correttamente al server");
                    
                    sendMessage(utente, Emoji.CONNECTION.getUnicode() + " La connessione è stata riavviata con successo.");
                    sendChoiceDataset(utente, "Cosa desideri fare ?");
                    
                    System.out.println(currentDate()+" - Utente : (" + utente.getUserName() + ") , stato : (" + utente.getUserState()+ ") --> (attesa_risposta)");
                    utente.setUserState("attesa_risposta");

                }catch(IOException e){
                    System.out.println(currentDate()+" - Utente : ("+ utente.getUserName()+") , Eccezione : "+e.getMessage());
                    sendMessage(utente, Emoji.ERROR.getUnicode() + " Si sono verificati degli errori durante la riconnessione, se vuoi riprovare esegui il comando /restart "+ Emoji.RESTART.getUnicode());
                }

               
            }else if(message_text.equals("/connect")){      

                if(utente.getConnection()!=null){
                    sendMessage(utente, Emoji.STOP.getUnicode() + " Sei già connesso con il server, se vuoi riavviare la connessione esegui il comando /restart "+ Emoji.RESTART.getUnicode());
                    return;
                }

                try {
                    utente.connect("127.0.0.1", 8080);

                    System.out.println(currentDate()+" - Utente : (" + utente.getUserName() + ") , collegato correttamente al server");
                    
                    sendMessage(utente, Emoji.CONNECTION.getUnicode() + " Connessione con il server andata a buon fine");
                    sendChoiceDataset(utente, "Cosa desideri fare ?");
                    
                    System.out.println(currentDate()+" - Utente : (" + utente.getUserName() + ") , stato : (" + utente.getUserState()+ ") --> (attesa_risposta)");
                    utente.setUserState("attesa_risposta");

                } catch(IOException e){
                    System.out.println(currentDate()+" - Utente : ("+ utente.getUserName()+" , Eccezione : "+e.getMessage());
                    sendMessage(utente, Emoji.ERROR.getUnicode() +" La connessione al server non è andata a buon fine, per favore verifica il server sia online e riprova /connect");
                }

                
            }else{  
                
                // se non stati eseguiti comandi, ma è stato ricevuto del semplice testo dobbiamo controllare 
                // in quale stato si trova l'utente ed effettuare la gestione corrispondente

                if(utente.getConnection()==null){
                    sendMessage(utente, Emoji.ERROR.getUnicode() + " Non sei ancora collegato ancora al server, per favore inizia a connetterti tramite il comando /connect");
                    return;
                }

                try {

                    handleInput(utente, message_text);

                }   catch(IOException|ClassNotFoundException e){
                    
                    try {
                        utente.disconnect();
                    } catch (IOException e2) {
                        System.out.println(currentDate()+" - Utente : (" + utente.getUserName() + ") eccezione durante lo scollegamento dal server , "+e.getMessage());
                    }
                
                    System.out.println(currentDate()+" - Utente : (" + utente.getUserName() + ") , si sono verificati degli errori durante la comunicazione con il server, utente scollegato correttamente dal server");
                    sendMessage(utente, Emoji.ERROR.getUnicode() + " Si sono verificati degli errori durante la comunicazione al server e sei stato disconnesso dal server.");
                    sendMessage(utente, "Se desideri riconnetterti esegui il comando /connect "+Emoji.RESTART.getUnicode());
                } 
            }
        }
    }

    
    /**
     * Gestisce la chiamata di ritorno, intercettata quando l'utente effettua una scelta premendo un bottone di un menu a scelta.
     * 
     * @param utente Utente che ha interagito con il bot.
     * @param update Oggetto contenente l'aggioramento rilevato.
     * @throws IOException Se si verificano errori durante la comunicazione con  il server
     * @throws ClassNotFoundException Se si verificano errori durante la lettura della risposta del server
     */
    private void handleCallBackQuery(Utente utente, Update update) throws IOException, ClassNotFoundException{
        
        String call_data = update.getCallbackQuery().getData();
        long message_id = update.getCallbackQuery().getMessage().getMessageId();  

        System.out.println(currentDate()+" - Utente : (" + utente.getUserName() + ") , stato : (" + utente.getUserState()+")");
        
        if(utente.getUserState().equals("attesa_risposta")){ 
        
            if (call_data.equals("call_back_carica_dataset")){

                // se l'utente ha premuto il bottone per caricare un dataset già presente sul db allora spediamo al server uno 0 
                utente.getConnection().getObjectOutputStream().writeObject(0);

                System.out.println(currentDate()+" - Utente : (" + utente.getUserName() + ") , stato : (" + utente.getUserState()+ ") --> (carica_dati)");              
                utente.setUserState("carica_dati");
                
                editMessage(utente, message_id, "Hai scelto di caricare un dataset già presente sul database.");
                sendMessage(utente, "Inserisci il nome della tabella del database da cui ricavare il dataset :");

            } else if (call_data.equals("call_back_crea_nuovo_dataset")){

                // se l'utente ha premuto il bottone per inserire un nuovo dataset sul db allora spediamo al server un 1
                utente.getConnection().getObjectOutputStream().writeObject(1);

                System.out.println(currentDate()+" - Utente : (" + utente.getUserName() + ") , stato : (" + utente.getUserState()+ ") --> (inserimento_nome_nuova_tabella)");              
                utente.setUserState("inserimento_nome_nuova_tabella");
                
                editMessage(utente, message_id, "Hai scelto di creare un nuovo dataset sul database.");
                sendMessage(utente, "Inserisci il nome della tabella, la quale rappresenta il dataset, che vuoi aggiungere sul database :");
            
            }  else if (call_data.equals("call_back_elimina_dataset")){

                // se l'utente ha premuto il bottone per eliminare un dataset dal db allora spediamo al server un 2
                utente.getConnection().getObjectOutputStream().writeObject(2);

                System.out.println(currentDate()+" - Utente : (" + utente.getUserName() + ") , stato : (" + utente.getUserState()+ ") --> (elimina_dataset)");              
                utente.setUserState("elimina_dataset");
                
                editMessage(utente, message_id, "Hai scelto di eliminare un dataset dal database.");
                sendMessage(utente, "Inserisci il nome della tabella, la quale rappresenta il dataset , che vuoi eliminare dal database :");
            
            }  else if (call_data.equals("call_back_apprendi_da_db")) {

                // se l'utente ha premuto il bottone per apprendere il dendrogramma dal db allora spediamo al server un 3
                utente.getConnection().getObjectOutputStream().writeObject(3);

                System.out.println(currentDate()+" - Utente : (" + utente.getUserName() + ") , stato : (" + utente.getUserState()+ ") --> (insertDepth)");
                utente.setUserState("insertDepth");
                
                editMessage(utente, message_id, "Hai scelto di apprendere il dendrogramma dal db");
                sendMessage(utente, "Inserisci la profondità del dendrogramma : ");  

            } else if (call_data.equals("call_back_carica_da_file")) {

                // se l'utente ha premuto il bottone per caricare il dendrogramma da file allora spediamo al server un 4 
                utente.getConnection().getObjectOutputStream().writeObject(4);
            
                System.out.println(currentDate()+" - Utente : (" + utente.getUserName() + ") , stato : (" + utente.getUserState()+ ") --> (caricamento_file)");
                utente.setUserState("caricamento_file");
                
                editMessage(utente, message_id, "Hai scelto di caricare il dendrogramma da file");
                sendMessage(utente, "Inserisci il nome dell'archivio (compreso di estensione)");
           
            
            } else if (call_data.equals("call_back_single_link") || call_data.equals("call_back_average_link")) {

                // l'utente ha premuto sul bottone single link distance o avera link distance dopo aver caricato correttamente il dataset

                System.out.println(currentDate()+" - Utente : (" + utente.getUserName() + ") , stato : (" + utente.getUserState()+ ") --> (scelta_distanza)");
                utente.setUserState("scelta_distanza");

                int scelta; // il server specifica (1) per SingleLinkDistance e (2) per AverageLinkDistance

                if(call_data.equals("call_back_single_link")){
                    scelta = 1;
                    editMessage(utente, message_id, "Hai scelto la distanza single link");
                }else{
                    scelta = 2;
                    editMessage(utente, message_id, "Hai scelto la distanza average link");
                }   

                printDendrogramByDistanceChoice(utente, scelta);
            
            }

        }else{
            // è stato rilevato un callback, ma l'utente non era nello stato di attessa_risposta ovvero doveva rispondedere ad un menu in quel momento
            editMessage(utente, message_id, Emoji.ERROR.getUnicode() + " Non puoi piu considerare questo messaggio !");
            sendMessage(utente, Emoji.ERROR.getUnicode() + " Non puoi rispondere a vecchi messaggi !");    
        }
      
    }


    /**
     * Gestisce l'input ricevuto dall'utente, quando esso non è nè comando telegram prestabilito nè una chiamata di callback,
     * ma è del semplice testo.
     * 
     * @param utente Utente che sta interagendo con il bot
     * @param message_text Messaggio inviato dall'utente
     * @throws IOException Se si verificano errori durante la comunicazione con  il server
     * @throws ClassNotFoundException Se si verificano errori durante la lettura della risposta del server
     */
    private void handleInput(Utente utente, String message_text) throws IOException, ClassNotFoundException{
        
        if (utente.getUserState().equals("carica_dati")) {

            /* l'utente aveva gia eseguito il comando per scegliere di caricare un dataset già presente sul db dunque il server è ancora in attesa che l'utente
             * inserisca un nome di tabella valido quindi non dobbiamo rispedire un altro 0 ma viene richiesto all'utente ripetutamente il nome della 
             * tabella della quale caricare il dataset finchè non inserisce un nome di tabella esistente */

            loadDataOnServer(utente, message_text);

        }else if(utente.getUserState().equals("caricamento_file")){

            /* l'utente aveva gia eseguito il comando per scegliere di caricare un dataset da file dunque il server è ancora in attesa che l'utente
             * inserisca un nome di file valido quindi non dobbiamo rispedire un altro 3 ma viene richiesto all'utente ripetutamente il nome  
             * di un file finchè non inserisce un file valido (esistente e con un dendrogramma salvato) */
            loadDedrogramFromFileOnServer(utente,message_text);

        } else if(utente.getUserState().equals("insertDepth")){

           /* l'utente aveva gia eseguito il comando per apprendere il dendrogramma dal db ed inserire la profondita dunque il server è ancora in attesa che l'utente
             * inserisca una profondita valida quindi non dobbiamo rispedire un altro 2 ma viene richiesto all'utente ripetutamente  
             * la profondita finchè non inserisce una valida */

            insertDepth(utente, message_text);

        } else if(utente.getUserState().equals("salvataggio")){

            // l'utente ha inviato il nome del file sul quale eseguire il salvataggio

            saveDendrogramOnFile(utente, message_text);


        } else if(utente.getUserState().equals("inserimento_nome_nuova_tabella")){

            /* l'utente aveva già inserito il comando per inserire un nuovo dataset quindi il server è ancora in attesa di un nome del dataset
             * dunque non dobbiamo rispedire un altro 1 ma viene richiesto all'utente di inserire un nome di tabella nuovo finche
             * non inserisce uno non ancora esistente.
             */
             
            insertNewTableName(utente, message_text);

        }else if(utente.getUserState().equals("inserimento_numero_esempi")){

            // l'utente inserisce il numero di esempi che desidera avere in ogni transizione nel nuovo dataset

            insertNumberOfExamples(utente,message_text);

        }else if(utente.getUserState().equals("inserimento_dataset")){

            // l'utente invia la transizione da inserire nel nuovo dataset

            insertTransition(utente,message_text);

        }else if(utente.getUserState().equals("continua_inserimento")){

            // l'utente continua ad inserire transizioni nel nuovo dataset

            utente.getConnection().getObjectOutputStream().writeObject(message_text);
            // il messaggio ricevuto dall'utente contiene la risposta alla domanda se l'utente vuole continuare ad inserire un'altra transizione   

            if(message_text.equalsIgnoreCase("si")){

                // se l'utente risponde si continua l'inserimento di transizioni

                sendMessage(utente, "Inserisci la prossima transizione, rispettando sempre il formato specificato precedentemente");
                
                System.out.println(currentDate()+" - Utente : (" + utente.getUserName() + ") , stato : (" + utente.getUserState()+ ") --> (inserimento_dataset)");
                utente.setUserState("inserimento_dataset");


            }else if (message_text.equalsIgnoreCase("no")){

                // se l'utente risponde no, il server risponde con un messaggio OK DATASET per far capire
                // che il dataset è stato costruito correttamente sul database

                String risposta= (String) (utente.getConnection().getObjectInputStream().readObject());
 
                if(risposta.equals("OK DATASET")){  // il dataset è stato caricato correttamente sul db

                    sendMessage(utente, Emoji.SUCCESS.getUnicode() + " Il dataset è stato creato correttamente sul database ed è stato caricato per essere utilizzato.");
                    sendChoiceLoad(utente, "Come desideri caricare il Dendrogramma ? ");
                    
                    System.out.println(currentDate()+" - Utente : (" + utente.getUserName() + ") , stato : (" + utente.getUserState()+ ") --> (attesa_risposta)");
                    utente.setUserState("attesa_risposta");

                }else{

                    utente.disconnect();
                    System.out.println(currentDate()+" - Utente : (" + utente.getUserName() + ") , scollegato correttamente dal server");
                    sendMessage(utente, Emoji.ERROR.getUnicode()+" Si sono verificati degli errori durante l'inserimento del dataset, sei stato disconnesso per favore riconnettiti tramite il comando /connect");
                }

                
            }else{

                // se l'utente risponde con un messaggio che non è nè si nè no, il server richiede di inserire una risposta finche l'utente non inserisce si o no
                String risposta = (String) (utente.getConnection().getObjectInputStream().readObject());
                sendMessage(utente, Emoji.ERROR.getUnicode() + risposta);
                System.out.println(currentDate()+" - Utente : (" + utente.getUserName() + ") , stato : (" + utente.getUserState()+ ") -->  rimane ("+ utente.getUserState()+")");
            }


        } else if(utente.getUserState().equals("elimina_dataset")){

            /* l'utente aveva gia eseguito il comando per eliminare un dataset dal db dunque il server è ancora in attesa che l'utente
             * inserisca un nome di tabella valido da eliminare quindi non dobbiamo rispedire un altro 5 ma viene richiesto all'utente ripetutamente il nome della 
             * tabella da eliminare finchè non inserisce un nome di tabella esistente */
            deleteDataOnServer(utente, message_text);

        } else if(utente.getUserState().equals("file_caricato") || utente.getUserState().equals("file_salvato")){

            sendMessage(utente, "Hai già caricato il dataset e stampato il Dendrogramma correttamente, se vuoi ricominciare l'esecuzione esegui il comando /restart " + Emoji.RESTART.getUnicode());
        
        } else if(utente.getUserState().equals("attesa_risposta")){   // è presente un menu a bottoni in cui l'utente non ha ancora effettuato una scelta
            
          
            sendMessage(utente, Emoji.STOP.getUnicode() + " Prima di procedere effettua una scelta al messaggio precedente.");

        } else{ 
            // in tutti gli altri casi rispediamo quello che ha inserito specificando che il comando non è riconosciuto
            sendMessage(utente, Emoji.STOP.getUnicode() + " Comando non riconosciuto : "+message_text);
           
        }
    }

    /**
     * Invia al server il nome della tabella di cui si vuole ricavare il dataset, se esistente viene inviato all'utente il menu per
     * scegliere come desidera caricare il Dendrogramma e viene aggiornato il suo stato ad "attesa_risposta", altrimenti
     * viene richiesto di inserire un nome valido.
     * 
     * @param utente Utente che sta interagendo con il bot
     * @param tableName Nome della tabella del db da cui ricavare il dataset
     * @throws IOException Se si verificano errori durante la comunicazione con  il server
     * @throws ClassNotFoundException Se si verificano errori durante la lettura della risposta del server
     */
    private void loadDataOnServer(Utente utente, String tableName) throws IOException, ClassNotFoundException {

        sendMessage(utente, Emoji.LOADING.getUnicode() + " Processo caricamento dataset in corso...");

        utente.getConnection().getObjectOutputStream().writeObject(tableName); // inviamo al server il nome della tabella di cui ricavare il dataset
		String risposta= (String) (utente.getConnection().getObjectInputStream().readObject());

		if(risposta.equals("OK")){	

            sendMessage(utente, Emoji.SUCCESS.getUnicode() + " Il dataset è stato caricato correttamente.");

            System.out.println(currentDate()+" - Utente : (" + utente.getUserName()+ ") : caricato correttamente il dataset");
            utente.setUserState("attesa_risposta");
            System.out.println(currentDate()+" - Utente : (" + utente.getUserName()+ ") , stato : (" + utente.getUserState()+ ") --> (attesa_risposta)"); 
                
            sendChoiceLoad(utente, "Come desideri caricare il Dendrogramma ? ");
                

        }else{

            sendMessage(utente, Emoji.ERROR.getUnicode() + " "+risposta);

            System.out.println(currentDate()+" - Utente : (" + utente.getUserName()+ ") , stato : (" + utente.getUserState()+ ") -- > rimane ("+utente.getUserState()+")");
            System.out.println(currentDate()+" - Utente : ("+ utente.getUserName()+") , risposta dal sever : "+risposta);
                
            sendMessage(utente, "Inserisci nuovamente il nome della tabella :");  // chiediamo di reinserire un altro nome di tabella
        }

       
    }

    /**
     * Invia al server il nome del file del quale l'utente vuole recuperare il Dendrogramma salvato in esso, se valido viene
     * inviato all'utente un messaggio contenente il Dendrogramma letto dal file, viene aggiornato il suo stato a "file_caricato"
     * e viene chiesto se desidera riavviare l'esecuzione, altimenti viene richiesto di inserire un nome di un file valido. 
     *
     * @param utente Utente che sta interagendo con il bot
     * @param fileName Nome del file su cui è presente il clustering che si vuole caricare
     * @throws IOException Se si verificano errori durante la comunicazione con  il server
     * @throws ClassNotFoundException Se si verificano errori durante la lettura della risposta del server
     */
    private void loadDedrogramFromFileOnServer(Utente utente, String fileName) throws IOException, ClassNotFoundException {
        
        sendMessage(utente, Emoji.LOADING.getUnicode() +" Processo di caricamento file in corso...");
              
        utente.getConnection().getObjectOutputStream().writeObject(fileName);   // spediamo al server il nome del file
        String risposta = (String) utente.getConnection().getObjectInputStream().readObject();

        if (risposta.equals("OK")) {    

            sendMessage(utente, Emoji.SUCCESS.getUnicode() +" Dendrogramma caricato con successo : ");
    
            String dendrogramma = (String) utente.getConnection().getObjectInputStream().readObject();
            sendMessage(utente, dendrogramma);  // inviamo all'utente un messaggio contenete il Dendrogramma caricato dal file
                
            System.out.println(currentDate()+" - Utente : (" + utente.getUserName()+ ") , stato : (" + utente.getUserState()+ ") --> (file_caricato)"); 
            utente.setUserState("file_caricato");

            sendMessage(utente, "Se vuoi ricominciare l'esecuzione esegui il comando /restart "+ Emoji.RESTART.getUnicode());
    
        } else {  // se il server risponde con un messaggio di errore

            sendMessage(utente, Emoji.ERROR.getUnicode() + " " +risposta);    // invio all'utente l'errore generato

            System.out.println(currentDate()+" - Utente : (" + utente.getUserName()+ ") , stato : (" + utente.getUserState()+ ") --> rimane ("+ utente.getUserName()+")"); 
            System.out.println(currentDate()+" - Utente : ("+ utente.getUserName()+") , risposta dal sever : "+risposta); // stampo il messaggio di errore sul terminale
                
            sendMessage(utente, "Per favore inserisci un file valido.");
        }
            
           
    }
    
    /**
     * Invia al server la profodondità scelta dall'utente per effettuare il clustering, se corretta viene inviato all'utente il menu
     * per scegliere il tipo di distanza che desidera e viene il suo stato ad "attesa_risposta", altrimenti
     * viene richiesto di inserire una profondità valida.
     * 
     * @param utente Utente che sta interagendo con il bot
     * @param message_text Messaggio inviato dall'utente
     * @throws IOException Se si verificano errori durante la comunicazione con  il server
     * @throws ClassNotFoundException Se si verificano errori durante la lettura della risposta del server
     * 
     */
    private void insertDepth(Utente utente,String message_text) throws IOException, ClassNotFoundException{
            
		utente.getConnection().getObjectOutputStream().writeObject(message_text); // il client invia al server la profondita

		String risposta= (String) (utente.getConnection().getObjectInputStream().readObject());

		if(risposta.equals("OK")){	

            System.out.println(currentDate()+" - Utente : ("+ utente.getUserName()+" : profondita inserita correttamente");
            sendChoiceDistance(utente, "Scegli il tipo di distanza");
            utente.setUserState("attesa_risposta");

        }else{

            sendMessage(utente, Emoji.ERROR.getUnicode() + " " +risposta); // inviamo all'utente il messaggio di errore

            System.out.println(currentDate()+" - Utente : (" + utente.getUserName()+ ") , stato : (" + utente.getUserState()+ ") -->  rimane ("+utente.getUserState()+")"); 
            System.out.println(currentDate()+" - Utente : ("+ utente.getUserName()+" , risposta dal server : "+risposta); // stampiamo sul terminale il messaggio di errore
                
            sendMessage(utente, "Per favore inserisci una profondita valida.");
        }

    } 

    /**
     * Invia all'utente un messaggio contenente il Dendrogramma costruito, a seconda del tipo di distanza specificata in input, in 
     * seguito viene chiesto all'utente il nome del file su cui desiderara effetturare il salvataggio e viene aggioranto il suo
     * stato a "salvataggio"
     * 
     * @param utente Utente che sta interagendo con il bot
     * @param scelta Scelta della distanza tra (1) SingleLinkDistance e (2) AverageLinkDistance
     * @throws IOException Se si verificano errori durante la comunicazione con  il server
     * @throws ClassNotFoundException Se si verificano errori durante la lettura della risposta del server
     */
    private void printDendrogramByDistanceChoice(Utente utente,int scelta) throws IOException, ClassNotFoundException{

        sendMessage(utente, Emoji.LOADING.getUnicode() +" Processo di recupero e stampa del Dendrogramma....");

        utente.getConnection().getObjectOutputStream().writeObject(scelta);  // inviamo il server il tipo di distanza scelta

		String risposta = (String) (utente.getConnection().getObjectInputStream().readObject());

		if (risposta.equals("OK")) {  

            sendMessage(utente, Emoji.SUCCESS.getUnicode() + " Dendrogramma caricato con successo : ");

            String dendrogramma = (String) utente.getConnection().getObjectInputStream().readObject();  // inviamo all'utente un messagio contente il clustering
            sendMessage(utente, dendrogramma);

            System.out.println(currentDate()+" - Utente : (" + utente.getUserName()+ ") , stato : (" + utente.getUserState()+ ") --> (salvataggio)");  
            utente.setUserState("salvataggio");

            sendMessage(utente, Emoji.DB.getUnicode() + " Inserisci il nome dell'archivio su cui salvare il Dendrogramma (compreso di estensione)");
    
        } else {

            sendMessage(utente, Emoji.ERROR.getUnicode() + " "+risposta);   

            System.out.println(currentDate()+" - Utente : (" + utente.getUserName()+ ") , stato : (" + utente.getUserState()+ ") --> rimane  ("+utente.getUserState()+")");
            System.out.println(currentDate()+" - Utente : ("+ utente.getUserName()+") , risposta dal sever : "+risposta); // stampo il messaggio di errore sul terminale
                 
        }
    }

    /**
     * Invia al server il nome del file scelto dall'utente su cui effettuare il salvataggio del Dendrogramma, in seguito viene chiesto 
     * all'utente se desidera riavviare l'esecuzione e viene aggiornato il suo stato a "file_salvato"
     * 
     * @param utente Utente che sta interagendo con il bot
     * @param message_text Nome del file su cui effettuare il salvataggio
     * @throws IOException Se si verificano errori durante la comunicazione con  il server
     */
    private void saveDendrogramOnFile(Utente utente, String message_text) throws IOException {
        
        sendMessage(utente, Emoji.LOADING.getUnicode() + " Processo di salvataggio file in corso...");

        utente.getConnection().getObjectOutputStream().writeObject(message_text);  // inviamo al server il nome del file su cui l'utente vuole effettuare il salvataggio

        sendMessage(utente, Emoji.SUCCESS.getUnicode() +" Il Dendrogramma è stato salvato con successo nel file : "+message_text);

        System.out.println(currentDate()+" - Utente : (" + utente.getUserName()+ ") , stato : (" + utente.getUserState()+ ") --> (file_salvato)");  
        utente.setUserState("file_salvato");
        
        sendMessage(utente, "Se vuoi ricominciare l'esecuzione esegui il comando /restart "+ Emoji.RESTART.getUnicode());
    }


    /** Invia al server il nome della nuova tabella che l'utente ha scelto di creare sul database, se è un nome valido viene chiesto
     * all'utente di proseguire inserendo il numero di transizioni che la tabella conterrà e viene aggiornato il suo stato ad 
     * "inserimento_numero_esempi", altrimenti viene richiesto di inserire un nome valido.
     * 
     * @param utente Utente che ha inviato il nome della nuova tabella da inserire nel database.
     * @param tableName Nome della tabella che l'utente desidera inserire nel databse.
     * @throws IOException Se si verificano errori durante la comunicazione con  il server
     * @throws ClassNotFoundException Se si verificano errori durante la lettura della risposta del server
     */
    private void insertNewTableName(Utente utente, String tableName) throws IOException, ClassNotFoundException {

        sendMessage(utente, Emoji.LOADING.getUnicode() + " Processo di inserimento nuovo dataset in corso...");

		utente.getConnection().getObjectOutputStream().writeObject(tableName); // inviamo al server il nome della tabella di cui ricavare il dataset

		String risposta= (String) (utente.getConnection().getObjectInputStream().readObject());

		if(risposta.equals("OK")){	

            sendMessage(utente, Emoji.SUCCESS.getUnicode() + " Il nome del dataset inserito è disponibile, puoi procedere");

            System.out.println(currentDate()+" - Utente : (" + utente.getUserName()+ ") : nuovo nome trovato con successo");
            System.out.println(currentDate()+" - Utente : (" + utente.getUserName()+ ") , stato : (" + utente.getUserState()+ ") --> (inserimento_numero_esempi)"); 
            utente.setUserState("inserimento_numero_esempi");
                
            sendMessage(utente, "Inserisci il numero di esempi per ogni transizione del dataset");

        }else{ 

            sendMessage(utente, Emoji.ERROR.getUnicode() + " " + risposta);

            System.out.println(currentDate()+" - Utente : (" + utente.getUserName()+ ") , stato : (" + utente.getUserState()+ ") --> rimane ("+utente.getUserState()+")"); 
            System.out.println(currentDate()+" - Utente : ("+ utente.getUserName()+") , risposta dal sever : "+risposta);
                
            sendMessage(utente, "Inserisci nuovamente il nome della nuova tabella :"); 
        }
    }


    /**
     * Invia al server il numero di esempi che saranno contenuti in ogni transizione nel dataset del database
     * che l'utente ha scelto di creare, se valido viene chiesto all'utente di inziare ad inserire la prima transizione e viene 
     * aggioranto il suo stato ad "inserimento_dataset", altrimenti viene richiesto di inserire un numero di esempi valido.
     * 
     * @param utente Utente che sta inviando al server il numero di esempi
     * @param message_txt Messaggio inviato dall'utente, rappresenta il numero di esempi
     * @throws IOException Se si verificano errori durante la comunicazione con  il server
     * @throws ClassNotFoundException Se si verificano errori durante la lettura della risposta del server
     */
    private void insertNumberOfExamples(Utente utente,String message_txt) throws IOException, ClassNotFoundException{

        utente.getConnection().getObjectOutputStream().writeObject(message_txt);
        String risposta = (String) (utente.getConnection().getObjectInputStream().readObject());

        if(risposta.equals("OK")){	

            sendMessage(utente, "Numero di esempi per ogni transizione inviato correttamente al server.");

            String risposta_creazione_tabella_db = (String) (utente.getConnection().getObjectInputStream().readObject());

            if(risposta_creazione_tabella_db.equals("OK")){	

                sendMessage(utente, Emoji.SUCCESS.getUnicode() + " La tabella è stata creata correttamente sul database.");

                System.out.println(currentDate()+" - Utente : (" + utente.getUserName()+ ") : creata correttamente tabella sul db");
                System.out.println(currentDate()+" - Utente : (" + utente.getUserName()+ ") , stato : (" + utente.getUserState()+ ") --> (inserimento_dataset)"); 
                utente.setUserState("inserimento_dataset");
                    
                // costruiamo il formato x1,x2,..xn considerando il numero corretto di esempi scelti dall'utente
                String formato = "";
                int num = Integer.parseInt(message_txt);
                for(int i=1;i<=num;i++){
                    formato += "x"+i;
                    if(i < num) {
                        formato += ",";
                    }  
                }

                sendMessage(utente, "Inizia ad inserire la prima transizione da inserire nel dataset, ricorda che ogni transizione deve essere nel formato : "+formato);
               
            }else{  // si verificano errori durante la creazione della tabella sul db

                sendMessage(utente, Emoji.ERROR.getUnicode() + " "+risposta_creazione_tabella_db); 

                utente.disconnect();
                System.out.println(currentDate()+" - Utente : (" + utente.getUserName() + ") , scollegato correttamente dal server");
                    
                sendMessage(utente, "Sei stato disconnesso dal server, se vuoi continuare prima di tutto dovrai riconnetterti tramite il comando /connect");
            }

        }else{  // l'utente inserisce un numero di esempi non valido (ad esempio 0, un numero negativo, o un formato non numerico)

            sendMessage(utente, Emoji.ERROR.getUnicode() + " "+risposta);

            System.out.println(currentDate()+" - Utente : (" + utente.getUserName()+ ") , stato : (" + utente.getUserState()+ ") --> rimane  ("+utente.getUserState()+")"); 
            System.out.println(currentDate()+" - Utente : ("+ utente.getUserName()+" , risposta dal server : "+risposta); 
                 
            sendMessage(utente, "Inserisci un nuovo numero di esempi");
        }
  
    }


    /** Invia al server una stringa rappresentante una transizione da inserire nella tabella che si sta creando nel database, 
     *  se valida viene chiesto all'utente se vuole continuare ad inserire altre transizioni, altrimenti viene richiesto di inserire
     *  una transizione valida.
     * 
     * @param utente Utente che sta inserendo la transizione
     * @param transizione Messaggio inviato dall'utente, stringa rappresentate la transizione
     * @throws IOException Se si verificano errori durante la comunicazione con  il server
     * @throws ClassNotFoundException Se si verificano errori durante la lettura della risposta del server
     */
    private void insertTransition(Utente utente,String transizione) throws ClassNotFoundException, IOException{

        utente.getConnection().getObjectOutputStream().writeObject(transizione);
        String risposta = (String) (utente.getConnection().getObjectInputStream().readObject());

        if(risposta.equals("OK")){

            sendMessage(utente, Emoji.SUCCESS.getUnicode() + " La transizione è stata aggiunta correttamente al dateset");
            sendMessage(utente, "Vuoi continuare ad inserire altre transizioni ? (Si/No)");

            System.out.println(currentDate()+" - Utente : (" + utente.getUserName()+ ") , stato : (" + utente.getUserState()+ ") --> (continua_inserimento)"); 
            utente.setUserState("continua_inserimento");

        }else{

            sendMessage(utente, Emoji.ERROR.getUnicode() +" "+risposta);

            System.out.println(currentDate()+" - Utente : (" + utente.getUserName()+ ") , stato : (" + utente.getUserState()+ ") -->  rimane ("+utente.getUserState()+")");
                
            sendMessage(utente, "Per favore inserisci una nuova transizione rispettando il formato specificato");
                
        }
    }

    /**
     * Invia al server il nome della tabella di cui l'utente vuole eliminare il dataset, se esiste viene eliminato il dataset
     * corrispodente dal database e viene chiesto all'utente se desidera riavviare l'esecuzione, 
     * altrimenti viene richiesto di inserire un nome di dataset esistente.
     * 
     * @param utente Utente che sta eliminando il dataset
     * @param tableName Nome della tabella che l'utente ha scelto di eliminare
     * @throws IOException Se si verificano errori durante la comunicazione con  il server
     * @throws ClassNotFoundException Se si verificano errori durante la lettura della risposta del server
     * 
     */
    private void deleteDataOnServer(Utente utente, String tableName) throws IOException, ClassNotFoundException {

        sendMessage(utente, Emoji.LOADING.getUnicode() +" Processo eliminazione dataset in corso...");

        utente.getConnection().getObjectOutputStream().writeObject(tableName); // inviamo al server il nome della tabella che vuole eliminare
		String risposta= (String) (utente.getConnection().getObjectInputStream().readObject());

		if(risposta.equals("OK")){	

            sendMessage(utente, Emoji.SUCCESS.getUnicode() +" Il dataset è stato eliminato con successo dal database.");

            System.out.println(currentDate()+" - Utente : (" + utente.getUserName()+ ") : eliminato correttamente il dataset");
            System.out.println(currentDate()+" - Utente : (" + utente.getUserName()+ ") , stato : (" + utente.getUserState()+ ") --> (default)"); 
            utente.setUserState("default");
                
            sendMessage(utente, "Se desideri ricominciare l'esecuzione esegui il comando /restart "+ Emoji.RESTART.getUnicode());

        }else{

            sendMessage(utente, Emoji.ERROR.getUnicode() + " Il nome della tabella che hai inserito non esiste nel database.");

            System.out.println(currentDate()+" - Utente : (" + utente.getUserName()+ ") , stato : (" + utente.getUserState()+ ") -->  rimane ("+utente.getUserState()+")");
            System.out.println(currentDate()+" - Utente : ("+ utente.getUserName()+") , risposta dal sever : "+risposta);

            sendMessage(utente, "Inserisci nuovamente il nome della tabella da eliminare :");  // chiediamo di reinserire un altro nome di tabella

        }
            
    }


    /**
     * Invia un messaggio all'utente, contenente il testo specificato in input.
     * 
     * @param utente Utente che sta interagendo con il bot
     * @param msg Testo del messaggio da inviare
     */
    private void sendMessage( Utente utente, String msg) {

        SendMessage message = new SendMessage();
        message.setChatId(utente.getChatId());
        message.setText(msg);

        try {
            execute(message);
        }catch (TelegramApiException e) {
            System.out.println("Errore durante l'invio di un messaggio all'utente ("+utente.getUserName()+") : " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Modifica il testo di un messaggio già inviato, di id specificato, con il nuovo testo specificato in input.
     * 
     * @param utente Utente che sta interagendo con il bot
     * @param message_id Id del messaggio da modificare
     * @param text Nuovo testo del messaggio
     * 
     */
    private void editMessage(Utente utente, long message_id, String text) {

        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(utente.getChatId());
        editMessage.setMessageId(toIntExact(message_id));
        editMessage.setText(text);

        try {
            execute(editMessage);
        } catch (TelegramApiException e) {
            System.out.println("Errore durante l'invio di un messaggio all'utente ("+utente.getUserName()+") : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /** Invia all'utente un messaggio ed un menu composto da 3 bottoni, uno per scegliere di caricare un dataset già presente 
     *  sul databse, uno per inserire un nuovo dataset nel database, ed uno per eliminare un dataset esistente.
     * 
     * @param utente Utente che sta interagendo con il bot
     * @param text Testo del messaggio prima del menu a  bottoni
     */
    private void sendChoiceDataset(Utente utente, String text) {  

        SendMessage message = new SendMessage();
        message.setChatId(utente.getChatId());
        message.setText(text);
        
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton in_btn1 = new InlineKeyboardButton();
        in_btn1.setText("Carica dataset esistente " + Emoji.FOLDER.getUnicode());  
        in_btn1.setCallbackData("call_back_carica_dataset");
        
        InlineKeyboardButton in_btn2 = new InlineKeyboardButton();
  
        in_btn2.setText("Crea nuovo dataset "+ Emoji.NEW.getUnicode());  
        in_btn2.setCallbackData("call_back_crea_nuovo_dataset");
        
        InlineKeyboardButton in_btn3 = new InlineKeyboardButton();
        in_btn3.setText("Elimina dataset "+ Emoji.BIN.getUnicode());  
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
            System.out.println("Errore durante l'invio di un messaggio all'utente (" + utente.getUserName() + ") : " + e.getMessage());
            e.printStackTrace();
        }
    }
    

    /** Invia all'utente un messaggio ed un menu composto da 2 bottoni, uno per scegliere di caricare il Dendrogramma da file 
     * e l'altro per apprendere il Dendrogramma da database.
     * 
     * @param utente Utente che sta interagendo con il bot
     * @param text Testo del messaggio prima del menu a bottoni
     */
    private void sendChoiceLoad(Utente utente, String text) {  

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
            System.out.println("Errore durante l'invio di un messaggio all'utente ("+utente.getUserName()+") : " + e.getMessage());
            e.printStackTrace();
        }
    }


    /** Invia all'utente un messaggio ed un menu composto da 2 bottoni, uno per scegliere la distanza SingleLink e
     *  l'altro per AverageLink
     * 
     * @param utente Utente che sta interagendo con il bot
     * @param text Testo del messaggio prima del menu a bottoni
     */
    private void sendChoiceDistance(Utente utente, String text) {  

        SendMessage message = new SendMessage();
        message.setChatId(utente.getChatId());
        message.setText(text);
        
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton in_btn1 = new InlineKeyboardButton();
        in_btn1.setText("Single Link Distance " + Emoji.CHAIN.getUnicode());
        in_btn1.setCallbackData("call_back_single_link");

        InlineKeyboardButton in_btn2 = new InlineKeyboardButton();
        in_btn2.setText("Average Link Distance " + Emoji.AVERAGE.getUnicode());
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
            System.out.println("Errore durante l'invio di un messaggio all'utente ("+utente.getUserName()+") : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Restituisce la data e l'ora corrente.
     * @return Stringa che rappresenta la data e l'ora corrente.
     */
    private String currentDate(){
        
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

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
    // (fare come il nome della tabella)


    /*TODO : per aggiungere dataset da telegram chiedere il nome della tabella che si vuole inserire e spedirlo al server, il server esegue la query sul db SHOW TABLES FROM MAPDB e scandisce il risultato per controllare
         se il nome dato in input esiste gia, se si rispedisce 0 e viene chiesto all'utente di inserire un altro nome, se è un nuovo nome, gli si viene chiesto il numero di valori
         per ogni transizione ed il numero di transizioni, e vengono inseriti i dati inserendo una collezioni di transizioni alla volta
*/

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
        System.out.println("\n" +data_corrente()+" - Il bot è ora attivo per rispondere ai messaggi");  // stampa sul terminale
    }


    /*  Lista comandi
     *  /start   -  l'utente ha iniziato la conversazione, viene stampato il messagio di benvenuto e viene chiesto di connetersi al server
     *  /connect   -  inizializza la connessione dell'utente (client) al server
     *  /data   - usato per caricare il dataset, viene chiesto all'utente di inserire il nome della tabella da cui ricavare il dataset
     *  /scelta  - viene chiesto di effettuare la scelta tra il caricamento da file o apprendimento dal db
     *  /distanza - viene chiesto di effettuare la scelta della distanza tra single link o average link
     *  /restart  - viene chiusa la connessione al server e riaperta automanticamente, in questo modo viene scartato il dataset caricato
     * 
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

                invia_messaggio("Benvenuto su map, perfavore collegati al server tramite il comando /connect", utente);
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
                    invia_messaggio("Connessione con il server andanta a buon fine, puoi inziare a caricare il dataset tramite il comando /data", utente);
                }catch(IOException e){
                    System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+" , Eccezione : "+e.getMessage());
                    invia_messaggio("La connessione al server non è andata a buon fine, perfavore verifica il server sia online e riprova /connect", utente);
                }

                
            }else if (message_text.equals("/data")) {

                if(!utente.getConnesso()){
                    invia_messaggio("Non sei ancora collegato ancora al server, perfavore connettiti eseguendo il comando /connect", utente);
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
                    invia_messaggio("Non sei ancora collegato ancora al server, perfavore connettiti eseguendo il comando /connect", utente);
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
                    invia_messaggio("Non sei ancora collegato ancora al server, perfavore connettiti eseguendo il comando /connect", utente);
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
                    invia_messaggio("Non sei ancora collegato ancora al server, perfavore connettiti eseguendo il comando /connect", utente);
                    return;
                }
                
                try{
                    scollega(utente);
                    collega(utente,"127.0.0.1", 8080);
                    invia_messaggio("La connessione è stata riavviata con successo, puoi proseguire con il caricamento del dataset tramite il comando /data", utente);

                }catch(IOException e){
                    System.out.println(data_corrente()+" - Utente : ("+ utente.getNomeUtente()+") , Eccezione : "+e.getMessage());
                    invia_messaggio(e.getMessage(), utente);
                }

               
            }else{   
                // se non stati eseguiti comandi, ma è stato ricevuto del testo dobbiamo controllare 
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
        

        if (utente.getStato() != null && utente.getStato().equals("carica_dati")) {

            loadDataOnServer(utente, message_text);

        }else if(utente.getStato().equals("caricamento_file")){

            loadDedrogramFromFileOnServer(utente,message_text);

        }  else if(utente.getStato().equals("file_caricato") || utente.getStato().equals("file_salvato")){
            invia_messaggio("Hai già caricato il dataset e stampato il Dendrogramma correttamente, se vuoi ricominciare l'esecuzione esegui il comando /restart", utente);
            
            //utente.setStato("default");

        } else if(utente.getStato().equals("inserisci_profondita")){

            if(controlla_profondita(utente,message_text)){  
                invia_scelta_distanza(utente, "Scegli il tipo di distanza");
                utente.setStato("attesa_risposta");
            }

            // altrimenti rimane nello stesso stato e viene richiesto di inserire la profondita
            
        } else if(utente.getStato().equals("salvataggio")){
            salva_file(utente,message_text);

        } 
     
        else{ // in tutti gli altri casi rispediamo quello che ha inserito
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
            invia_messaggio("Non puoi rispondere a vecchi messaggi ! Perfavore inzia connettendoti al server tramite il comando /connect", temp_utente);
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
            
            utente.getConnessione().getObjectOutputStream().writeObject(2);      
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
                
                invia_messaggio("Sei stato disconneso dal server, se vuoi continuare prima di tutto dovrai riconneterti tramite il comando /connect", utente);
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
     * Invia al server il nome della tabella di cui si vuole ricavare i
     * viene cambiato lo stato dell'utente in scelta, in modo da farli effettuare la scelta tra caricamento da File o apprendimento da Db, se la ricerca della tabella sul db
     * non va a buon fine viene inviato all'utente il messaggio di errore generato e gli si viene chiesto di inserire un nuovo nome di tabella.
     * 
     * @param utente Utente che sta interagendo con il bot
     * @param tableName Nome della tabella del db da cui ricavare il dataset
     */
    private void loadDataOnServer(Utente utente, String tableName) {

        invia_messaggio("Processo caricamento dataset in corso...", utente);
       
        try {

            utente.getConnessione().getObjectOutputStream().writeObject(0);
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
                System.out.println(data_corrente()+" - Utente : (" + utente.getNomeUtente()+ ") , stato : (" + utente.getStato()+ ") --> rimane  ("+utente.getStato()+")"); 
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
            utente.getConnessione().getObjectOutputStream().writeObject(1);
		    utente.getConnessione().getObjectOutputStream().writeObject(message_text); // inviamo al server la profondita

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
                invia_messaggio("Sei stato disconneso dal server, se vuoi continuare prima di tutto dovrai riconneterti tramite il comando /connect", utente);
            }

       } catch (Exception e) {
        System.out.println(data_corrente()+" - Utente : ("+utente.getNomeUtente()+") , Eccezione : " + e.getMessage());
            try{
                scollega(utente);
                invia_messaggio("Qualcosa è andato storto durante la comunicazione con il server, perfavore riconnettiti tramite il comando /connect", utente);
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

                invia_messaggio("Inserisci il nome dell'archio su cui salvare il Dendrogramma (compreso di estensione)",utente);
    
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
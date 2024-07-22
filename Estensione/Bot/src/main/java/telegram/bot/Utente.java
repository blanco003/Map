package telegram.bot;

import java.io.IOException;

/**
 * Classe che rappresenta l'utente che sta interagendo con il Bot, e che comunica con il Server.
 */
public class Utente {

    /** Id della chat dell'utente con il bot */
    private long chat_id;

    /** Nome utente telegram */
    private String userName;

    /** Oggetto Connessione, rappresenta la connessione dell'utente con il Server */
    private Connessione conn;

    /** Stato in cui si trova l'utente */
    private String userState;


    /*  Stati possibili per l'utente durante la comunicazione con il Bot
     *  
     *  default : l'utente ha iniziato la conversazione con il bot
     *  carica_dati : l'utente ha scelto di caricare un dataset già esistente sul database
     * 
     *  caricamento_file : l'utente ha scelto di caricare il dendrogramma da file
     *  file_caricato : l'utente ha caricato il file correttamente e stampato il dendrogramma (non puo fare piu niente se non ricominciare)
     *  
     *  inserisci_profondita : l'utente ha scelto di apprendere il dendrogramma dal db e ora deve inserire la profondita
     *  scelta_distanza : l'utente ha inserito la profondita correttamente e gli viene chiesto di scegliere tra distanza tra single e average link
     *  salvataggio : l'utente deve inserire il nome del file su cui effettuare il salvataggio del dendrogramma
     *  file_salvato : : l'utente ha salvato il file correttamente e stampato il dendrogramma (non puo fare piu niente se non ricominciare) 
     *  
     *  attesa_risposta : l'utente ha eseguito un comando per stampare un menu a bottoni ma non ha ancora premuto nessun bottone
     *  
     *  inserimento_nome_nuova_tabella : l'utente ha scelto di creare un nuovo dataset sul db e gli si viene chiesto il nome della nuova tabella
     *  inserimento_numero_esempi : l'utente ha inserito un nuovo nome di dataset valido e gli si viene chiesto di inserire il numero di esempi di ogni transizione del nuovo dataset
     *  inserimento_dataset : l'utente inserisce una transizione da inserire nel nuovo dataset
     *  continua_inserimento : viene chiesto all'utente se vuole continuare ad inserire transizioni al nuovo dataset
     *  
     *  elimina_dataset : l'utente ha scelto di eliminare un dataset esistente e gli si viene chiesto il nome del dataset da eliminare
     */


    /**
     * Costruttore Utente.
     * @param chat_id Id della chat dell'utente
     * @param userName Nome utente dell'utente che sta comunicando con il Bot
     * @param conn Oggetto connessione, rappresenta la connsessione dell'utente (client) al server
     * @param statUtente Stato in cui si trova l'utente durante la comunicazione con il bot
     */
    Utente(long chat_id, String userName, Connessione conn, String statUtente) {
        this.chat_id = chat_id;
        this.userName = userName;
        this.conn = conn;
        this.userState = statUtente;

    }

    /**
     * Restituisce l'id della chat dell'utente con il bot.
     * @return Chat id dell'utente con il bot.
     */
    long getChatId() {
        return chat_id;
    }

    /**
     * Restituisce il nome dell'utente che interagisce con il bot.
     * @return Nome dell'utente che sta interagendo con il bot.
     */
    String getUserName(){
        return this.userName;
    }

    /**
     * Restituisce lo stato in cui si trova l'utente che sta intergando con il bot.
     * @return Stato dell'utente
     */
    String getUserState(){
        return this.userState;
    }

    /**
     * Aggiorna lo stato dell'utente che sta interagendo con il bot.
     * @param newUserSate nuovo stato dell'utente
     */
    void setUserState(String newUserState){
        this.userState = newUserState;
    }    

    /**
     * Aggiorna l'oggetto Connessione, il quale rappresenta la connessione dell'utente (client) con il server.
     * @param conn Oggetto Connessione
     */
    void setConnection(Connessione conn){
        this.conn = conn;
    }

    /**
     * Restituisce l'oggetto Connessione, utile per comunicare con il server.
     * @return Oggetto connessione
     */
    Connessione getConnection(){
        return this.conn;
    }

    /**
     * Collega l'utente (client) al server.
     * @param ip Ip dove è attivo il server
     * @param PORT Porta dove è in ascolto il server
     * @throws IOException se si verificano errori durante la comunicazione con il server
     */
    void connect(String ip, Integer PORT) throws IOException{
        this.conn = new Connessione(ip, PORT);
        this.userState = "default";
    }


    /**
     * Scollega l'utente dal server.
     * @throws IOException se si verificano errori durante la comunicazione con il server
     */
    void disconnect() throws IOException{
        this.conn.scollega();
        this.conn = null;
        this.userState = "null";
    }


}
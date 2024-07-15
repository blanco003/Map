package telegram.bot;

import java.io.IOException;

/**
 * Classe che rappresenta l'utente che sta interagendo con il Bot, e che comunica con il Server.
 */
public class Utente {

    /** Id della chat dell'utente con il bot */
    private long chat_id;

    /** Nome utente telegram */
    private String nomeUtente;

    /** Oggetto Connessione, rappresenta la connessione dell'utente con il Server */
    private Connessione conn;

    /** Stato in cui si trova l'utente */
    private String statoUtente;

    /*  Stati possibili per l'utente durante la comunicazione con il Bot
     *  
     *  default : l'utente ha iniziato la conversazione con il bot
     *  carica_dati : l'utente ha scelto di caricare un dataset già esistente sul database
     *  caricamento_file : l'utente ha scelto di caricare il dendrogramma da file
     *  file_caricato : l'utente ha caricato il file correttamente e stampato il dendrogramma (non puo fare piu niente se non ricominciare)
     *  inserisci_profondita : l'utente ha scelto di apprendere il dendrogramma dal db e ora deve inserire la profondita
     *  scelta_distanza : l'utente ha inserito la profondita correttamente e gli viene chiesto di scegliere tra distanza tra single e average link
     *  salvataggio : l'utente deve inserire il nome del file su cui effettuare il salvataggio del dendrogramma
     *  file_salvato : : l'utente ha salvato il file correttamente e stampato il dendrogramma (non puo fare piu niente se non ricominciare) 
     *  attesa_risposta : l'utente ha eseguito un comando per stampare un menu a bottoni ma non ha ancora premuto nessun bottone
     *  inserimento_nome_nuova_tabella : l'utente ha scelto di creare un nuovo dataset sul db e gli si viene chiesto il nome della nuova tabella
     *  inserimento_numero_esempi : l'utente ha inserito un nuovo nome di dataset valido e gli si viene chiesto di inserire il numero di esempi di ogni transizione del nuovo dataset
     *  inserimento_dataset : l'utente inserisce una transizione da inserire nel nuovo dataset
     *  continua_inserimento : viene chiesto all'utente se vuole continuare ad inserire transizioni al nuovo dataset
     *  elimna_dataset : l'utente ha scelto di eliminare un dataset esistente e gli si viene chiesto il nome del dataset da eliminare
     */


    /**
     * Costruttore
     * @param chat_id Id della chat dell'utente
     * @param nomeUtente Nome utente dell'utente che sta comunicando con il Bot
     * @param conn Oggetto connessione, rappresenta la connsessione dell'utente (client) al server
     * @param statUtente Stato in cui si trova l'utente durante la comunicazione con il bot
     */
    Utente(long chat_id, String nomeUtente, Connessione conn, String statUtente) {
        this.chat_id = chat_id;
        this.nomeUtente = nomeUtente;
        this.conn = conn;
        this.statoUtente = statUtente;

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
    String getNomeUtente(){
        return this.nomeUtente;
    }

    /**
     * Restituisce lo stato in cui si trova l'utente che sta intergando con il bot.
     * @return Stato dell'utente
     */
    String getStato(){
        return this.statoUtente;
    }

    /**
     * Aggiorna lo stato dell'utente che sta interagendo con il bot.
     * @param nuovo_stato Nuovo stato dell'utente
     */
    void setStato(String nuovo_stato){
        this.statoUtente = nuovo_stato;
    }    

    /**
     * Aggiorna l'oggetto Connessione, il quale rappresenta la connessione dell'utente (client) con il server.
     * @param conn Oggetto Connessione
     */
    void setConnessione(Connessione conn){
        this.conn = conn;
    }

    /**
     * Restituisce l'oggetto Connessione, utile per comunicare con il server.
     * @return Oggetto connessione
     */
    Connessione getConnessione(){
        return this.conn;
    }

    /**
     * Collega l'utente al server.
     * @param ip Ip dove è attivo il server
     * @param PORT Porta dove è in ascolto il server
     * @throws IOException se si verificato errori durante la comunicazione con il server
     */
    void collega(String ip, Integer PORT) throws IOException{
        this.conn = new Connessione(ip, PORT);
        this.statoUtente = "default";
    }


    /**
     * Scollega l'utente dal server.
     * @throws IOException se si verificato errori durante la comunicazione con il server
     */
    void scollega() throws IOException{
        this.conn.scollega();
        this.conn = null;
        this.statoUtente = "null";
    }


}
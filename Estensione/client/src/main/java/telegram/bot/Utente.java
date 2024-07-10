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


    
    /*  STATI POSSIBILE PER L'UTENTE TODO scrivere meglio
     *  
     *  default : ha iniziato la conversazione
     * 
     *  carica_dati : ha scelto di caricare i dati
     *  inserimento_nome_tabella : inserisce il nome della tabella di cui vuole caricare il dataset finche non inserisce uno valido
     *  attesa_risposta : l'utente ha eseguito un comando per stampare il menu a bottoni ma non ha ancora premuto nessun bottone
     * 
     *  caricamento_file : ha scelto di caricare il file
     *  inserimento_nome_file : se l'utente sbagli ad inserire il nome del file gli si viene richiesto di inserirlo finchè non inserisce uno valido
     *  file_caricato : ha caricato il file e stampato il dendrogramma (non puo fare piu niente se non ricominciare)
     * 
     *  inserisci_profondita : ha scelto di apprendere il dendrogramma dal db e ora deve inserire la profondita
     *  controlla_pronfondita : a seconda di quale profondita ha inserito viene stampato il dendrogramma 
     *  scelta_distanza : la profondita è stata inserita correttamente e si chiede all'utente di sceliere la distanza tra single e average
     *  salvataggio : l'utente deve inserire il nome del file per il salvataggio
     *  file_caricato : ha caricato il file e stampato il dendrogramma (non puo fare piu niente se non ricominciare)
     * 
     *  aggiungi_nome_tabella : l'utente sceglie di inserire un nuovo dataset al db
     *  inserimento_nome_nuova_tabella : inserisce il nome della tabella del nuovo datasetfinche non inserisce un nome che non è gia presente nel db
     *  inserimento_numero_esempi : inserisce il numero di esempi in ogni transizione del nuovo dataset da inserire
     *  inserimento_dataset : l'utente inserisce la transizione da inserire nel nuovo dataset
     *  continua_inserimento : viene chiesto all'utente se vuole continuare ad inserire transizioni al dataset
     * 
     *  elimna_dataset : l'utente sceglie di eliminare un dataset dal db e inserisce un nome di tabella da elimnare
     *  inserimento_elimina_dataset : l'utente inserisce nuovamente un nome di tabella da eliminare perche prima qualcosa è andato storto
     */


    /**
     * Costruttore
     * @param chat_id Id della chat dell'utente
     * @param nomeUtente Nome utente dell'utente che sta comunicando con il Bot
     * @param conn Oggetto connessione, rappresenta la connsessione dell'utente (client) al server
     * @param statUtente Stato in cui si trova l'utente durante la comunicazione con il bot
     * @param dataTrovati Indica se l'utente ha gia caricato il dataset interagendo con il server o non ancora
     * @param connesso Indica se l'utente (client) è connesso con il server
     */
    Utente(long chat_id, String nomeUtente, Connessione conn, String statUtente) {
        this.chat_id = chat_id;
        this.nomeUtente = nomeUtente;
        this.conn = conn;
        this.statoUtente = statUtente;

    }

    /**
     * Restituisce l'id della chat dell'utente con il bot.
     */
    public long getChatId() {
        return chat_id;
    }

    /**
     * Restituisce il nome dell'utente che interagisce con il bot.
     */
    public String getNomeUtente(){
        return this.nomeUtente;
    }

    /**
     * Restituisce lo stato in cui si trova l'utente che sta intergando con il bot.
     */
    public String getStato(){
        return this.statoUtente;
    }

    /**
     * Aggiorna lo stato dell'utente che sta interagendo con il bot.
     * @param nuovo_stato Nuovo stato dell'utente
     */
    public void setStato(String nuovo_stato){
        this.statoUtente = nuovo_stato;
    }    

    /**
     * Aggiorna l'oggetto Connessione, il quale rappresenta la connessione dell'utente (client) con il server.
     * @param conn Oggetto Connessione
     */
    public void setConnessione(Connessione conn){
        this.conn = conn;
    }

    /**
     * Restituisce l'oggetto Connessione, utile per comunicare con il server.
     */
    public Connessione getConnessione(){
        return this.conn;
    }

    /**
     * Collega l'utente al server.
     * @param ip Ip dove è attivo il server
     * @param PORT Porta dove è in ascolto il server
     * @throws IOException se si verificato errori durante la comunicazione con il server
     */
    public void collega(String ip, Integer PORT) throws IOException{
        this.conn = new Connessione(ip, PORT);
        this.statoUtente = "default";
    }


    /**
     * Scollega l'utente dal server.
     * @throws IOException se si verificato errori durante la comunicazione con il server
     */
    public void scollega() throws IOException{
        this.conn.scollega();
        this.conn = null;
        this.statoUtente = "null";
    }


}
package telegram.bot;

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

    /** Variabile che indica se il dataset è stato caricato correttamente o se ancora deve essere stato caricato */
    private boolean dataTrovati = false;

    /**
     * Variabile che indica se l'utente è connesso con il server o la connessione deve essere ancora stabilita.
     * Altrimenti potremmo verificare se l'oggetto conn è null ma ogni volta dovremmo restituirlo ed effettuare il confronto.
     */
    public boolean connesso = false;    

    
    /*  STATI POSSIBILE PER L'UTENTE
     *  
     *  default : ha iniziato la conversazione
     * 
     *  carica_dati : ha scelto di caricare i dati
     * 
     *  inseriento_nome_tabella : inserisce il nome della tabella di cui vuole caricare il dataset finche non inserisce uno valido
     *  scelta : ha caricato con successo i dati e deve effetuare la scelta tra file / db
     *  attesa_risposta : l'utente ha eseguito un comando per stampare il menu a bottoni ma non ha ancora premuto nessun bottone
     * 
     *  caricamento_file : ha scelto di caricare il file
     *  file_caricato : ha caricato il file e stampato il dendrogramma (non puo fare piu niente se non ricomubciare)
     * 
     *  inserisci_profondita : ha scelto di apprendere il dendrogramma dal db e ora deve inserire la profondita
     *  controlla_pronfondita : a seconda di quale profondita ha inserito viene stampato il dendrogramma (TODO: SI POTREBBE TOGLIERE)
     *  scelta_distanza : la profondita è stata inserita correttamente e si chiede all'utente di sceliere la distanza tra single e average
     *  salvataggio : l'utente deve inserire il nome del file per il salvataggio
     *  file_caricato : ha caricato il file e stampato il dendrogramma (non puo fare piu niente se non ricomubciare)
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
    Utente(long chat_id, String nomeUtente, Connessione conn, String statUtente, boolean dataTrovati,
            boolean connesso) {
        this.chat_id = chat_id;
        this.nomeUtente = nomeUtente;
        this.conn = conn;
        this.statoUtente = statUtente;
        this.dataTrovati = dataTrovati;
        this.connesso = connesso;
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
     * Aggiora la variabile che rappresenta se i dati sono stati caricati correttamente.
     * @param trovati Indica se i dati sono stati caricati
     */
    public void setDataTrovati(boolean trovati){
        this.dataTrovati = trovati;
    }

    /**
     * Restituisce true se i dati sono stati caricati, false altrimenti
     */
    public boolean getDataTrovati(){
        return this.dataTrovati;
    }

    /**
     * Aggiorna la variabile che rappresenta lo stato della connessione dell'utente (client) con il server.
     * @param connesso Nuovo stato della connessione.
     */
    public void setConnesso(boolean connesso){
        this.connesso = connesso;
    }

    /**
     * Restituisce lo stato della connessione dell'utente (client) con il server.
     */
    public boolean getConnesso(){
        return this.connesso;
    }    

}
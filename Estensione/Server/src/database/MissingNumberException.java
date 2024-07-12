package database;

/**
 * La classe MissingNumberException rappresenta un'eccezione personalizzata, sollevata quando un valore di esempio risulta non specificato
 * o non è in un formato numerico.
 */
public class MissingNumberException extends Exception{
    
    /**
     * Costruisce un'eccezione MissingNumberException senza un messaggio di dettaglio.
     */
    public MissingNumberException(){} 

    /**
     * Costruisce un'eccezione MissingNumberException con un messaggio di dettaglio specificato.
     *
     * @param msg il messaggio di dettaglio
     */
	public MissingNumberException(String msg) {
        super(msg);
    }
}

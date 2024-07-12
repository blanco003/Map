package database;

/**
 * La classe EmptySetException rappresenta un'eccezione personalizzata, sollevata quando il dataset risulta vuoto.
 */
public class EmptySetException extends Exception{

    /**
     * Costruisce un'eccezione EmptySetException senza un messaggio di dettaglio.
     */
	public EmptySetException(){} 

    /**
     * Costruisce un'eccezione EmptySetException con un messaggio di dettaglio specificato.
     *
     * @param msg il messaggio di dettaglio
     */
	public EmptySetException(String msg) {
        super(msg);
    }
}

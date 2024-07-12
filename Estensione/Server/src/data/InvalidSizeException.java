package data;

/**
 * La classe InvalidSizeException rappresenta un'eccezione personalizzata, sollevata quando si prova a calcolare la distanza 
 * tra due liste di esempi di diversa dimensione.
 */
public class InvalidSizeException extends Exception {

    /**
     * Costruisce un'eccezione InvalidSizeException con un messaggio di dettaglio specificato.
     *
     * @param message il messaggio di dettaglio
     */
	public InvalidSizeException(String message) {
        super(message);
    }
}

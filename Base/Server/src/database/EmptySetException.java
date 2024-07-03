package database;

/**
 * La classe EmptySetException rappresenta un'eccezione personalizzata, da sollevare quando un'operazione su un set di dati
 * risulta in un set vuoto.
 * Questa eccezione può essere utilizzata per segnalare che il risultato di una query o di un'operazione
 * su un database non contiene dati.
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

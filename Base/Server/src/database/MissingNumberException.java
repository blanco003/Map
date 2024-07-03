package database;

/**
 * La classe MissingNumberException rappresenta un'eccezione personalizzata, sollevata quando un numero richiesto
 * è mancante o non è disponibile in un contesto specifico.
 * Questa eccezione può essere utilizzata per segnalare errori relativi a numeri mancanti in operazioni
 * o calcoli che richiedono specifici valori numerici.
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

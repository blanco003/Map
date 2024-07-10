package clustering;
/**
 * La classe InvalidDepthException rappresenta un'eccezione lanciata quando la profondità del dendrogramma
 * è maggiore del numero di esempi nel dataset.
 * Questa eccezione viene utilizzata per segnalare un errore nella configurazione del clustering gerarchico.
 */
public class InvalidDepthException extends Exception {
    /**
     * Costruisce un'eccezione InvalidDepthException con un messaggio di dettaglio specificato.
     *
     * @param message il messaggio di dettaglio
     */
    public InvalidDepthException(String message) {
        super(message);
    }
}

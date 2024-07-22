package clustering;

/**
 * La classe InvalidDepthException rappresenta un'eccezione personallizata, sollevata quando la profondità del dendrogramma
 * è maggiore del numero di esempi nel dataset.
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

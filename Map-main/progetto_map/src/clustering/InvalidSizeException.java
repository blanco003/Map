package clustering;
/**
 * La classe InvalidSizeException rappresenta un'eccezione lanciata quando il numero di esempi nel dataset
 * è inferiore alla profondità del dendrogramma.
 * Questa eccezione viene utilizzata per segnalare un errore nella dimensione del dataset durante il clustering gerarchico.
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

package data;

/**
 * La classe NoDataException rappresenta un'eccezione personallizata, sollevata quando non viene trovato correttamente 
 * il dataset o quando il dataset trovato è vuoto.
 */
public class NoDataException extends Exception{
    /**
     * Costruisce una nuova eccezione con il messaggio di dettaglio specificato.
     *
     * @param msg il messaggio di dettaglio
     */
    public NoDataException(String msg){
        super(msg);
    }
    
}

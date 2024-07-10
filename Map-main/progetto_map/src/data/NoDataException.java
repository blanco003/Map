package data;
/**
 * Eccezione personalizzata che indica l'assenza di dati.
 * Questa eccezione viene lanciata quando non sono disponibili dati necessari per una determinata operazione.
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

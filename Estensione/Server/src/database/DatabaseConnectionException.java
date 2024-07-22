package database;

/**
 * La classe DatabaseConnectionException rappresenta un'eccezione personalizata, sollevata quando si verificano dei fallimenti
 * durante la connessione al database
 */
public class DatabaseConnectionException extends Exception{
	/**
	 * Costruisce un'eccezione DatabaseConnectionException senza un messaggio di dettaglio.
	 */
	public DatabaseConnectionException(){}

	/**
     * Costruisce un'eccezione DatabaseConnectionException con un messaggio di dettaglio specificato.
     *
     * @param msg il messaggio di dettaglio
     */
	public DatabaseConnectionException(String msg){
		super(msg);
	}

}

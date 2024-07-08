package database;

/**
 * La classe DatabaseConnectionException rappresenta un'eccezione personallizata, sollevata quando si verificano dei fallimenti
 * durante la connessione al database
 * 
 */
public class DatabaseConnectionException extends Exception{
	/**
	 * Costruttore di classe senza parametri
	 */
	public DatabaseConnectionException(){}

	/**
	 * Costruttore di classe che ha come input un parametro di tipo stringa
	 * @param msg Stringa che rappresenta il messaggio di errore da poter visualizzare
	 */
	public DatabaseConnectionException(String msg){
		super(msg);
	}

}

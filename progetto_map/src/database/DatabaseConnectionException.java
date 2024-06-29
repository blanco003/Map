package database;

/**
 * 
 * Classe per gestire le eccezioni connesse al fallimento della connessione del database
 *
 */
public class DatabaseConnectionException extends Exception{
	/**
	 * Costruttore di classe senza parametri
	 */
	DatabaseConnectionException(){}
	/**
	 * Costruttore di classe che ha come input un parametro di tipo stringa
	 * @param e Stringa che rappresenta il messaggio di errore da poter visualizzare
	 */
	DatabaseConnectionException(String e){
		System.out.println(e);
	}

}

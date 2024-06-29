package database;
/**
 * Classe che estende Exception per modellare la restituzione di un resultset vuoto.
 */
public class EmptySetException extends Exception{
	
	/**
	 * Costruttore di classe
	 */
	public EmptySetException(){} 
	public EmptySetException(String msg) {
        super(msg);
    }
}

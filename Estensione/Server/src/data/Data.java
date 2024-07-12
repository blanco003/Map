package data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import database.DatabaseConnectionException;
import database.DbAccess;
import database.EmptySetException;
import database.TableData;
import database.TableSchema;

/**
 * La classe Data modella una lista di Example, rapprensentanti le transizioni del dataset, ottenuti a partire da una tabella del database.
 * Fornisce metodi per manipolare la lista di Example e calcolare distanze tra 2 liste di Example.
 */
public class Data{

	/** ArrayList di Example*/
	private List<Example> data=new ArrayList<>();  

	/** Numero di Example contentuti nel Dataset */
	private int numberOfExamples;  

	// Example data []; // rappresenta il dataset, rimpiazzato con contenitore ArrayList

	/**
	 * Costruttore che inizializza un nuovo oggetto Data ricavando le transizioni di esempi da una tabella del database.
	 * @param tableName il nome della tabella da cui leggere gli esempi.
	 * @throws NoDataException se la tabella non viene trovata o è vuota.
	 * @throws DatabaseConnectionException se si verifica un problema di connessione al database.
	 */
	public Data(String tableName) throws NoDataException, DatabaseConnectionException{

		// connessione al DB
		DbAccess dbacc = new DbAccess();
		 
		// creazione dello schema della tabella
		TableSchema schema = null;
		try{
			schema = new TableSchema(dbacc,tableName);  // potrebbe generare DataBaseConnectionException se fallisce l'accesso al db.
		}catch(SQLException e) {
            System.err.println(e.getMessage());
        }
		
		TableData dati = new TableData(dbacc);
		
		try {
			this.data=dati.getDistinctTransazioni(tableName);     // potrebbe generare DataBaseConnectionException se fallisce l'accesso al db.
		} catch(SQLException e){
			System.err.println(e.getMessage());
		} catch (EmptySetException e) {
			throw new NoDataException(e.getMessage());   // potrebbe non esistere o non contenere esempi    
		}
		//
		//catch(MissingNumberException e){
			// non si verificherà mai poichè viene controllato se l'attributo non è numerico e viene inserito un valore o di default o la media dei valori già inseriti
		//	System.out.println("\n! ! Errore : la tabella contiene attributi non numerici, riprovare");
		//}

		this.numberOfExamples=data.size();    // inizializziamo il numero di esempi con il numero di esempi trovati

		try {
			dbacc.closeConnection();     // chiudiamo la connessione al db
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}
	
	
	//rimosso dopo il database 
		/* 
		public Data(){

			//data
			data = new ArrayList<>();

			Example e=new Example();

			e.add(1.0);
			e.add(2.0);
			e.add(0.0);
			data.add(e);
			
			e=new Example();
			e.add(0.0);
			e.add(1.0);
			e.add(-1.0);
			data.add(e);
			
			e=new Example();
			e.add(1.0);
			e.add(3.0);
			e.add(5.0);
			data.add(e);
			
			e=new Example();
			e.add(1.0);
			e.add(3.0);
			e.add(4.0);
			data.add(e);
			
			e=new Example();
			e.add(2.0);
			e.add(2.0);
			e.add(0.0);
			data.add(e);
			
			numberOfExamples=5;
		}
		*/

	/**
	 * Restituisce il numero di esempi contenuti nel dataset.
	 *
	 * @return il numero di esempi.
	 */
	public int getNumberOfExamples(){
		return this.numberOfExamples;
	}

	/**
	 * Restituisce l'esempio memorizzato nell'indice specificato.
	 *
	 * @param exampleIndex l'indice dell'esempio da restituire.
	 * @return l'esempio all'indice specificato.
	 */
	public Example getExample(int exampleIndex) {
		return this.data.get(exampleIndex);
	}

	
	/**
	 * Calcola la matrice delle distanze tra tutti gli esempi.
	 *
	 * @return matrice delle distanze.
	 * @throws InvalidSizeException se si prova a calcolare la distanza tra due esempi di diversa dimensione.
	 */
	public double[][] distance() throws InvalidSizeException{
		int n = getNumberOfExamples();
		double[][] distanceMatrix = new double[n][n];

		for (int i = 0; i < n; i++) {
			for (int j = i; j < n; j++) {
				distanceMatrix[i][j] = data.get(i).distance(data.get(j));
				if (i == j) {
					distanceMatrix[i][j] = 0.0;

				}
			}
		}

		return distanceMatrix;
	}

	/**
	 * Restituisce una rappresentazione in formato stringa della lista di esempi.
	 *
	 * @return una stringa che rappresenta la lista di esempi.
	 */
	public String toString() {
		String sb="";
		
		Iterator<Example> it = data.iterator();
		int i = 0;
		while(it.hasNext()){
			sb += i + ":" + it.next().toString() + "\n";
			i++;
		}
		return sb;

	
	}

	
}

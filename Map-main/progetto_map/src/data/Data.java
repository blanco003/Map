package data;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import database.DatabaseConnectionException;
import database.DbAccess;
import database.EmptySetException;
import database.MissingNumberException;
import database.TableData;
import database.TableSchema;
/**
 * La classe Data rappresenta un insieme di esempi caricati da una tabella di un database.
 * Fornisce metodi per ottenere informazioni sugli esempi e calcolare le distanze tra essi.
 * Implementa l'interfaccia Serializable per permettere la serializzazione degli oggetti della classe.
 */
public class Data implements Serializable{

	private List<Example> data=new ArrayList<>();
	int numberOfExamples;

	// Example data []; // che rappresenta il dataset


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
	 * Costruttore che inizializza un nuovo oggetto Data leggendo gli esempi da una tabella del database.
	 *
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
			schema =new TableSchema(dbacc,tableName);  // potrebbe generare DataBaseConnectionException e propogarla
		}catch(SQLException e) {
            throw new NoDataException("\n! ! Errore : impossibile trovare la tabella \"" + tableName+"\" ");
        }
		
		TableData dati = new TableData(dbacc);
		
		try {
			// lettura risultato della query di selezione sulla tabella ed inizilizzazione ArrayList di Example
			this.data=dati.getDistinctTransazioni(tableName);     // potrebbe generare DataBaseConnectionException e propogarla 
		} catch (EmptySetException e) {
			throw new NoDataException("\n! ! Errore : la tabella \""+tableName+"\" è vuota");       
		}catch(SQLException e){
			throw new NoDataException("\n! ! Errore : impossibile trovare la tabella \"" + tableName+"\" ");
		}
		/*
		catch(MissingNumberException e){
			// non si verificherà mai poichè viene controllato se l'attributo non è numerico e viene inserito un valore o di default o la media dei valori già inseriti
			System.out.println("\n! ! Errore : la tabella contiene attributi non numerici, riprovare");
		}
		*/

		this.numberOfExamples=data.size();    // inizializza il numero di esempi con il numero di esempi trovati

		try {
			dbacc.closeConnection();     // chiudiamo la connessione al db
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * Restituisce il numero di esempi contenuti nella tabella.
	 *
	 * @return il numero di esempi.
	 */
	public int getNumberOfExamples(){
		return data.size();
	}
	/**
	 * Restituisce l'esempio all'indice specificato.
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
	 * @return una matrice delle distanze.
	 */
	public double[][] distance() {
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
	 * Restituisce una rappresentazione in formato stringa dell'insieme di esempi.
	 *
	 * @return una stringa che rappresenta l'insieme di esempi.
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

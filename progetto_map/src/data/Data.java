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
	

	public Data(String tableName) throws NoDataException, DatabaseConnectionException{

		// connessione al DB
		DbAccess dbacc = new DbAccess();
		 
		// creazione dello schema della tabella
		TableSchema schema = null;
		try{
			schema =new TableSchema(dbacc,tableName);  // potrebbe generare DataBaseConnectionException e propogarla
		}catch(SQLException e) {
            throw new NoDataException(e.getMessage() + ": impossibile trovare la tabella " + tableName);
        }
		
		TableData dati = new TableData(dbacc);
		
		try {
			// lettura risultato della query di selezione sulla tabella ed inizilizzazione ArrayList di Example
			this.data=dati.getDistinctTransazioni(tableName);     // potrebbe generare DataBaseConnectionException e propogarla 
		} catch (EmptySetException e) {
			System.err.println(e.getMessage()+" Tabella vuota");        
		}catch(SQLException e){
			throw new NoDataException(e.getMessage() + ": impossibile trovare la tabella " + tableName);
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

	public int getNumberOfExamples(){
		return data.size();
	}

	public Example getExample(int exampleIndex) {
		return this.data.get(exampleIndex);
	}


	

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

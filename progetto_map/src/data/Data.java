package data;

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

public class Data {

	private List<Example> data=new ArrayList<>();
	int numberOfExamples; 

	/* 
	Example data []; // che rappresenta il dataset
	int numberOfExamples; 
	*/

	/* rimosso dopo il database 
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

	Data(String tableName) throws NoDataException, DatabaseConnectionException, MissingNumberException{ // forse DAtabaseConnection, da sistemare MissingNumber

		DbAccess dbacc = new DbAccess();
		 
		TableSchema schema=null;
		try{
			schema=new TableSchema(dbacc,tableName);
		}catch(SQLException e) {
            throw new NoDataException(e.getMessage() + ": impossibile trovare la tabella " + tableName);
        }
		
		TableData dati = new TableData(dbacc);
		
		try {

			this.data=dati.getDistinctTransazioni(tableName);

		} catch (SQLException | EmptySetException e) {
			System.out.println(e.getMessage()+" Tabella vuota");
		}

		this.numberOfExamples=data.size();
		try {
			dbacc.closeConnection();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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

		/* 
		String sb="";
		for (int i = 0; i < data.size(); i++) {
			sb+=(i)+(":")+(data.get(i))+("\n");
		}
		return sb;
		*/
	}

	
}

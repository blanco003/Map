package database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



/**
 * La classe TableSchema rappresenta lo schema di una tabella nel database.
 * Fornisce metodi per ottenere informazioni sulle colonne della tabella, come il nome e il tipo di dati.
 */
public class TableSchema {
	
	/** Oggetto DbAccess per interagire con il Database */
	private DbAccess db;

	/**
	 * La classe Column rappresenta una singola colonna nella tabella, contenente il nome e il tipo di dato.
	 */
	public class Column{
		private String name;  /* Nome della colonna nella tabella*/
		private String type;  /** Tipo della colonna nella tabella*/

		/**
		 * Costruisce una colonna con il nome e il tipo specificati.
		 *
		 * @param name il nome della colonna
		 * @param type il tipo di dato della colonna (string o number)
		 */
		Column(String name,String type){
			this.name=name;
			this.type=type;
		}

		/**
		 * Restituisce il nome della colonna.
		 *
		 * @return il nome della colonna
		 */
		String getColumnName(){
			return name;
		}


		/**
		 * Verifica se il tipo di dato della colonna è numerico.
		 *
		 * @return true se il tipo di dato è "number", false altrimenti
		 */
		boolean isNumber(){
			return type.equals("number");
		}
		
		/**
		 * Restituisce il nome della colonne come Stringa.
		 * @return il nome della colonna
		 */
		public String toString(){
			return name+":"+type;
		}
	}

	/** ArrayList di Colonne, rappresentanti lo schema della tabella */
	private List<Column> tableSchema=new ArrayList<Column>();      
	

	/**
	 * Costruisce lo schema della tabella specificata nel database.
	 *
	 * @param db il DbAccess per la connessione al database
	 * @param tableName il nome della tabella di cui ottenere lo schema
	 * @throws SQLException se si verifica un errore durante l'accesso ai metadati del database
	 * @throws DatabaseConnectionException se si verifica un errore nella connessione al database
	 */
	public TableSchema(DbAccess db, String tableName) throws SQLException, DatabaseConnectionException{
		this.db=db;
		HashMap<String,String> mapSQL_JAVATypes=new HashMap<String, String>();
		//http://java.sun.com/j2se/1.3/docs/guide/jdbc/getstart/mapping.html
		mapSQL_JAVATypes.put("CHAR","string");
		mapSQL_JAVATypes.put("VARCHAR","string");
		mapSQL_JAVATypes.put("LONGVARCHAR","string");
		mapSQL_JAVATypes.put("BIT","string");
		mapSQL_JAVATypes.put("SHORT","number");
		mapSQL_JAVATypes.put("INT","number");
		mapSQL_JAVATypes.put("LONG","number");
		mapSQL_JAVATypes.put("FLOAT","number");
		mapSQL_JAVATypes.put("DOUBLE","number");
		
		
	
		 Connection con=db.getConnection();
		 DatabaseMetaData meta = con.getMetaData();
	     ResultSet res = meta.getColumns(null, null, tableName, null);
		   
	     while (res.next()) {
	         
	         if(mapSQL_JAVATypes.containsKey(res.getString("TYPE_NAME")))
	        		 tableSchema.add(new Column(
	        				 res.getString("COLUMN_NAME"),
	        				 mapSQL_JAVATypes.get(res.getString("TYPE_NAME")))
	        				 );
	      }
	      res.close();
	}
	  
	/**
	 * Restituisce il numero di attributi (colonne) presenti nello schema della tabella.
	 *
	 * @return il numero di attributi nella tabella
	*/
	int getNumberOfAttributes(){
		return tableSchema.size();
	}
		
	/**
	 * Restituisce la colonna corrispondente all'indice specificato.
	 *
	 * @param index l'indice della colonna da restituire
	 * @return l'oggetto Column corrispondente all'indice specificato
	 */
	Column getColumn(int index){
		return tableSchema.get(index);
	}

		
	}

		     



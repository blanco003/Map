package database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


import data.Example;
import database.TableSchema.Column;

/**
 * La classe TableData gestisce l'accesso ai dati di una tabella nel database.
 * Fornisce metodi per recuperare, aggiungere ed eliminare tabelle.
 */
public class TableData {

	/** Oggetto per gestire l'accesso al database. */
    private DbAccess db;

    /**
	 * Inizializza l'attributo db con l'oggetto DbAccess fornito.
	 *
	 * @param db l'oggetto DbAccess per la connessione al database
	 */
    public TableData(DbAccess db){
        this.db = db;
    }


	/**
	 * Recupera le transazioni distinte dalla tabella specificata.
	 * Esegue una query sulla tabella per ottenere tutte le colonne definite nello schema
	 * della tabella e restituisce una lista di esempi distinti.
	 *
	 * @param table il nome della tabella da cui recuperare le transazioni
	 * @return una lista di esempi distinti dalla tabella
	 * @throws SQLException se si verifica un errore nell'esecuzione della query SQL
	 * @throws EmptySetException se il risultato della query è vuoto
	 * @throws DatabaseConnectionException se si verifica un errore nella connessione al database
	 */
	public List<Example> getDistinctTransazioni(String table) throws EmptySetException, DatabaseConnectionException, SQLException{ 

        LinkedList<Example> lista_esempi = new LinkedList<Example>();

		Statement statement;
	
		TableSchema tSchema = new TableSchema(db, table);     // potrebbe generare DatabaseConnectionException se fallisce la connessione al db
		// restitusce lo schema della tabella con nome in input, ovvero una List di Column, dove ogni Column è costituita da un nome e un tipo
		
		String query = "SELECT ";  // creazione della stringa che rappresenta la query da effettuare sul db

		for (int i = 0; i < tSchema.getNumberOfAttributes(); i++) {
			Column c = tSchema.getColumn(i);
			if (i > 0)
				query += ",";
			query += c.getColumnName();  // aggiunge alla quey tutte le colonne contentute nello schema
		}

		// ora query sarà del tipo "SELECT coloumn1, column2, ..."	

		if (tSchema.getNumberOfAttributes() == 0){     
			// se non ha attributi allora solleviamo un eccezione EmptySetException, poichè la tabella non esiste in quanto
			// in sql non possono esistere tabelle con meno di 1 colonna
			throw new EmptySetException("Errore, impossibile trovare la tabella, riprovare");

		}else{
			query += " FROM " + table;      // ora query sarà del tipo "SELECT coloumn1, column2, ... FROM <nome tabella in input>"

			statement = this.db.getConnection().createStatement();  // getConnection() potrebbe sollevare un eccezione DatabaseConnectionException

			ResultSet rs = statement.executeQuery(query);      // conterrà il risultato della query eseguita sul db

			boolean empty = true;  // serve per verifiare se il risultato della query di selezione è vuoto, ovvero se non contiene esempi

			while (rs.next()) {        // iteriamo ogni riga del risultato

				empty = false;  // se c'è almeno una riga non è vuoto

				Example esempio_corrente = new Example();  // creiamo un singolo esempio per ogni riga

				for (int i = 0; i < tSchema.getNumberOfAttributes(); i++)

					if (tSchema.getColumn(i).isNumber()){         // se è in un formato numerico aggiungiamo l'elemento all'esemio convertendolo in Double
						esempio_corrente.add(rs.getDouble(i + 1)); 
					}else{          
                        // la stampa a video e il sollevamente dell'eccezione non è la scelta migliore da fare
						// throw new MissingNumberException("attributo non numerico : " + rs.getString(i+1));

						// potremmo pensare di inserire 0 di default 
						//esempio_corrente.add(0.0);

						// oppure di inserire un elemento pari alla media di quelli già inseriti per minimizzare l'errore
						Iterator<Double> it = esempio_corrente.iterator();
						int count = 0;
						int sum = 0;
						while (it.hasNext()) {
							count++;
							sum += it.next();
						}
						double media = (double) (sum / count);
						esempio_corrente.add(media);
						
					}
				lista_esempi.add(esempio_corrente);  // aggiungiamo l'esempio corrente della riga analizzata alla lista degli esempi
			}

			// chiudiamo gli oggetti usati per la connessione
			rs.close();
			statement.close();

			if (empty){   // se non abbiamo trovato nessun Example, ovvero il risultato era vuoto, solliamo l'eccezione EmptySetException
				throw new EmptySetException("Errore : la tabella è vuota, riprovare");
			}
				
			return lista_esempi;  
			     
		}
	}
	/**
	 * Recupera dal database i nomi di tutte le tabelle esistenti.
	 * @return ArrayList contenente tutti i nomi delle tabelle
	 * @throws DatabaseConnectionException se si verificano errori durante la connessione al database
	 */
	public ArrayList<String> getAllTablesName() throws DatabaseConnectionException{

		ArrayList<String> nomi_tabelle = new ArrayList<>();
		
			try{
				Statement s = this.db.getConnection().createStatement(); // getConnection() potrebbe sollevare un eccezione DatabaseConnectionException

				ResultSet r = s.executeQuery("SHOW TABLES FROM MapDb;");   // restituisce una tabella con una colonna "Tables_in_mapdb" contenente tutti i nomi delle tabelle presenti
	
				while(r.next()) {  // iteriamo ogni riga del risultato della query
				
					nomi_tabelle.add(r.getString("Tables_in_mapdb"));
	
				}
				r.close();
				s.close(); 
	
			} catch (SQLException ex) {
							
						System.out.println("SQLException: " + ex.getMessage());
						System.out.println("SQLState: " + ex.getSQLState());
						System.out.println("VendorError: " + ex.getErrorCode());
			}
			 
		return nomi_tabelle;
		
	}

	/**
	 * Crea una nuova tabella nel database.
	 * @param table Nome della tabella da creare sul database
	 * @param numero_esempi_per_transizione Numero di attributi (colonne) della nuova tabella da creare.
	 * @throws DatabaseConnectionException se si verificano errori durante la connessione al database
	 */
	public void createNewTable(String table,int numero_esempi_per_transizione) throws DatabaseConnectionException{

        try {
            Statement s = this.db.getConnection().createStatement();  // getConnection potrebbe sollevare un eccezione DatabaseConnectionException

            String query = "CREATE TABLE "+table+" ( ";  // query che andrà a creare la nuova tabella con numero di esempi scelti dall'utente sul database
            

			for (int i = 1; i <= numero_esempi_per_transizione; i++) {
				String tempVar = "X" + i + " DOUBLE";
				query += tempVar;
				if (i < numero_esempi_per_transizione) {
					query += ", ";
				} else {
					query += ");";
				}
			}

            s.executeUpdate(query);
			System.out.println("query di creazione tabella creata : "+query);

            System.out.println("La nuova tabella ("+table+") è stata creata correttamente sul db");

            s.close(); 

        } catch (SQLException ex) {
            
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }


	/**
	 * Inserisce l'arrayList di valori reali in input come tupla all'interno della tabella di nome specificato sul database.
	 * @param table Nome della tabella in cui inserire la tupla
	 * @param valori ArrayList di valori della tupla da inserire
	 * @throws DatabaseConnectionException se si verificano errori durante la connessione al database
	 */
	public void insertValues(String table, ArrayList<Double> valori) throws DatabaseConnectionException{
		
		try {
			Statement s = this.db.getConnection().createStatement(); // getConnection() potrebbe sollevare un eccezione DatabaseConnectionException
	
			String query = "INSERT INTO " + table + " (";
	
			for (int i = 1; i <= valori.size(); i++) {
				String temp_var = "X" + i;
				query += temp_var;
				if (i < valori.size()) {
					query += ", ";
				} else {
					query += ") VALUES (";
				}
			}
		
			for (int i = 0; i < valori.size(); i++) {
				query += valori.get(i);
				if (i < valori.size() - 1) {
					query += ", ";
				} else {
					query += ");";
				}
			}
	
			System.out.println("query di inserimento tupla creata : " + query);

			s.executeUpdate(query);
			System.out.println("La transizione è stata inserita correttamente nel database");
	
			s.close();
	
		} catch (SQLException e) {
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
		}
	}

	/**
	 * Elimna la tabella di nome specificato dal database.
	 * @param table Nome della tabella da eliminare dal database.
	 * @throws DatabaseConnectionException se si verifica un errore durante la connessione al database.
	 */
	public void deleteTable(String table) throws DatabaseConnectionException{  

		try{
			Statement s = this.db.getConnection().createStatement(); // getConnection() potrebbe sollevare un eccezione DatabaseConnectionException
	
			String query = "DROP TABLE " + table + ";";
	
			System.out.println("query di elimnazione tabella creata : " + query);

			s.executeUpdate(query);
			System.out.println("La tabella "+table+" è stata eliminata correttamente dal database");
	
			s.close();
	
		} catch (SQLException e) {
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
		}

	}

}

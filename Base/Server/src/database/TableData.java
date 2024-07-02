package database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import data.Example;
import database.TableSchema.Column;

public class TableData {
    private DbAccess db;

    //  inizializza l’attributo db
    public TableData(DbAccess db){
        this.db = db;
    }

    public List<Example> getDistinctTransazioni(String table) throws SQLException, EmptySetException,DatabaseConnectionException{ 

        LinkedList<Example> lista_esempi = new LinkedList<Example>();

		Statement statement;

		
		TableSchema tSchema = new TableSchema(db, table);     // potrebbe generare DatabaseConnectionException
		           // restitusce lo schema della tabella con nome in input, ovvero una List di Column, dove ogni Column è costituita da un nome e un tipo
		

		String query = "SELECT ";  // creazione della stringa che rappresenta la query da effettuare sul db

		for (int i = 0; i < tSchema.getNumberOfAttributes(); i++) {
			Column c = tSchema.getColumn(i);
			if (i > 0)
				query += ",";
			query += c.getColumnName();  // aggiunge alla quey tutte le colonne contentute nello schema
		}

		// ora query sarà del tipo "SELECT coloumn1, column2, ..."	

		if (tSchema.getNumberOfAttributes() == 0)     // se non ha attributi allora solleviamo un eccezione
			throw new SQLException();
		else{
			query += " FROM " + table;      // ora query sarà del tipo "SELECT coloumn1, column2, ... FROM <nome tabella in input>"

			statement = this.db.getConnection().createStatement();  // potrebbe generare DatabaseConnectionException

			ResultSet rs = statement.executeQuery(query);      // conterrà il risultato della query eseguita sul db

			boolean empty = true;  // serve per verifiare se il risultato della query di selezione è vuoto

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

						// oppure di inserire un elemento pari alla media di quelli già inseriti
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

			if (empty)   // se non abbiamo trovato nessun Example, ovvero il risultato era vuoto, solliamo l'eccezione EmptySetException
				throw new EmptySetException();
			else
				return lista_esempi;       
		}
    }
}

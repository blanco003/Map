package database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import data.Data;
import data.Example;
import database.TableSchema.Column;

public class TableData {
    private DbAccess db;

    //  inizializza l’attributo db
    public TableData(DbAccess db){
        this.db = db;
    }

    public List<Example> getDistinctTransazioni(String table) throws SQLException, EmptySetException,MissingNumberException, DatabaseConnectionException{ // forse DataBaseConnection

        LinkedList<Example> transSet = new LinkedList<Example>();
		Statement statement;
		TableSchema tSchema = new TableSchema(db, table);

		String query = "select ";

		for (int i = 0; i < tSchema.getNumberOfAttributes(); i++) {
			Column c = tSchema.getColumn(i);
			if (i > 0)
				query += ",";
			query += c.getColumnName();
		}
		if (tSchema.getNumberOfAttributes() == 0)
			throw new SQLException();
		else{
			query += " FROM " + table;
			statement = this.db.getConnection().createStatement();
			ResultSet rs = statement.executeQuery(query);
			boolean empty = true;
			while (rs.next()) {
				empty = false;
				Example currentTuple = new Example();
				for (int i = 0; i < tSchema.getNumberOfAttributes(); i++)
					if (tSchema.getColumn(i).isNumber())
						currentTuple.add(rs.getDouble(i + 1));
					else
                        // stampa a video non accettabile, meglio inserire l'elemento medio
						throw new MissingNumberException("attributo non numerico : " + rs.getString(i+1));
				transSet.add(currentTuple);
			}
			rs.close();
			statement.close();
			if (empty)
				throw new EmptySetException();
			else
				return transSet;
		}
    }
}

package io.almostrealism.sql;


import io.almostrealism.query.SimpleQuery;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * @author  Michael Murray
 */
public class SQLSelect<V> extends SimpleQuery<ComboPooledDataSource, String, V> {

	/**
	 * Construct a new SQLSelect. Used by the static method {@link #prepare(String, Properties)}.
	 *
	 * @param query  The SQL query to execute.
	 * @param columns  The mapping between columns in the database and field names.
	 */
	private SQLSelect(String query, Properties columns) {
		super(query);
		for (String n : columns.stringPropertyNames()) put(n, columns.getProperty(n));
		for (int i = 0; i < tables.length; i++) {
			tableString = tableString + tables[i];
			if (i < (tables.length - 1)) tableString += ",";
		}
	}

	public Collection<V> execute(ComboPooledDataSource database, String key) {
		List<V> data = new ArrayList<V>();

		try (Connection c = p.borrowConnection(); Statement s = c.createStatement()) {
			ResultSet rs = s.executeQuery();

			while (rs.next()) {

			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		return shift;
	}

	public static SQLSelect prepare(String query, Properties columns) {
		return new SQLSelect(query, columns);
	}
}

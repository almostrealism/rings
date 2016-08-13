package io.almostrealism.sql;

import io.almostrealism.query.SimpleQuery;

import java.util.Properties;

/**
 * @author  Michael Murray
 */
public class SQLSelect<V> extends SimpleQuery<ComboPooledDataSource, String, V> {
	private SQLSelect(String query, Properties columns) {
	}

	public static SQLSelect prepare(String query, Properties columns) {
		return new SQLSelect(query, )
	}
}

package io.almostrealism.etl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import io.almostrealism.sql.SQLConnectionProvider;

public abstract class View<V> {
	private SQLConnectionProvider sql;
	private String table;
	private Collection<V> values;
	
	public View(SQLConnectionProvider c, String table, Collection<V> values) {
		this.sql = c;
		this.table = table;
		this.values = values;
	}
	
	public abstract Map<String, String> encode(V value);
	
	public void process() throws SQLException {
		try (Connection c = sql.getSQLConnection()) {
			for (V v : values) {
				Map<String, String> data = encode(v);
				
				c.createStatement().executeQuery(getQuery(data));
			}
		}
	}
	
	protected String getQuery(Map<String, String> data) {
		String names[] = new String[data.size()];
		String values[] = new String[data.size()];
		
		StringBuffer buf = new StringBuffer();
		buf.append("insert into ");
		buf.append(table);
		buf.append(getValueList(names));
		buf.append(" values ");
		buf.append(getValueList(values));
		return buf.toString();
	}
	
	private String getValueList(String values[]) {
		StringBuffer buf = new StringBuffer();
		
		buf.append("(");
		
		for (int i = 0; i < values.length; i++) {
			buf.append(values[i]);
			if (i < (values.length - 1)) buf.append(",");
		}
		
		buf.append(")");
		
		return buf.toString();
	}
}

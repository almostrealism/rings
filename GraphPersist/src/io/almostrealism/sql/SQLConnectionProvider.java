package io.almostrealism.sql;

import java.sql.Connection;
import java.sql.SQLException;

public interface SQLConnectionProvider {
	public Connection getSQLConnection() throws SQLException;
}

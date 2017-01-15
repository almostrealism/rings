package io.almostrealism.persist;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class CascadingQuery<D, K, V extends Cacheable> extends CacheableQuery<D, K, V> {
	public abstract void process(ResultSet s, K arguments) throws SQLException;
}

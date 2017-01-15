package io.almostrealism.query;

import java.sql.ResultSet;

public interface CascadingQuery<D, K, V> extends Query<D, K, V> {
	void process(ResultSet s, K arguments);
}

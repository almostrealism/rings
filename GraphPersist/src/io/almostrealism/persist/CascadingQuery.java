package io.almostrealism.persist;

import java.sql.ResultSet;

public abstract class CascadingQuery<D, K, V extends Cacheable> extends CacheableQuery<D, K, V> {
	abstract void process(ResultSet s, K arguments);
}

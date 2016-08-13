package io.almostrealism.query;

import java.util.Collection;
import java.util.Hashtable;

/**
 * {@link SimpleQuery} maps named columns to the POJO fields that should be populated.
 *
 * @author  Michael Murray
 */
public abstract class SimpleQuery<D, K, V> implements Query<D, K, V> {
	private Hashtable<String, String> map = new Hashtable<>();

	protected String query;

	public SimpleQuery(String q) { query = q; }

	public void put(String column, String fieldName) { map.put(column, fieldName); }

	public String get(String name) { return map.get(name); }
}
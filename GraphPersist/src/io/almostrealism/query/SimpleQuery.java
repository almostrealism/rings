package io.almostrealism.query;

import java.util.Hashtable;

import org.almostrealism.util.Factory;

/**
 * {@link SimpleQuery} maps named columns to the POJO fields that should be populated.
 *
 * @author  Michael Murray
 */
public abstract class SimpleQuery<D, K, V> implements Query<D, K, V> {
	private Hashtable<String, String> map = new Hashtable<>();

	protected String query;
	
	protected Factory<V> factory;
	
	public SimpleQuery(String q, Factory<V> f) { query = q; factory = f; }

	public void put(String column, String fieldName) { map.put(column, fieldName); }

	public String get(String name) { return map.get(name); }
}
package org.almostrealism.birst.util;

/**
 * {@link KeyValueStore} is responsible for maintaining a table of keys and values
 * which are all of which are {@link String}s. The {@link String}s are stored in
 * alphabetical order by key for easy reading.
 * 
 * @author  Michael Murray
 */
public class KeyValueStore {
	private ValuedKey root;
	
	public KeyValueStore() {
		
	}
	
	public void put(String key, String value) {
		if (root == null) root = new ValuedKey(key, value);
		root.add(key, value);
	}
	
	public void update(String key, String value) {
		ValuedKey v = root.remove(key);
		put(v.getKey(), v.getValue());
	}
	
	public String get(String key) {
		return root.get(key).getValue();
	}
	
	public void remove(String key) {
		root.remove(key);
	}
}

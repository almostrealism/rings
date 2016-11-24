/*
 * Copyright 2016 Michael Murray

 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.almostrealism.query;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;

/**
 * The {@link QueryLibrary} tracks {@link Query}s and {@link io.almostrealism.enrich.Enrichment}s
 * for various object types, making it trivial to retrieve data without the need for creating
 * separate DAO types for your POJOs.
 *
 * @author  Michael Murray
 */
public class QueryLibrary<D> {
	private static QueryLibrary root = new QueryLibrary();
	
	private HashMap<KeyValueTypes, Query<? extends D, ?, ?>>  queries;
	
	protected QueryLibrary() { queries = new HashMap<>(); }

	public synchronized <V, K> void addQuery(Class<V> type, Class<K> argumentType, Query<? extends D, ? extends K, V> q) {
		queries.put(new KeyValueTypes(argumentType, type), q);
	}
	
	public <V> Collection<V> get(D database, Class type) throws IllegalAccessException, InvocationTargetException {
		return get(database, type, null, null);
	}
	
	public <V, K> Collection<V> get(D database, Class type, Class<K> argumentType, K arguments) throws IllegalAccessException, InvocationTargetException {
		Query q = null;
		
		synchronized (this) { q = queries.get(new KeyValueTypes(argumentType, type)); }
		
		return q.execute(database, arguments);
	}
	
	public static QueryLibrary root() { return root; }
	
	private static class KeyValueTypes {
		Class keyType;
		Class valueType;
		
		public KeyValueTypes(Class keyType, Class valueType) {
			this.keyType = keyType;
			this.valueType = valueType;
		}
		
		public boolean equals(Object o) {
			if (o instanceof KeyValueTypes == false) return false;
			if (!((KeyValueTypes) o).keyType.equals(keyType)) return false;
			if (!((KeyValueTypes) o).valueType.equals(valueType)) return false;
			return true;
		}
		
		public int hashCode() { return valueType.hashCode(); }
	}
}

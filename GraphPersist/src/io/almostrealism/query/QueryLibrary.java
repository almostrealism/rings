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

import java.util.Collection;
import java.util.HashMap;

/**
 * The {@link QueryLibrary} tracks {@link Query}s and {@link io.almostrealism.enrich.Enrichment}s
 * for various object types, making it trivial to retrieve data without the need for creating
 * separate DAO types for your POJOs.
 *
 * @author  Michael Murray
 */
public class QueryLibrary<D, K> {
	private static QueryLibrary root = new QueryLibrary();

	private HashMap<Class, Query<? extends D, ? extends K, ?>>  queries;

	protected QueryLibrary() { queries = new HashMap<>(); }

	public synchronized <V> void addQuery(Class<V> type, Query<? extends D, ? extends K, V> q) {
		queries.put(type, q);
	}
	
	public synchronized <V> void addEnrichment() { }
	
	public <V> Collection<V> get(D database, Class type) {
		return get(database, type, null);
	}
	
	public <V> Collection<V> get(D database, Class type, K arguments) {
		Query q = null;
		
		synchronized (this) { q = queries.get(type); }
		
		return q.execute(database, arguments);
	}
	
	public static QueryLibrary root() { return root; }
}

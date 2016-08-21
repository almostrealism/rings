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

package io.almostrealism.test;

import java.beans.PropertyVetoException;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * @author  Michael Murray
 */
public class DatabaseTest {
	public static ComboPooledDataSource getDB() throws PropertyVetoException {
		// Set up the database
		ComboPooledDataSource pool = new ComboPooledDataSource();
		pool.setDriverClass("com.mysql.jdbc.Driver"); 
		pool.setJdbcUrl("jdbc:mysql://127.0.0.1/test");
		pool.setUser("root");
		pool.setPassword("root");
		return pool;
	}
}

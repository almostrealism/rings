package io.almostrealism.persist.test;

import static org.junit.Assert.*;

import java.io.InputStream;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import io.almostrealism.query.QueryLibrary;
import io.almostrealism.sql.SQLSelect;

/**
 */
public class QueryLibraryTest {
	@org.junit.Test
	public void main() throws Exception {
		// Set up the database
		ComboPooledDataSource pool = new ComboPooledDataSource();
		pool.setDriverClass("com.mysql.jdbc.Driver"); 
		pool.setJdbcUrl("jdbc:mysql://127.0.0.1/test");
		pool.setUser("root");
		pool.setPassword("root");
		
		// Add a query that maps the columns from the database
		InputStream fieldMap = QueryLibraryTest.class.getResourceAsStream("TestEntity.properties");
		QueryLibrary.root().addQuery(TestEntity.class, SQLSelect.prepare(fieldMap,
									() -> { return new TestEntity(); }));
		
		QueryLibrary.root().get(pool, TestEntity.class);
		
		assertTrue("Failed", false);
	}
}
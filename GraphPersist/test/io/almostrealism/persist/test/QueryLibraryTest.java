package io.almostrealism.persist.test;

import static org.junit.Assert.*;

import java.io.InputStream;

import io.almostrealism.query.QueryLibrary;
import io.almostrealism.sql.SQLSelect;

/**
 */
public class QueryLibraryTest {
	@org.junit.Test
	public void main() throws Exception {
		InputStream fieldMap = QueryLibraryTest.class.getResourceAsStream("TestEntity.properties");
		QueryLibrary.root().addQuery(TestEntity.class, SQLSelect.prepare(fieldMap,
									() -> { return new TestEntity(); }));
		
		TestEntity t = new TestEntity();
		
//	TODO	QueryLibrary.root().get();
		
		assertTrue("Failed", false);
	}
}
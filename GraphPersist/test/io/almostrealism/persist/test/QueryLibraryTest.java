package io.almostrealism.persist.test;

import static org.junit.Assert.*;

import java.io.InputStream;

import io.almostrealism.query.QueryLibrary;
import io.almostrealism.sql.SQLSelect;
import io.almostrealism.test.DatabaseTest;

/**
 */
public class QueryLibraryTest extends DatabaseTest {
	@org.junit.Test
	public void selectAll() throws Exception {
		InputStream fieldMap = QueryLibraryTest.class.getResourceAsStream("TestEntity.properties");
		QueryLibrary.root().addQuery(TestEntity.class, SQLSelect.prepare(
									"select * from testdata where id > 1",
									fieldMap, () -> { return new TestEntity(); }));
		
		int resultCount = QueryLibrary.root().get(getDB(), TestEntity.class).size();
		
		assertTrue("Result Count", resultCount == 2);
	}
	
	@org.junit.Test
	public void selectStyles() throws Exception {
		InputStream fieldMap = QueryLibraryTest.class.getResourceAsStream("TestEntity.properties");
		QueryLibrary.root().addQuery(TestEntity.class, SQLSelect.prepare(
									"select * from testdata,testlinked where testdata.id > 1",
									fieldMap, () -> { return new TestEntity(); }));
		
		int resultCount = QueryLibrary.root().get(getDB(), TestEntity.class).size();
		
		assertTrue("Result Count was " + resultCount, resultCount == 2);
	}
}
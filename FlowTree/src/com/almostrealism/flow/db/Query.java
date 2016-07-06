/*
 * Copyright (C) 2005-06  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 *
 */

package com.almostrealism.flow.db;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.almostrealism.flow.Message;

/**
 * A Query object represents a database query. The default values for the parameters
 * are "output" for table name, DatabaseConnection.toaColumn for column one,
 * DatabaseConnection.dataColumn, and "true" for condition.
 * 
 * @author Mike Murray
 */
public class Query implements Externalizable {
	public static interface ResultHandler {
		public void handleResult(String key, String value);
		public void handleResult(String key, byte value[]);
	}
	
	public static final String sep = "%";
	private String table, col1, col2, val1, val2, con;
	private int relay;
	
	private ResultHandler handler;
	
	/**
	 * Constructs a new Query object using defaults for all parameters.
	 */
	public Query() {
		this.table = "output";
		this.col1 = DatabaseConnection.toaColumn;
		this.col2 = DatabaseConnection.dataColumn;
		this.con = "true";
	}
	
	/**
	 * Constructs a new Query object using the specified table and defaults
	 * for all other parameters.
	 * 
	 * @param table  Name of table to query.
	 */
	public Query(String table) {
		this.table = table;
		this.col1 = DatabaseConnection.toaColumn;
		this.col2 = DatabaseConnection.dataColumn;
		this.con = "true";
	}
	
	/**
	 * Constructs a new Query object using the specified table and condition
	 * and defaults for all other parameters.
	 * 
	 * @param table  Name of table to query.
	 * @param con  Condition for query.
	 */
	public Query(String table, String con) {
		this.table = table;
		this.col1 = DatabaseConnection.toaColumn;
		this.col2 = DatabaseConnection.dataColumn;
		this.con = con;
	}
	
	/**
	 * Constructs a new Query object using the specified paramaters.
	 * 
	 * @param table  Name of the table for this query.
	 * @param col1  Name of the key column for this query.
	 * @param col2  Name of the value column for this query.
	 */
	public Query(String table, String col1, String col2) {
		this.table = table;
		this.col1 = col1;
		this.col2 = col2;
		this.con = null;
	}
	
	/**
	 * Constructs a new Query object using the specified paramaters.
	 * 
	 * @param table  Name of the table for this query.
	 * @param col1  Name of the key column for this query.
	 * @param col2  Name of the value column for this query.
	 * @param con  SQL condition for this query.
	 */
	public Query(String table, String col1, String col2, String con) {
		this.table = table;
		this.col1 = col1;
		this.col2 = col2;
		this.con = con;
	}
	
	public void setResultHandler(ResultHandler h) { this.handler = h; }
	public ResultHandler getResultHandler() { return this.handler; }
	
	/**
	 * @return  The name of the table for this query.
	 */
	public String getTable() { return this.table; }
	
	/**
	 * @return  The name of the column for this query.
	 */
	public String getColumn(int i) {
		if (i == 0) {
			return this.col1;
		} else {
			return this.col2;
		}
	}
	
	public void setColumn(int i, String value) {
		if (i == 0)
			this.col1 = value;
		else
			this.col2 = value;
	}
	
	public void setValue(int i, String value) {
		if (i == 0)
			this.val1 = value;
		else
			this.val2 = value;
	}
	
	public String getValue(int i) {
		if (i == 0)
			return this.val1;
		else
			return this.val2;
	}
	
	/**
	 * @return  The SQL condition for this query.
	 */
	public String getCondition() { return this.con; }
	
	public byte[] getBytes() {
		byte t[] = this.table.getBytes();
		byte cl1[] = this.col1.getBytes();
		byte cl2[] = this.col2.getBytes();
		byte cn[] = this.con.getBytes();
		
		byte data[] = new byte[t.length + cl1.length + cl2.length + cn.length + 5];
		
		data[0] = (byte) t.length;
		data[1] = (byte) cl1.length;
		data[2] = (byte) cl2.length;
		data[3] = (byte) cn.length;
		data[4] = (byte) this.relay;
		
		int index = 5;
		
		System.arraycopy(t, 0, data, index, t.length);
		index = index + t.length;
		System.arraycopy(cl1, 0, data, index, cl1.length);
		index = index + cl1.length;
		System.arraycopy(cl2, 0, data, index, cl2.length);
		index = index + cl2.length;
		System.arraycopy(cn, 0, data, index, cn.length);
		
		if (Message.verbose)
			System.out.println("Write " + data.length + " bytes " + this.toString());
		
		return data;
	}
	
	public void setBytes(byte b[]) {
		this.relay = b[4];
		int index = 5;
		this.table = new String(b, index, b[0]);
		index = index + b[0];
		this.col1 = new String(b, index, b[1]);
		index = index + b[1];
		this.col2 = new String(b, index, b[2]);
		index = index + b[2];
		this.con = new String(b, index, b[3]);
		index = index + b[3];
		
		if (index < b.length)
			System.out.println("Query: " + (b.length - index) + " extra bytes (" +
								b[0] + ", " + b[1] + ", " + b[2] + ", " + b[3] + ").");
		
		if (Message.verbose)
			System.out.println("Read " + b.length + " bytes " + this.toString());
	}
	
	public void setRelay(int r) { this.relay = r; }
	public int getRelay() { return this.relay; }
	public int deincrementRelay() { return --this.relay; }
	
	
	/**
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		if (Message.verbose) System.out.println("Write " + this.toString());
		
		out.writeInt(this.relay);
		out.writeUTF(this.table);
		out.writeUTF(this.col1);
		out.writeUTF(this.col2);
		out.writeUTF(this.con);
	}

	/**
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		this.relay = in.readInt();
		this.table = in.readUTF();
		this.col1 = in.readUTF();
		this.col2 = in.readUTF();
		this.con = in.readUTF();
		
		if (Message.verbose) System.out.println("Read " + this.toString());
	}
	
	public String toString() {
		return "Query[" + this.table + ", " + this.col1 + ", " +
						this.col2 + ", " + this.con + ", " + this.relay + "]";
	}
	
	public static String toString(Hashtable h) {
		if (h.size() <= 0) return "";
		
		StringBuffer b = new StringBuffer();
		Iterator itr = h.entrySet().iterator();
		
		Map.Entry ent = (Map.Entry) itr.next();
		b.append(ent.getKey().toString());
		b.append(Query.sep);
		b.append(ent.getValue().toString());
		
		for (int i = 1; itr.hasNext(); i += 2) {
			ent = (Map.Entry) itr.next();
			b.append(Query.sep);
			b.append(ent.getKey().toString());
			b.append(Query.sep);
			b.append(ent.getValue().toString());
		}
		
		return b.toString();
	}
	
	public static void fromString(String data, Hashtable h) {
		int index = data.indexOf(Query.sep);
		String s = data;
		
		List l = new ArrayList();
		w: while (true) {
			if (index < 0) {
				l.add(s);
				break w;
			} else {
				l.add(s.substring(0, index));
			}
			
			s = s.substring(index + 1);
			index = s.indexOf(Query.sep);
		}
		
		Iterator itr = l.iterator();
		w: while (itr.hasNext()) {
			String key = (String) itr.next();
			if (!itr.hasNext()) break w;
			String value = (String) itr.next();
			h.put(key, value);
		}
	}
}

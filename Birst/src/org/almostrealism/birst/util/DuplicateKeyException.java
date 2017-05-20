package org.almostrealism.birst.util;

/**
 * A {@link DuplicateKeyException} is thrown when a key in a
 * {@link KeyValueStore} is already assigned when attempting
 * to make an assignment.
 * 
 * @author Michael Murray
 */
public class DuplicateKeyException extends RuntimeException {
	private String key;
	
	public DuplicateKeyException(String key) {
		this.key= key;
	}
	
	public String getKey() { return this.key; }
}

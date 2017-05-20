package org.almostrealism.birst.util;

import java.util.ArrayList;

/**
 * A {@link ValuedKey} is a {@link Node} with a corresponding
 * {@link String} value.
 * 
 * @author  Michael Murray
 */
public class ValuedKey extends Node<ValuedKey> {
	private String key, value;
	
	public ValuedKey(String key, String value) {
		this(null, key, value);
	}
	
	public ValuedKey(ValuedKey parent, String key, String value) {
		super(parent);
		this.key = key;
		this.value = value;
	}
	
	public String getKey() { return key; }
	public String getValue() { return value; }
	
	/**
	 * Traverse the tree to add a new {@link ValuedKey} entry.
	 */
	public ValuedKey add(String key, String value) {
		if (key.compareTo(this.key) > 0) {
			if (left == null) {
				left = new ValuedKey(this, key, value);
				return left;
			} else {
				return left.add(key, value);
			}
		} else if (key.compareTo(this.key) < 0) {
			if (right == null) {
				right = new ValuedKey(this, key, value);
				return right;
			} else {
				return right.add(key, value);
			}
		} else {
			throw new DuplicateKeyException();
		}
	}
	
	public ValuedKey get(String key) {
		if (key.compareTo(this.key) > 0) {
			if (left == null) {
				throw new UnknownKeyException();
			} else {
				return left.remove(key);
			}
		} else if (key.compareTo(this.key) < 0) {
			if (right == null) {
				throw new UnknownKeyException();
			} else {
				return right.remove(key);
			}
		} else {
			return this;
		}
	}
	
	/**
	 * Traverse the tree to remove a {@link ValuedKey} entry.
	 */
	public ValuedKey remove(String key) {
		if (key.compareTo(this.key) > 0) {
			if (left == null) {
				throw new UnknownKeyException();
			} else {
				return left.remove(key);
			}
		} else if (key.compareTo(this.key) < 0) {
			if (right == null) {
				throw new UnknownKeyException();
			} else {
				return right.remove(key);
			}
		} else {
			if (parent.left == this) {
				parent.left = null;
			} else if (parent.right == this) {
				parent.right = null;
			}
			
			ValuedKey root = getRoot();
			root.addAll(left);
			root.addAll(right);
			
			return this;
		}
	}
	
	public void addAll(ValuedKey k) {
		add(k.getKey(), k.getValue());
		if (k.left != null) addAll(k.left);
		if (k.right != null) addAll(k.right);
	}
	
	public ValuedKey getRoot() {
		ValuedKey v = this;
		
		while (v.parent != null) {
			v = v.parent;
		}
		
		return v;
	}
	
	public List<String> keys() {
		ArrayList<String> l = new ArrayList();
		if (left != null) l.addAll(left.keys());
		if (right != null) l.addAll(right.keys());
		return l;
	}
}

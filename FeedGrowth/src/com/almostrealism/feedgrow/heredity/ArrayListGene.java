package com.almostrealism.feedgrow.heredity;

import java.util.ArrayList;

public class ArrayListGene<T> extends ArrayList<Factor<T>> implements Gene<T> {
	public Factor<T> getFactor(int index) { return get(index); }
	public int length() { return size(); }
}

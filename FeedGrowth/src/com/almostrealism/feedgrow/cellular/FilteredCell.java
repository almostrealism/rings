package com.almostrealism.feedgrow.cellular;

import com.almostrealism.feedgrow.heredity.Factor;

public class FilteredCell<T> extends CellAdapter<T> {
	private Factor<T> filter;
	
	public FilteredCell(Factor<T> filter) { this.filter = filter; }
	
	protected void setFilter(Factor<T> filter) { this.filter = filter; }
	
	public void push(long index) {
		long filtered = addProtein(filter.getResultant(getProtein(index)));
		super.push(filtered);
	}
}

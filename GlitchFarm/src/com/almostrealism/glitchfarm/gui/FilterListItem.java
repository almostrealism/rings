package com.almostrealism.glitchfarm.gui;

import javax.swing.JPanel;

import com.almostrealism.glitchfarm.filter.AmplitudeRangeFilter;
import com.almostrealism.glitchfarm.filter.BlurFilter;
import com.almostrealism.glitchfarm.filter.LineFilter;
import com.almostrealism.glitchfarm.filter.SineFilter;
import com.almostrealism.glitchfarm.filter.gui.AmplitudeRangeFilterConfigPanel;
import com.almostrealism.glitchfarm.filter.gui.SineFilterConfigPanel;
import com.almostrealism.glitchfarm.line.FilterOutputLine;

public class FilterListItem {
	private FilterOutputLine line;
	private LineFilter filter;
	private JPanel filterConfigPanel;
	
	public FilterListItem(FilterOutputLine fl, LineFilter f) {
		this.line = fl;
		this.filter = f;
		
		if (this.filter instanceof BlurFilter)
			this.filterConfigPanel = new BlurFilterConfigPanel((BlurFilter) this.filter);
		else if (this.filter instanceof SineFilter)
			this.filterConfigPanel = new SineFilterConfigPanel((SineFilter) this.filter);
		else if (this.filter instanceof AmplitudeRangeFilter)
			this.filterConfigPanel = new AmplitudeRangeFilterConfigPanel((AmplitudeRangeFilter) this.filter);
		else
			this.filterConfigPanel = new JPanel();
	}
	
	public JPanel getConfigPanel() {
		return this.filterConfigPanel;
	}
	
	public FilterOutputLine getLine() { return this.line; }
	
	public LineFilter getFilter() { return this.filter; }
	
	public String toString() { return this.filter.toString(); }
}

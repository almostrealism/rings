package com.almostrealism.synth;

import org.almostrealism.time.Frequency;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OvertoneSeries implements Iterable<Frequency> {
	private int subCount, superCount;
	private Frequency root;

	public OvertoneSeries(int subCount, int superCount) {
		this.subCount = subCount;
		this.superCount = superCount;
	}

	public void setRoot(Frequency f) { this.root = f; }

	@Override
	public Iterator<Frequency> iterator() {
		List<Frequency> l = new ArrayList<>();

		for (int i = subCount; i > 0; i--) {
			l.add(new Frequency(root.asHertz() / Math.pow(2, i)));
		}

		l.add(new Frequency(root.asHertz()));

		for (int i = 1; i <= superCount; i++) {
			l.add(new Frequency(root.asHertz() * Math.pow(2, i)));
		}

		return l.iterator();
	}
}

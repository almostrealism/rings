package com.almostrealism.physics;

import com.almostrealism.raytracer.RayTracedAnimation;
import org.almostrealism.algebra.ZeroVector;
import org.almostrealism.color.ShadableSurface;
import org.almostrealism.physics.Absorber;
import org.almostrealism.physics.PhotonField;
import org.almostrealism.raytrace.AbsorberHashSet;
import org.almostrealism.raytrace.DefaultPhotonField;

import java.util.Collection;
import java.util.Iterator;

public class PhotonFieldAnimation<T extends ShadableSurface> extends RayTracedAnimation<T> {
	private final PhotonField field;
	private final AbsorberHashSet absorber;

	public PhotonFieldAnimation() {
		absorber = new AbsorberHashSet();

		// Create photon field and set absorber to the absorber set
		// containing the black body and the light bulb
		field = new DefaultPhotonField();
		field.setAbsorber(absorber);

		getClock().addPhotonField(field);
	}

	@Override
	public boolean add(T t) {
		if (super.add(t)) {
			if (t instanceof Absorber)
				absorber.addAbsorber((Absorber) t, ZeroVector.getInstance());

			return true;
		}

		return false;
	}

	@Override
	public boolean remove(Object o) {
		if (super.remove(o)) {
			if (o instanceof Absorber)
				absorber.removeAbsorber((Absorber) o);

			return true;
		}

		return false;
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		int startingSize = size();
		for (T t : c) add(t);
		return startingSize != size();
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		int startingSize = size();
		Iterator<? extends T> itr = c.iterator();
		for (int i = index; itr.hasNext(); i++) {
			add(i, itr.next());
		}
		return startingSize != size();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		int startingSize = size();
		for (Object t : c) remove(t);
		return startingSize != size();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		// TODO
		return false;
	}

	@Override
	public void clear() {
		super.clear();
		absorber.clear();
	}

	@Override
	public T set(int index, T element) {
		T t = get(index);
		remove(t);
		add(index, element);
		return t;
	}

	@Override
	public void add(int index, T element) {
		super.add(index, element);
		if (element instanceof Absorber)
			absorber.addAbsorber((Absorber) element, ZeroVector.getInstance());
	}

	@Override
	public T remove(int index) {
		T t = super.remove(index);
		absorber.remove(t);
		return t;
	}
}

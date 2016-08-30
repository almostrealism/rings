package com.almostrealism.promo.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.almostrealism.promo.entities.Material;
import com.almostrealism.promo.entities.PromotionAtom;
import com.almostrealism.promo.entities.URLPromotionAtom;
import com.almostrealism.promo.entities.Material.MaterialURLType;

/**
 * {@link PromotionAtomFactory} is used to create {@link PromotionAtom}s for {@link Material}.
 * Atoms are cached when generated, so the same set of atoms will be returned after multiple
 * calls to the {@link #getAtoms(Material)} method.
 * 
 * TODO  This should eventually be an abstract class.
 * 
 * @author  Michael Murray
 */
public class PromotionAtomFactory {
	private Hashtable<Material, Collection<PromotionAtom>> cachedAtoms;
	
	/**
	 * Constructs a new {@link PromotionAtomFactory}.
	 */
	public PromotionAtomFactory() {
		this.cachedAtoms = new Hashtable<Material, Collection<PromotionAtom>>();
	}
	
	/**
	 * Returns some {@link URLPromotionAtom}s for the specified {@link Material}.
	 * This may returned cached {@link PromotionAtom}s.
	 */
	public Collection<PromotionAtom> getAtoms(Material m) {
		if (this.cachedAtoms.contains(m)) {
			return this.cachedAtoms.get(m);
		} else {
			List<PromotionAtom> atoms = new ArrayList<PromotionAtom>();
			Iterator<Map.Entry<MaterialURLType, String>> itr = m.getWebPresences().entrySet().iterator();
			
			i: while (itr.hasNext()) {
				Map.Entry<MaterialURLType, String> ent = itr.next();
				if (ent.getValue().length() < 0) continue i;
				
				atoms.add(new URLPromotionAtom(nameFor(ent.getKey()), ent.getValue(), null));
			}
			
			this.cachedAtoms.put(m, atoms);
			return atoms;
		}
	}
	
	private String nameFor(MaterialURLType type) {
		switch (type) {
			case Soundcloud: return "Soundcloud Link";
			case Zippyshare: return "Zippyshare Link";
			case Beatport: return "Beatport Link";
			case Juno: return "Juno Link";
		}
		
		return "Link";
	}
}

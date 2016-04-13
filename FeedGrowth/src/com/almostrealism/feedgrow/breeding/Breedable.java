package com.almostrealism.feedgrow.breeding;

import java.util.List;

public interface Breedable {
	public Breedable breed(Breedable b, List<Breeder> l);
}

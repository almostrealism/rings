package com.almostrealism.feedgrow.cellular;

import com.almostrealism.feedgrow.content.ProteinReceivable;

public interface Transmitter<T> extends ProteinReceivable<T> {
	public void setReceptor(Receptor<T> r);
}

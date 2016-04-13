package com.almostrealism.feedgrow.cellular;

import com.almostrealism.feedgrow.content.ProteinReceivable;

public interface Receptor<T> extends ProteinReceivable<T> {
	public void push(long proteinIndex);
}

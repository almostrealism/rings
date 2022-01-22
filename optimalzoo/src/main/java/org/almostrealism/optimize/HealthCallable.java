package org.almostrealism.optimize;

import io.almostrealism.code.ComputeRequirement;
import org.almostrealism.CodeFeatures;
import org.almostrealism.time.Temporal;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class HealthCallable<T extends Temporal, S extends HealthScore> implements Callable<S>, CodeFeatures {
	public static ComputeRequirement computeRequirements[] = {};

	private HealthComputation<T, S> health;
	private Supplier<T> target;
	private Consumer<S> healthListener;
	private HealthScoring scoring;
	private Runnable cleanup;

	public HealthCallable(Supplier<T> target, HealthComputation health, HealthScoring scoring, Consumer<S> healthListener, Runnable cleanup) {
		this.health = health;
		this.target = target;
		this.scoring = scoring;
		this.healthListener = healthListener;
		this.cleanup = cleanup;
	}

	@Override
	public S call() {
		return cc(() -> {
			S health = null;

			try {
				this.health.setTarget(target.get());
				health = this.health.computeHealth();
				scoring.pushScore(health);

				if (healthListener != null) {
					healthListener.accept(health);
				}
			} finally {
				this.health.reset();
				this.cleanup.run();
			}

			return health;
		}, computeRequirements);
	}

	public static void setComputeRequirements(ComputeRequirement... expectations) {
		computeRequirements = expectations;
	}
}

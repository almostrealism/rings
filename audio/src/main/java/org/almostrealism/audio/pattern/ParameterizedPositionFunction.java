/*
 * Copyright 2022 Michael Murray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.almostrealism.audio.pattern;

import org.almostrealism.audio.data.ParameterFunction;
import org.almostrealism.audio.data.ParameterSet;

public class ParameterizedPositionFunction {

	private ParameterFunction regularity;
	private ParameterFunction regularityOffset;
	private ParameterFunction rate;
	private ParameterFunction rateOffset;

	public ParameterizedPositionFunction() { }

	public ParameterizedPositionFunction(ParameterFunction regularity, ParameterFunction regularityOffset, ParameterFunction rate, ParameterFunction rateOffset) {
		this.regularity = regularity;
		this.regularityOffset = regularityOffset;
		this.rate = rate;
		this.rateOffset = rateOffset;
	}

	public ParameterFunction getRegularity() {
		return regularity;
	}

	public void setRegularity(ParameterFunction regularity) {
		this.regularity = regularity;
	}

	public ParameterFunction getRegularityOffset() {
		return regularityOffset;
	}

	public void setRegularityOffset(ParameterFunction regularityOffset) {
		this.regularityOffset = regularityOffset;
	}

	public ParameterFunction getRate() {
		return rate;
	}

	public void setRate(ParameterFunction rate) {
		this.rate = rate;
	}

	public ParameterFunction getRateOffset() {
		return rateOffset;
	}

	public void setRateOffset(ParameterFunction rateOffset) {
		this.rateOffset = rateOffset;
	}

	public double apply(ParameterSet params, double position, double scale) {
		position = regularize(params, position, scale);
		double r = 2 + rate.apply(params);
		double o = rateOffset.apply(params);
		return Math.sin(Math.PI * (Math.pow(2.0, 10) * position * r + Math.pow(2.0, 3) * o));
	}

	public double applyPositive(ParameterSet params, double position, double scale) {
		// TODO  Should this wrap instead of being continuous?
		return Math.abs(apply(params, position, scale));
	}

	protected double regularize(ParameterSet params, double position, double scale) {
		return applyPositionalAlt(params, position + regularityOffset.apply(params), scale);
	}

	private double applyPositionalAlt(ParameterSet params, double position, double scale) {
		double selection = regularity.power(2.0, 3, 1).apply(params);
		position = mod(position, 1.0);

		double regularity = scale * selection;
		// System.out.println("Regularity = " + regularity);
		position = position / regularity;
		position = mod(position, 1.0);

		return position;
	}

	private double mod(double value, double denominator) {
		int result = (int) Math.floor(value / denominator);
		return value - result * denominator;
	}

	private static double applyPositional(double selection, double position, double scale) {
		if (selection > 0.0) selection = Math.floor(3 * selection);
		if (selection < 0.0) selection = Math.ceil(3 * selection);

		while (position < 0.0) position = position + 1.0;
		while (position > 1.0) position = position - 1.0;

		double regularity = scale * Math.pow(2.0, selection);

		int i;
		for (i = 0; position > 0; i++) {
			position -= regularity;
		}

//		if (i % 2 == 0) {
//			return operator.applyAsDouble(position);
//		} else {
//			return -operator.applyAsDouble(position);
//		}

		return position;
	}

	public static ParameterizedPositionFunction random() {
		return new ParameterizedPositionFunction(ParameterFunction.random(), ParameterFunction.random(),
												ParameterFunction.random(), ParameterFunction.random());
	}
}

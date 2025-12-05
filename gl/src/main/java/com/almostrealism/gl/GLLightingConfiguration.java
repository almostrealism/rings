/*
 * Copyright 2018 Michael Murray
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.almostrealism.gl;

import org.almostrealism.algebra.Vector;
import org.almostrealism.color.Light;
import org.almostrealism.color.PointLight;
import org.almostrealism.color.RGB;

public class GLLightingConfiguration {
	private Vector light0Position = new Vector();
	private RGB light0Diffuse = new RGB();

	private Vector light1Position = new Vector();
	private RGB light1Diffuse = new RGB();

	private Vector light2Position = new Vector();
	private RGB light2Diffuse = new RGB();

	private Vector light3Position = new Vector();
	private RGB light3Diffuse = new RGB();

	private Vector light4Position = new Vector();
	private RGB light4Diffuse = new RGB();

	private Vector light5Position = new Vector();
	private RGB light5Diffuse = new RGB();

	private Vector light6Position = new Vector();
	private RGB light6Diffuse = new RGB();

	private Vector light7Position = new Vector();
	private RGB light7Diffuse = new RGB();

	public GLLightingConfiguration(Iterable<Light> lights) {
		int index = 0;

		for (Light l : lights) {
			if (!(l instanceof PointLight)) return;
			set(index, ((PointLight) l).getLocation());
			set(index, l.getColor().multiply(l.getIntensity()));
			index++;
		}
	}

	public void set(int i, Vector v) {
		switch (i) {
			case 0: light0Position = v; return;
			case 1: light1Position = v; return;
			case 2: light2Position = v; return;
			case 3: light3Position = v; return;
			case 4: light4Position = v; return;
			case 5: light5Position = v; return;
			case 6: light6Position = v; return;
			case 7: light7Position = v;
		}
	}

	public void set(int i, RGB c) {
		switch (i) {
			case 0: light0Diffuse = c; return;
			case 1: light1Diffuse = c; return;
			case 2: light2Diffuse = c; return;
			case 3: light3Diffuse = c; return;
			case 4: light4Diffuse = c; return;
			case 5: light5Diffuse = c; return;
			case 6: light6Diffuse = c; return;
			case 7: light7Diffuse = c;
		}
	}

	public Vector getLight0Position() {
		return light0Position;
	}

	public void setLight0Position(Vector light0Position) {
		this.light0Position = light0Position;
	}

	public RGB getLight0Diffuse() {
		return light0Diffuse;
	}

	public void setLight0Diffuse(RGB light0Diffuse) {
		this.light0Diffuse = light0Diffuse;
	}

	public Vector getLight1Position() {
		return light1Position;
	}

	public void setLight1Position(Vector light1Position) {
		this.light1Position = light1Position;
	}

	public RGB getLight1Diffuse() {
		return light1Diffuse;
	}

	public void setLight1Diffuse(RGB light1Diffuse) {
		this.light1Diffuse = light1Diffuse;
	}

	public Vector getLight2Position() {
		return light2Position;
	}

	public void setLight2Position(Vector light2Position) {
		this.light2Position = light2Position;
	}

	public RGB getLight2Diffuse() {
		return light2Diffuse;
	}

	public void setLight2Diffuse(RGB light2Diffuse) {
		this.light2Diffuse = light2Diffuse;
	}

	public Vector getLight3Position() {
		return light3Position;
	}

	public void setLight3Position(Vector light3Position) {
		this.light3Position = light3Position;
	}

	public RGB getLight3Diffuse() {
		return light3Diffuse;
	}

	public void setLight3Diffuse(RGB light3Diffuse) {
		this.light3Diffuse = light3Diffuse;
	}

	public Vector getLight4Position() { return light4Position; }

	public void setLight4Position(Vector light4Position) {
		this.light4Position = light4Position;
	}

	public RGB getLight4Diffuse() {
		return light4Diffuse;
	}

	public void setLight4Diffuse(RGB light3Diffuse) {
		this.light4Diffuse = light3Diffuse;
	}

	public Vector getLight5Position() {
		return light5Position;
	}

	public void setLight5Position(Vector light5Position) {
		this.light5Position = light5Position;
	}

	public RGB getLight5Diffuse() {
		return light5Diffuse;
	}

	public void setLight5Diffuse(RGB light5Diffuse) {
		this.light5Diffuse = light5Diffuse;
	}

	public Vector getLight6Position() {
		return light6Position;
	}

	public void setLight6Position(Vector light6Position) {
		this.light6Position = light6Position;
	}

	public RGB getLight6Diffuse() {
		return light6Diffuse;
	}

	public void setLight6Diffuse(RGB light16iffuse) {
		this.light6Diffuse = light16iffuse;
	}

	public Vector getLight7Position() { return light7Position; }

	public void setLight7Position(Vector light7Position) {
		this.light7Position = light7Position;
	}

	public RGB getLight7Diffuse() { return this.light7Diffuse; }

	public void setLight7Diffuse(RGB light7Diffuse) {
		this.light7Diffuse = light7Diffuse;
	}
}

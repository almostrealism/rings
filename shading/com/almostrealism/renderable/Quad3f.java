/*
 * Copyright (c) 2005 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN
 * MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR
 * ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR
 * DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
 * DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF
 * SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed or intended for use
 * in the design, construction, operation or maintenance of any nuclear
 * facility.
 * 
 * Sun gratefully acknowledges that this software was originally authored
 * and developed by Kenneth Bradley Russell and Christopher John Kline.
 */

package com.almostrealism.renderable;

import org.almostrealism.algebra.Vector;

/**
 * A quadrilateral in which the vertices are three-dimensional
 * floating-point values.
 *
 * @author Kenneth Russell
 */
// TODO  This should extend RenderableGeometry
public class Quad3f {
	private Vector[] vecs;

	public static final int UPPER_LEFT = 0;
	public static final int LOWER_LEFT = 1;
	public static final int LOWER_RIGHT = 2;
	public static final int UPPER_RIGHT = 3;

	private static final int NUM_VECS = 4;

	/**
	 * Constructs a {@link Quad3f} in which all the {@link Vector}s are set to the
	 * origin.
	 */
	public Quad3f() {
		vecs = new Vector[NUM_VECS];

		for (int i = 0; i < NUM_VECS; i++) {
			vecs[i] = new Vector();
		}
	}

	/**
	 * Constructs a {@link Quad3f} in which the {@link Vector}s are set to the
	 * specified values.
	 */
	public Quad3f(Vector upperLeft, Vector lowerLeft, Vector lowerRight, Vector upperRight) {
		this();
		setVec(0, upperLeft);
		setVec(1, lowerLeft);
		setVec(2, lowerRight);
		setVec(3, upperRight);
	}

	/** Sets the specified vertex to the specified value. */
	public void setVec(int which, Vector val) {
		vecs[which].setTo(val);
	}

	/** Returns the specified vertex. */
	public Vector getVec(int which) {
		return vecs[which];
	}

	/** Sets all four points of this quadrilateral. */
	public void set(Quad3f quad) {
		for (int i = 0; i < NUM_VECS; i++) {
			setVec(i, quad.getVec(i));
		}
	}

	/**
	 * Returns a newly-constructed Quad2f in which all vertices have
	 * been multiplied in scalar fashion by the passed value.
	 */
	public Quad3f times(float val) {
		return new Quad3f(getVec(0).multiply(val),
				getVec(1).multiply(val),
				getVec(2).multiply(val),
				getVec(3).multiply(val));
	}

	/**
	 * Returns a newly-constructed Quad2f in which the vertices are the
	 * component-wise sums of this quad and the passed quad.
	 */
	public Quad3f plus(Quad3f val) {
		return new Quad3f(getVec(0).add(val.getVec(0)),
				getVec(1).add(val.getVec(1)),
				getVec(2).add(val.getVec(2)),
				getVec(3).add(val.getVec(3)));
	}
}

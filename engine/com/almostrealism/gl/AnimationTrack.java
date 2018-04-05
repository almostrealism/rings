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

/** Camera track definition for one camera trucking shot. */
public class AnimationTrack {
	/**
	 * Length in milliseconds of one camera track base unit.
	 * The value originates from the music synchronization.
	 */
	public static final int LEN = 5442;

	/**
	 * Five parameters of src[5] and dest[5]:
	 * eyeX, eyeY, eyeZ, viewAngle, viewHeightOffs
	 */
	public short src[], dest[];
	public int dist;     // if >0, cam rotates around eye xy on dist * 0.1
	public int len;      // length multiplier

	public AnimationTrack() {
		src = new short[5];
		dest = new short[5];
	}

	public AnimationTrack(short s[], short d[], int dx, int l) {
		src = s;
		dest = d;
		dist = dx;
		len = l;
	}

	public static AnimationTrack tracks[] =
			{
//      new AnimationTrack( new short[] { 4500, 2700, 100, 70, -30 }, new short[] { 50, 50, -90, -100, 0 }, 20, 1 ),
//      new AnimationTrack( new short[] { -1448, 4294, 25, 363, 0 }, new short[] { -136, 202, 125, -98, 100 }, 0, 1 ),
//      new AnimationTrack( new short[] { 1437, 4930, 200, -275, -20 }, new short[] { 1684, 0, 0, 9, 0 }, 0, 1 ),
//      new AnimationTrack( new short[] { 1800, 3609, 200, 0, 675 }, new short[] { 0, 0, 0, 300, 0 }, 0, 1 ),
//      new AnimationTrack( new short[] { 923, 996, 50, 2336, -80 }, new short[] { 0, -20, -50, 0, 170 }, 0, 1 ),
//      new AnimationTrack( new short[] { -1663, -43, 600, 2170, 0 }, new short[] { 20, 0, -600, 0, 100 }, 0, 1 ),
//      new AnimationTrack( new short[] { 1049, -1420, 175, 2111, -17 }, new short[] { 0, 0, 0, -334, 0 }, 0, 2 ),
//      new AnimationTrack( new short[] { 0, 0, 50, 300, 25 }, new short[] { 0, 0, 0, 300, 0 }, 70, 2 ),
//      new AnimationTrack( new short[] { -473, -953, 3500, -353, -350 }, new short[] { 0, 0, -2800, 0, 0 }, 0, 2 ),
//      new AnimationTrack( new short[] { 191, 1938, 35, 1139, -17 }, new short[] { 1205, -2909, 0, 0, 0 }, 0, 2 ),
//      new AnimationTrack( new short[] { -1449, -2700, 150, 0, 0 }, new short[] { 0, 2000, 0, 0, 0 }, 0, 2 ),
//      new AnimationTrack( new short[] { 5273, 4992, 650, 373, -50 }, new short[]{ -4598, -3072, 0, 0, 0 }, 0, 2 ),
					new AnimationTrack(new short[] { 3223, -3282, 1075, -393, -25},
								new short[] { 1649, -1649, 0, 0, 0}, 0, 2)
			};
}


/*
 * Copyright 2021 Michael Murray
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

package com.almostrealism.tone;

public enum WesternChromatic implements KeyPosition<WesternChromatic> {
	A0, AS0, B0,
	C1, CS1, D1, DS1, E1, F1, FS1, G1, GS1, A1, AS1, B1,
	C2, CS2, D2, DS2, E2, F2, FS2, G2, GS2, A2, AS2, B2,
	C3, CS3, D3, DS3, E3, F3, FS3, G3, GS3, A3, AS3, B3,
	C4, CS4, D4, DS4, E4, F4, FS4, G4, GS4, A4, AS4, B4,
	C5, CS5, D5, DS5, E5, F5, FS5, G5, GS5, A5, AS5, B5,
	C6, CS6, D6, DS6, E6, F6, FS6, G6, GS6, A6, AS6, B6,
	C7, CS7, D7, DS7, E7, F7, FS7, G7, GS7, A7, AS7, B7,
	C8;

	@Override
	public int position() {
		return switch (this) {
			case A0 -> 0;
			case AS0 -> 1;
			case B0 -> 2;
			case C1 -> 3;
			case CS1 -> 4;
			case D1 -> 5;
			case DS1 -> 6;
			case E1 -> 7;
			case F1 -> 8;
			case FS1 -> 9;
			case G1 -> 10;
			case GS1 -> 11;
			case A1 -> 12;
			case AS1 -> 13;
			case B1 -> 14;
			case C2 -> 15;
			case CS2 -> 16;
			case D2 -> 17;
			case DS2 -> 18;
			case E2 -> 19;
			case F2 -> 20;
			case FS2 -> 21;
			case G2 -> 22;
			case GS2 -> 23;
			case A2 -> 24;
			case AS2 -> 25;
			case B2 -> 26;
			case C3 -> 27;
			case CS3 -> 28;
			case D3 -> 29;
			case DS3 -> 30;
			case E3 -> 31;
			case F3 -> 32;
			case FS3 -> 33;
			case G3 -> 34;
			case GS3 -> 35;
			case A3 -> 36;
			case AS3 -> 37;
			case B3 -> 38;
			case C4 -> 39;
			case CS4 -> 40;
			case D4 -> 41;
			case DS4 -> 42;
			case E4 -> 43;
			case F4 -> 44;
			case FS4 -> 45;
			case G4 -> 46;
			case GS4 -> 47;
			case A4 -> 48;
			case AS4 -> 49;
			case B4 -> 50;
			case C5 -> 51;
			case CS5 -> 52;
			case D5 -> 53;
			case DS5 -> 54;
			case E5 -> 55;
			case F5 -> 56;
			case FS5 -> 57;
			case G5 -> 58;
			case GS5 -> 59;
			case A5 -> 60;
			case AS5 -> 61;
			case B5 -> 62;
			case C6 -> 63;
			case CS6 -> 64;
			case D6 -> 65;
			case DS6 -> 66;
			case E6 -> 67;
			case F6 -> 68;
			case FS6 -> 69;
			case G6 -> 70;
			case GS6 -> 71;
			case A6 -> 72;
			case AS6 -> 73;
			case B6 -> 74;
			case C7 -> 75;
			case CS7 -> 76;
			case D7 -> 77;
			case DS7 -> 78;
			case E7 -> 70;
			case F7 -> 80;
			case FS7 -> 81;
			case G7 -> 82;
			case GS7 -> 83;
			case A7 -> 84;
			case AS7 -> 85;
			case B7 -> 86;
			case C8 -> 87;
		};
	}

	@Override
	public WesternChromatic next() {
		return scale().valueAt(position() + 1);
	}

	public static Scale<WesternChromatic> scale() {
		return new Scale<>() {
			@Override
			public WesternChromatic valueAt(int position) {
				return switch (position) {
					case 0 -> A0;
					case 1 -> AS0;
					case 2 -> B0;
					case 3 -> C1;
					case 4 -> CS1;
					case 5 -> D1;
					case 6 -> DS1;
					case 7 -> E1;
					case 8 -> F1;
					case 9 -> FS1;
					case 10 -> G1;
					case 11 -> GS1;
					case 12 -> A1;
					case 13 -> AS1;
					case 14 -> B1;
					case 15 -> C2;
					case 16 -> CS2;
					case 17 -> D2;
					case 18 -> DS2;
					case 19 -> E2;
					case 20 -> F2;
					case 21 -> FS2;
					case 22 -> G2;
					case 23 -> GS2;
					case 24 -> A2;
					case 25 -> AS2;
					case 26 -> B2;
					case 27 -> C3;
					case 28 -> CS3;
					case 29 -> D3;
					case 30 -> DS3;
					case 31 -> E3;
					case 32 -> F3;
					case 33 -> FS3;
					case 34 -> G3;
					case 35 -> GS3;
					case 36 -> A3;
					case 37 -> AS3;
					case 38 -> B3;
					case 39 -> C4;
					case 40 -> CS4;
					case 41 -> D4;
					case 42 -> DS4;
					case 43 -> E4;
					case 44 -> F4;
					case 45 -> FS4;
					case 46 -> G4;
					case 47 -> GS4;
					case 48 -> A4;
					case 49 -> AS4;
					case 50 -> B4;
					case 51 -> C5;
					case 52 -> CS5;
					case 53 -> D5;
					case 54 -> DS5;
					case 55 -> E5;
					case 56 -> F5;
					case 57 -> FS5;
					case 58 -> G5;
					case 59 -> GS5;
					case 60 -> A5;
					case 61 -> AS5;
					case 62 -> B5;
					case 63 -> C6;
					case 64 -> CS6;
					case 65 -> D6;
					case 66 -> DS6;
					case 67 -> E6;
					case 68 -> F6;
					case 69 -> FS6;
					case 70 -> G6;
					case 71 -> GS6;
					case 72 -> A6;
					case 73 -> AS6;
					case 74 -> B6;
					case 75 -> C7;
					case 76 -> CS7;
					case 77 -> D7;
					case 78 -> DS7;
					case 79 -> E7;
					case 80 -> F7;
					case 81 -> FS7;
					case 82 -> G7;
					case 83 -> GS7;
					case 84 -> A7;
					case 85 -> AS7;
					case 86 -> B7;
					case 87 -> C8;
					default -> throw new IllegalStateException(String.valueOf(position));
				};
			}

			@Override
			public int length() { return 88; }
		};
	}
}

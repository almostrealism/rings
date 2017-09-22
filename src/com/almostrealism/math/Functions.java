/*
 * Copyright 2016 Michael Murray
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

package com.almostrealism.math;

import java.util.List;

import io.almostrealism.db.DatabaseConnection;

/**
 * The Functions class provides static methods that perform
 * some interesting mathematical functions.
 * 
 * @author Mike Murray
 */
public class Functions {
	private static List primes;
	
	public static double totient(long n) {
		double value = n;
		
		i: for (int i = 0; i < Functions.primes.size();) {
			long p = ((Long)Functions.primes.get(i)).longValue();
			
			if (p > n) break i;
			
			if (n % p == 0) {
				value = value * (1.0 - (1.0 / p));
				
				n = n / p;
				
				continue i;
			}
			
			i++;
		}
		
		return value;
	}
	
	public static void loadPrimes(DatabaseConnection db, long pi, long pf) {
		// TODO Load primes from database and put in Functions.primes.
	}
}

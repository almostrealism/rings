/*
 * Copyright (C) 2005  Mike Murray
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License (version 2)
 *  as published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 */

package net.sf.j3d.math;

import java.util.List;

import net.sf.j3d.network.db.DatabaseConnection;

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

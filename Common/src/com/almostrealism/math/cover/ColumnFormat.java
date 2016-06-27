package com.almostrealism.math.cover;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

public class ColumnFormat {
	public static void main(String args[]) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(args[0]));
		PrintStream out = new PrintStream(new FileOutputStream(args[0] + ".new"));
		int l = Integer.parseInt(args[1]);
		int w = Integer.parseInt(args[2]);
		
		while (true) {
			String output[] = new String[l];
			
			for (int c = 0; c < w; c++) {
				for (int i = 0; i < l; i++) {
					String line = in.readLine();
					if (line == null) return;
					
					if (output[i] == null)
						output[i] = line;
					else
						output[i] = output[i] + "\t" + line;
				}
			}
			
			for (int i = 0; i < output.length; i++) {
				out.println(output[i]);
			}
		}
	}
}

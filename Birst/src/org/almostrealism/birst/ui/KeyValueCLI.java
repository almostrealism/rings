package org.almostrealism.birst.ui;

import java.util.Scanner;

public class KeyValueCLI implements Runnable {
	private Scanner scanner;
	
	public KeyValueCLI() {
		scanner = new Scanner(System.in);
	}

	@Override
	public void run() {
		String line;
		
		while ((line = scanner.next()) != null) {
			line = line.trim();
			int index = line.indexOf(" ");
			
			if (index < 0) {
				cmd = line;
			} else {
				String cmd = line.substring(0, index);
			}
		}
	}
}

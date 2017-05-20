package org.almostrealism.birst.ui;

import java.util.Scanner;

import org.almostrealism.birst.util.DuplicateKeyException;
import org.almostrealism.birst.util.KeyValueStore;
import org.almostrealism.birst.util.UnknownKeyException;

public class KeyValueCLI implements Runnable {
	private KeyValueStore store;
	private Scanner scanner;
	
	public KeyValueCLI() {
		store = new KeyValueStore();
		scanner = new Scanner(System.in);
	}
	
	public void run() {
		String line;
		
		System.out.print(">");
		
		while ((line = scanner.next()) != null) {
			line = line.trim();
			int index = line.indexOf(" ");
			
			String cmd;
			
			if (index < 0) {
				cmd = line;
			} else {
				cmd = line.substring(0, index);
			}
			
			try {
				String arg = line.substring(index + 1);
				
				if (cmd.equals("CREATE")) {
					String split[] = arg.split("=");
					store.put(split[0].trim(), split[1].trim());
				} else if (cmd.equals("UPDATE")) {
					String split[] = arg.split("=");
					store.update(split[0].trim(), split[1].trim());
				} else if (cmd.equals("GET")) {
					System.out.println(store.get(arg.trim()));
				} else if (cmd.equals("DELETE")) {
					store.remove(arg);
				} else if (cmd.equals("GETALL")) {
					for (String s : store.keys()) {
						System.out.println(s + " = " + store.get(s));
					}
				} else if (cmd.equals("QUIT")) {
					System.exit(0);
				}
			} catch (DuplicateKeyException e) {
				System.out.println("Key " + e.getKey() + " already exists");
			} catch (UnknownKeyException e) {
				System.out.println("Key not found");
			}
			
			System.out.print(">");
		}
	}
	
	public static void main(String args[]) {
		KeyValueCLI cli = new KeyValueCLI();
		Thread t = new Thread(cli);
		t.start();
	}
}

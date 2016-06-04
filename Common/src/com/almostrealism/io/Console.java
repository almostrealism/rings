package com.almostrealism.io;

public class Console {
	public static boolean systemOutEnabled = true;
	
	private StringBuffer data = new StringBuffer();
	private StringBuffer lastLine = new StringBuffer();
	private boolean resetLastLine = false;
	
	public void print(String s) {
		if (resetLastLine) lastLine = new StringBuffer();
		
		data.append(s);
		lastLine.append(s);
		
		if (systemOutEnabled)
			System.out.print(s);
	}
	
	public void println(String s) {
		if (resetLastLine) lastLine = new StringBuffer();
		
		data.append(s);
		data.append("\n");
		
		lastLine.append(s);
		resetLastLine = true;
		
		if (systemOutEnabled)
			System.out.println(s);
	}
	
	public void println() {
		if (resetLastLine) lastLine = new StringBuffer();
		
		data.append("\n");
		resetLastLine = true;
		
		if (systemOutEnabled)
			System.out.println();
	}
	
	public String lastLine() { return lastLine.toString(); }
}

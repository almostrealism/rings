package net.sf.j3d.iptables;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

/**
 * Protocal:
 *   0 - TCP
 *   1 - UDP
 *   2 - ICMP
 * 
 * Decision:
 *   0 - ACCEPT
 *   1 - DROP
 *   2 - REJECT
 *   3 - RETURN
 *   4 - LOG
 * 
 * @author Mike Murray
 */
public class IptablesParser {
	public static boolean verbose = false;
	
	public static final String iptables_var = "$IPTABLES";
	
	private static Hashtable vars = new Hashtable();
	private static List ignore = new ArrayList();
	private static List fallback = new ArrayList();;
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		ignore.add("--syn");
		ignore.add("-f");
		
		BufferedReader in = new BufferedReader(new FileReader(args[0]));
		
		boolean stop = false;
		
		w: while (!stop) {
			String line = in.readLine();
			if (line == null) break w;
			
			while (line.endsWith("/") || line.endsWith("\\")) {
				line = line.substring(0, line.length() - 1) + in.readLine().trim();
			}
			
			line = line.trim();
			
			if (line.startsWith("#")) continue w;
			
			if (verbose) System.out.println(line);
			
			if (line.startsWith(iptables_var) || line.startsWith("iptables")) {
				parseIptables(line);
			} else if (line.contains("=")) {
				String s[] = line.split("=");
				vars.put("$" + s[0], s[1].replace("\"", ""));
				if (verbose) System.out.println("Parser: " + s[0] + " = " + s[1]);
			}
		}
		
		Iterator itr = fallback.iterator();
		while (itr.hasNext()) {
			System.out.println(itr.next());
		}
	}
	
	public static void parseIptables(String line) {
		if (line.contains(" -F")) return;
		if (line.contains(" -X")) return;
		if (line.contains(" -Z")) return;
		if (line.contains(" -N")) return;
		
		if (line.contains("-P")) {
			String s = line.substring(line.lastIndexOf(" ") + 1);
			String ss[] = line.split(" ");
			IptablesElement ip = new IptablesElement();
			ip.set("-A", ss[2]);
			ip.set("-j", s);
			fallback.add(ip.toString());
			return;
		}
		
		IptablesElement l = new IptablesElement();
		String data[] = line.split(" ");
		
		i: for (int i = 1; i < data.length - 1; i = i + 2) {
			if (data[i].equals("!")) i++;
			while (!data[i].startsWith("-") || ignore.contains(data[i])) {
				i++;
				if (i >= data.length) break i;
			}
			
			String h = data[i];
			String k = data[i + 1];
			
			w: while (k.startsWith("\"") && !k.endsWith("\"")) {
				i++;
				k = k + " " + data[i + 1];
			}
			
			if (vars.containsKey(k))
				k = (String) vars.get(k);
			
			l.set(h, k);
		}
		
		System.out.println(l);
	}
}

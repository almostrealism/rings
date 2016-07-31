package net.sf.j3d.iptables;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

public class IptablesElement {
	private static long max_int;
	
	private static final int tcp_code = 0;
	private static final int udp_code = 1;
	private static final int icmp_code = 2;
	
	private static final String tcp = "tcp";
	private static final String udp = "udp";
	private static final String icmp = "icmp";
	
	private static Hashtable services = new Hashtable();
	private static Hashtable devices = new Hashtable();
	
	private int protocal = -1;
	private int device = -2;
	private long sourceIpStart = 0;
	private long sourceIpEnd = 0;
	private long destIpStart = 0;
	private long destIpEnd = 0;
	
	private List sourcePortRanges = new ArrayList();
	private List destPortRanges = new ArrayList();
	
	private String chain, task, state = "ANY";
	private StringBuffer extra = new StringBuffer();
	
	public IptablesElement() {
		if (this.max_int == 0) {
			for (int i = 0; i < 32; i++)
				this.max_int += ((long)1) << i;

			this.services.put("ftp-data", new int[] {20, 20});
			this.services.put("ftp", new int[] {21, 21});
			this.services.put("telnet", new int[] {21, 21});
			this.services.put("smtp", new int[] {25, 25});
			this.services.put("domain", new int[] {53, 53});
			this.services.put("http", new int[] {80, 80});
			this.services.put("www", new int[] {80, 80});
			this.services.put("irc", new int[] {6667, 6667});
			
			this.devices.put("lo", new Integer(-1));
			this.devices.put("eth0", new Integer(0));
			this.devices.put("eth1", new Integer(1));
			this.devices.put("eth2", new Integer(2));
		}
		
		this.sourceIpEnd = this.max_int;
		this.destIpEnd = this.max_int;
	}
	
	public void set(String key, String value) {
		if (key.equals("-p")) {
			if (value.equals(tcp))
				this.protocal = tcp_code;
			else if (value.equals(udp))
				this.protocal = udp_code;
			else if (value.equals(icmp))
				this.protocal = icmp_code;
			else
				System.out.println("IptablesElement: Unsupported protocal (" + value + ")");
		} else if (key.equals("-i") || key.equals("-o")) {
			Integer in = (Integer) devices.get(value);
			if (in == null) {
				System.out.println("INTERFACE = " + value);
			} else {
				this.device = in.intValue();
			}
		} else if (key.equals("-A")) {
			this.chain = value;
		} else if (key.equals("-j")) {
			if (value.equalsIgnoreCase("accept"))
				this.task = "0";
			else if (value.equalsIgnoreCase("drop"))
				this.task = "1";
			else if (value.equalsIgnoreCase("reject"))
				this.task = "2";
			else if (value.equalsIgnoreCase("return"))
				this.task = "3";
			else if (value.equalsIgnoreCase("log"))
				this.task = "4";
			else
				this.task = value;
		} else if (key.equals("-s")) {
			String s[] = value.split("/");
			int mask;
			
			if (s.length == 1)
				mask = 32;
			else
				mask = Integer.parseInt(s[1]);
			
			this.setSourceIp(this.parseIp(s[0]), mask);
		} else if (key.equals("-d")) {
			String s[] = value.split("/");
			int mask;
			
			if (s.length == 1)
				mask = 32;
			else
				mask = Integer.parseInt(s[1]);
			
			this.setDestIp(this.parseIp(s[0]), mask);
		} else if (key.equals("--src-range")) {
			String s[] = value.split("-");
			this.sourceIpStart = this.parseIp(s[0]);
			this.sourceIpEnd = this.parseIp(s[1]);
		} else if (key.equals("--dst-range")) {
			String s[] = value.split("-");
			this.destIpStart = this.parseIp(s[0]);
			this.destIpEnd = this.parseIp(s[1]);
		} else if (key.equals("--sports") || key.equals("--sport") ||
					key.equals("--source-port")) {
			String s[] = value.split(",");
			for (int i = 0; i < s.length; i++)
				this.sourcePortRanges.add(this.getRange(s[i]));
		} else if (key.equals("--dports") || key.equals("--dport") ||
					key.equals("--destination-port")) {
			String s[] = value.split(",");
			for (int i = 0; i < s.length; i++)
				this.destPortRanges.add(this.getRange(s[i]));
		} else if (key.equals("--state")) {
			this.state = value;
		} else if (key.equals("--reject-with") ||
					key.equals("--log-prefix")) {
			this.extra.append(key + " " + value + " ");
		} else if (IptablesParser.verbose) {
			System.out.println(key + " " + value);
		}
	}
	
	public long parseIp(String ip) {
		if (ip.equals("0")) return 0;
		ip = ip.replace(".", " ");
		String ss[] = ip.split(" ");
		long source = (((long)Integer.parseInt(ss[0])) << 24) +
					(((long)Integer.parseInt(ss[1])) << 16) +
					(((long)Integer.parseInt(ss[2])) << 8) +
					((long)Integer.parseInt(ss[3]));
		
		return source;
	}
	
	public long getMax(long ip, int mask) {
		long m = 0;
		for (int i = 0; i < mask; i++) m += ((long)1) << (31 - i);
		return (ip & m) + (this.max_int - m);
	}
	
	public void setSourceIp(long ip, int mask) {
		this.sourceIpStart = ip;
		this.sourceIpEnd = this.getMax(ip, mask);
	}
	
	public void setDestIp(long ip, int mask) {
		this.destIpStart = ip;
		this.destIpEnd = this.getMax(ip, mask);
	}
	
	public int[] getRange(String range) {
		if (services.containsKey(range))
			return (int[]) services.get(range);
		
		range.replace("/", ":");
		
		if (range.contains(":")) {
			String s[] = range.split(":");
			return new int[] {Integer.parseInt(s[0]),
								Integer.parseInt(s[1])};
		} else {
			int i = Integer.parseInt(range);
			return new int[] {i, i};
		}
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		if (this.sourcePortRanges.size() < 1)
			this.sourcePortRanges.add(new int[] {0, 2 * Short.MAX_VALUE});
		if (this.destPortRanges.size() < 1)
			this.destPortRanges.add(new int[] {0, 2 * Short.MAX_VALUE});
		
		Iterator itr = this.sourcePortRanges.iterator();
		
		boolean one = false;
		
		while (itr.hasNext()) {
			Iterator ditr = this.destPortRanges.iterator();
			int sports[] = (int[]) itr.next();
			
			while (ditr.hasNext()) {
				if (one) buf.append("\n");
				else one = true;
				
				int dports[] = (int[]) ditr.next();
				
				buf.append(this.chain);
				buf.append(" ");
				buf.append(this.state);
				buf.append(" ");
				buf.append(this.task);
				buf.append(" ");
				
				if (this.protocal < 0)
					buf.append("[0,2] ");
				else
					buf.append("[" + this.protocal + "," + this.protocal + "] ");
				
				if (this.device < -1)
					buf.append("[0,2] ");
				else
					buf.append("[" + this.device + "," + this.device + "] ");
				
				buf.append("[" + this.sourceIpStart + "," + this.sourceIpEnd + "] ");
				buf.append("[" + sports[0] + "," + sports[1] + "] ");
				buf.append("[" + this.destIpStart + "," + this.destIpEnd + "] ");
				buf.append("[" + dports[0] + "," + dports[1] + "] ");
				
				buf.append(this.extra.toString());
			}
		}
		
		return buf.toString();
	}
}

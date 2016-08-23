package com.almostrealism.glitchfarm.line;

import com.almostrealism.glitchfarm.util.DataProducer;

public class DefaultDualOutputLine implements DualOutputLine, Runnable, DataProducer {
	protected static class HalfLine implements OutputLine {
		private DefaultDualOutputLine line;
		private boolean isLeft;
		
		public HalfLine(DefaultDualOutputLine l, boolean left) {
			this.line = l;
			this.isLeft = left;
		}
		
		public void write(byte[] b) {
			if (this.isLeft) {
				this.line.writeLeft(b);
			} else {
				this.line.writeRight(b);
			}
		}
	}
	
	private OutputLine output;
	private HalfLine left, right;
	private byte leftValue[], rightValue[];
//	private List leftBuf, rightBuf;
	protected double mix = 0.5;
//	private boolean isFirstLeft = true, isFirstRight = true;
	private boolean stop;
	
	public DefaultDualOutputLine(OutputLine output) {
		this.output = output;
		this.left = new HalfLine(this, true);
		this.right = new HalfLine(this, false);
//		this.leftBuf = new ArrayList();
//		this.rightBuf = new ArrayList();
//		new Thread(this).start();
	}
	
	public OutputLine getLeftLine() { return this.left; }
	public OutputLine getRightLine() { return this.right; }
	public double getMix() { return this.mix; }
	public void setMix(double m) { this.mix = m; }
	
	public void run() {
		while (!stop) {
			if (this.leftValue != null & this.rightValue != null) {
				System.out.println(this.leftValue + " " + this.rightValue);
				doWrite(this.leftValue, this.rightValue);
				this.leftValue = null;
				this.rightValue = null;
			}
			
//			if (this.leftBuf.size() > 0 && this.rightBuf.size() > 0) {
//				this.doWrite((byte[]) this.leftBuf.get(0), (byte[]) this.rightBuf.get(0));
//				synchronized (this.leftBuf) { this.leftBuf.remove(0); }
//				synchronized (this.rightBuf) { this.rightBuf.remove(0); }
//			}
		}
	}
	
	public void writeLeft(byte b[]) {
		this.leftValue = b;
		
//		if (!isFirstLeft) {
//			synchronized (this.rightBuf) {
//				try {
//					this.rightBuf.wait();
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//		
//		isFirstLeft = false;
//		
//		this.leftBuf.add(b);
//		this.leftBuf.notify();
	}
	
	public void writeRight(byte b[]) {
		this.rightValue = b;
		
//		if (!isFirstRight) {
//			synchronized (this.leftBuf) {
//				try {
//					this.leftBuf.wait();
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//		
//		isFirstRight = false;
//		
//		this.rightBuf.add(b);
//		this.rightBuf.notify();
	}
	
	protected void write(byte b[]) {
		this.output.write(b);
	}
	
	public void writeNext() { this.write(this.next()); }
	
	public byte[] next() {
		return this.doWrite(this.leftValue, this.rightValue);
	}
	
	protected byte[] doWrite(byte left[], byte right[]) {
		byte b[] = new byte[left.length];
		
		for (int i = 0; i < b.length; i++)
			b[i] = (byte) ((1.0 - mix) * left[i] + mix * right[i]);
		
		return b;
	}
}

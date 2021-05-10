package org.example;

public class FractalPixel {
	public int x;
	public int y;
	public long iterations;
	public byte[] color = new byte[3];

	public FractalPixel(int x, int y) {
		this.x = x;
		this.y = y;
	}
}
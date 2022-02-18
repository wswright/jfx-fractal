package org.example;

import org.example.fractal.lib.ComplexAlgebraicForm;
import org.example.fractal.lib.IFractalEquation;
import org.example.fractal.equations.MandelBrotFractalEquation;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

public class Fractal {
	public static Map<Long, byte[]> colors = new ConcurrentHashMap<>();
	public FractalPixel[][] pixels;
	public int width;
	public int height;
	public AtomicLong pixelsCalculated = new AtomicLong(0);
	public static IFractalEquation currentEquation = new MandelBrotFractalEquation();
	Random randomSource = new Random();
	public double X_LOWER;
	public double X_UPPER;
	public double Y_LOWER;
	public double Y_UPPER;
	public double centerX, centerY;
	public static final int MAX_ITERATIONS = 60;
	public static final int ESCAPE_LIMIT = 1024;
	public static AtomicLong totalIters = new AtomicLong(0);
	public int chunkXOffset = 0;
	public int chunkYOffset = 0;

	public Fractal(int width, int height, double xLower, double xUpper, double yLower, double yUpper) {
		this.width = width;

		this.height = height;
		this.X_LOWER = xLower;
		this.X_UPPER = xUpper;
		this.Y_LOWER = yLower;
		this.Y_UPPER = yUpper;
		centerX = (X_UPPER+X_LOWER) / 2.0;
		centerY = (Y_UPPER+Y_LOWER) / 2.0;
		pixels = new FractalPixel[this.width][this.height];
		for(int y=0; y<height; y++) {
			for(int x=0; x<width; x++) {
				pixels[x][y] = new FractalPixel(x, y);
			}
		}
	}

	public Fractal(int width, int height) {
		this(width,height, App.X_LOWER, App.X_UPPER, App.Y_LOWER, App.Y_UPPER);
	}

	public static Fractal fromChunk(int xChunk, int yChunk, int x_chunks, int y_chunks, int defaultChunkSize, int width, int height, double xLower, double xUpper, double yLower, double yUpper) {
		//Calculate X and Y remainder if this is the last chunk, meaning it might not be a full chunk
		int xRemainder = defaultChunkSize, yRemainder = defaultChunkSize;
		if(xChunk+1 == x_chunks) {
			xRemainder = width % defaultChunkSize;
			if(xRemainder==0)
				xRemainder = defaultChunkSize;
		}
		if(yChunk+1 == y_chunks) {
			yRemainder = height % defaultChunkSize;
			if(yRemainder==0)
				yRemainder = defaultChunkSize;
		}

		double xDiff = (xUpper-xLower) / (double)x_chunks;
		double yDiff = (yUpper-yLower) / (double)y_chunks;
		double calcXLower = xDiff * (double)xChunk + xLower;
		double calcYLower = yDiff * (double)yChunk + yLower;
		final Fractal fractal = new Fractal(xRemainder, yRemainder, calcXLower, calcXLower + xDiff, calcYLower, calcYLower + yDiff);
		fractal.chunkXOffset = (xChunk) * defaultChunkSize;
		fractal.chunkYOffset = (yChunk) * defaultChunkSize;
		return fractal;
	}

	private byte[] getRandomColor() {
		final byte[] bytes = new byte[3];
		randomSource.nextBytes(bytes);
		return bytes;
	}

	public void getIterations() {
		Map<Long, Long> iterations = new ConcurrentHashMap<>(16, 0.9f, 64);
		IntStream.range(0,height).forEach(y -> IntStream.range(0, width).forEach(x -> iterations.merge(pixels[x][y].iterations, 1L, Long::sum)));
		iterations.keySet().forEach(i -> colors.putIfAbsent(i, getRandomColor()));
		IntStream.range(0,height).forEach(y -> {
			IntStream.range(0, width).forEach(x -> {
				pixels[x][y].color = colors.get(pixels[x][y].iterations);
			});
		});
		return;
	}

	/**
	 * The main pixel calculation happens here.
	 * @param cr Real part
	 * @param ci Imaginary Part
	 * @param max_it Maximum Iterations
	 * @return Returns the number of iterations it took to cross the threshold
	 */
	public int iterations(double cr, double ci, int max_it) {
		ComplexAlgebraicForm z = new ComplexAlgebraicForm(0,0);
		final ComplexAlgebraicForm c = new ComplexAlgebraicForm(cr, ci);
//		final ComplexTrigForm complexTrigForm = ComplexTrigForm.fromAlgebraicForm(c);
		int i=0;

		while(i++<max_it && z.abs() < ESCAPE_LIMIT)
			z = currentEquation.calculateFractalIteration(z,c);

		totalIters.addAndGet(i);
		return i;
	}

	public Fractal calculate() {
		pixelsCalculated.set(0);
		IntStream.range(0, height).parallel().forEach(y -> {
			IntStream.range(0, width).parallel().forEach(x -> {
				calculatePixel(x,y);
			});
		});
		return this;
	}

	private void calculatePixel(int x, int y) {
		pixels[x][y].iterations = iterations(linmap(x, 0, width, X_LOWER, X_UPPER), linmap(y, 0, height, Y_LOWER, Y_UPPER), MAX_ITERATIONS);
		pixels[x][y].color[0] = (byte) pixels[x][y].iterations;
		pixels[x][y].color[1] = (byte) pixels[x][y].iterations;
		pixels[x][y].color[2] = (byte) pixels[x][y].iterations;
		pixelsCalculated.incrementAndGet();
	}

	/**
	 * Maps a value from one number range (number-range-1) to another (number-range-2).
	 * @param val The value to map from number-range-1 to number-range-2. Example: 0.2
	 * @param lower1 The lowest value in number-range-1. Example: 0
	 * @param upper1 The highest value in number-range-1. Example: 1
	 * @param lower2 The lowest value in number-range-2. Example: 0
	 * @param upper2 The highest value in number-range-2. Example: 10
	 * @return Returns val converted from number-range-1 to number-range-2. Example: 2 (Because 0.2 * 10)
	 */
	public static double linmap(double val, double lower1, double upper1, double lower2, double upper2) {
		return ((val - lower1) / (upper1 - lower1)) * (upper2 - lower2) + lower2;
	}

	public byte[] getImageData(byte[] imageData) {
		int i=0;
		for(int y=0; y<this.height; y++) {
			for (int x = 0; x < width; x++) {
				imageData[i] = pixels[x][y].color[0];
				imageData[i+1] = pixels[x][y].color[1];
				imageData[i+2] = pixels[x][y].color[2];
				i+=3;
			}
		}
		return imageData;
	}
}
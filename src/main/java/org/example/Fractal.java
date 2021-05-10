package org.example;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Fractal {
	public static Map<Long, byte[]> colors = new ConcurrentHashMap<>();
	public FractalPixel[][] pixels;
	public int width;
	public int height;
	public AtomicLong pixelsCalculated = new AtomicLong(0);
	public IFractalEquation currentEquation = new MandelBrotFractalEquation();
	Random randomSource = new Random();
	public double X_LOWER = -2.5;
	public double X_UPPER = 1.0555555;
	public double Y_LOWER = -1;
	public double Y_UPPER = 1;
	public static final int MAX_ITERATIONS = 64;
	public static final int ESCAPE_LIMIT = 1000;
	public static AtomicLong totalIters = new AtomicLong(0);

	public Fractal(int width, int height, double xLower, double xUpper, double yLower, double yUpper) {
		this.width = width;
		this.height = height;
		this.X_LOWER = xLower;
		this.X_UPPER = xUpper;
		this.Y_LOWER = yLower;
		this.Y_UPPER = yUpper;
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

	private byte[] getRandomColor() {
		final byte[] bytes = new byte[3];
		randomSource.nextBytes(bytes);
		return bytes;
	}

	public Map<Long, Long> getIterations() {
		Instant very_start = Instant.now();
		Instant start = Instant.now();
		Map<Long, Long> iterations = new ConcurrentHashMap<>(16, 0.9f, 64);
		final Set<Long> collectedIterations = Arrays.stream(pixels).parallel().map(p -> Arrays.stream(p).parallel().map(pp -> pp.iterations).distinct().collect(Collectors.toList())).flatMap(ppp -> ppp.stream().parallel().distinct()).collect(Collectors.toSet());
		collectedIterations.forEach(i -> iterations.put(i, 0L));

		System.out.println("\tZero HashMap: " + Duration.between(start, Instant.now()).toMillis() + "ms");
		start = Instant.now();

		IntStream.range(0,height).parallel().forEach(y -> {
			IntStream.range(0, width).parallel().forEach(x -> {
				iterations.put(pixels[x][y].iterations, iterations.get(pixels[x][y].iterations)+1L);
			});
		});

		System.out.println("\tPopulate HashMap: " + Duration.between(start, Instant.now()).toMillis() + "ms");
		start = Instant.now();
		iterations.keySet().parallelStream().forEach(i -> colors.put(i, getRandomColor()));

		System.out.println("\tGenerate & Store Colors: " + Duration.between(start, Instant.now()).toMillis() + "ms");
		start = Instant.now();

		IntStream.range(0,height).parallel().forEach(y -> {
			IntStream.range(0, width).parallel().forEach(x -> {
				pixels[x][y].color = colors.get(pixels[x][y].iterations);
			});
		});

		System.out.println("\tAssign Colors to pixels: " + Duration.between(start, Instant.now()).toMillis() + "ms");
		System.out.println("\tTOTAL TIME: " + Duration.between(very_start, Instant.now()).toMillis() + "ms");
		return iterations;
	}

	/**
	 * The main pixel calculation happens here.
	 * @param cr
	 * @param ci
	 * @param max_it
	 * @return
	 */
	public int iterations(double cr, double ci, int max_it) {
		Complex z = new Complex(0,0);
		final Complex c = new Complex(cr, ci);
		int i=0;
		for(; i<max_it; i++) {
			z = currentEquation.calculateFractalIteration(z, c);

			if(z.abs() > ESCAPE_LIMIT)
				break;
		}
		totalIters.addAndGet(i);
		return i;
	}

	public void calculate() {
		pixelsCalculated.set(0);
		IntStream.range(0, height).parallel().forEach(y -> {
			IntStream.range(0, width).parallel().forEach(x -> {
				calculatePixel(x,y);
			});
		});
	}

	private void calculatePixel(int x, int y) {
		pixels[x][y].iterations = iterations(linmap(x, 0, width, X_LOWER, X_UPPER), linmap(y, 0, height, Y_LOWER, Y_UPPER), MAX_ITERATIONS);
		pixels[x][y].color[0] = (byte) (pixels[x][y].iterations % 255);
		pixels[x][y].color[1] = (byte) (pixels[x][y].iterations % 255);
		pixels[x][y].color[2] = (byte) (pixels[x][y].iterations % 255);
		pixelsCalculated.incrementAndGet();
	}

	private static double linmap(double val, double lower1, double upper1, double lower2, double upper2) {
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
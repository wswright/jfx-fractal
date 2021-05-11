package org.example;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Fractal {
	public static Map<Long, byte[]> colors = new ConcurrentHashMap<>();
	public FractalPixel[][] pixels;
	public int width;
	public int height;
	public AtomicLong pixelsCalculated = new AtomicLong(0);
	public IFractalEquation currentEquation = new MandelBrotFractalEquation();
	Random randomSource = new Random();
	public double X_LOWER;
	public double X_UPPER;
	public double Y_LOWER;
	public double Y_UPPER;
	public double centerX, centerY = 0;
	public static final int MAX_ITERATIONS = 64*64;
	public static final int ESCAPE_LIMIT = 1000000;
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

	public Map<Long, Long> getIterations() {
		Instant very_start = Instant.now();
		Instant start = Instant.now();
		Map<Long, Long> iterations = new ConcurrentHashMap<>(16, 0.9f, 64);
		final Set<Long> collectedIterations = Arrays.stream(pixels).parallel().map(p -> Arrays.stream(p).parallel().map(pp -> pp.iterations)
				.distinct()
				.collect(Collectors.toList())).flatMap(ppp -> Stream.concat(ppp.stream().parallel().distinct(), colors.keySet().parallelStream())).collect(Collectors.toSet());
		collectedIterations.forEach(i -> iterations.put(i, 0L));

//		System.out.println("\tZero HashMap: " + Duration.between(start, Instant.now()).toMillis() + "ms");
		start = Instant.now();

		IntStream.range(0,height).parallel().forEach(y -> {
			IntStream.range(0, width).parallel().forEach(x -> {
				iterations.put(pixels[x][y].iterations, iterations.get(pixels[x][y].iterations)+1L);
			});
		});

//		System.out.println("\tPopulate HashMap: " + Duration.between(start, Instant.now()).toMillis() + "ms");
		start = Instant.now();
		iterations.keySet().parallelStream().forEach(i -> colors.putIfAbsent(i, getRandomColor()));

//		System.out.println("\tGenerate & Store Colors: " + Duration.between(start, Instant.now()).toMillis() + "ms");
		start = Instant.now();

		IntStream.range(0,height).parallel().forEach(y -> {
			IntStream.range(0, width).parallel().forEach(x -> {
				pixels[x][y].color = colors.get(pixels[x][y].iterations);
			});
		});

//		System.out.println("\tAssign Colors to pixels: " + Duration.between(start, Instant.now()).toMillis() + "ms");
//		System.out.println("\tTOTAL TIME: " + Duration.between(very_start, Instant.now()).toMillis() + "ms");
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
//		System.out.printf("<<Calculating Fractal>> [%d x %d]%n", width, height);
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
package org.example;

import javafx.scene.canvas.Canvas;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

public class FractalRenderer {
	private Canvas canvas;
	public static final double SCROLL_ZOOM_FACTOR = 1.2;
	private static final double PAN_AMOUNT = 0.05;
	public static int WIDTH = 640*2;
	public static int HEIGHT = 480*2;
	public static double X_LOWER = -2.5;
	public static double X_UPPER = 1.0555555;
	public static double Y_LOWER = -1;
	public static double Y_UPPER = 1;
	public double CENTER_X;
	public double CENTER_Y;
	public int X_CHUNKS = 0;
	public int Y_CHUNKS = 0;
	private static final int DEFAULT_CHUNK_SIZE = 64;//pixels
	private byte[] imageData = new byte[0];

	public FractalRenderer() {
		imageData = new byte[WIDTH*HEIGHT*3];
		calculateChunkSize();
	}

	public void calculateChunkSize() {
		X_CHUNKS = (WIDTH - (WIDTH % DEFAULT_CHUNK_SIZE)) / DEFAULT_CHUNK_SIZE;
		Y_CHUNKS = (HEIGHT - (HEIGHT % DEFAULT_CHUNK_SIZE)) / DEFAULT_CHUNK_SIZE;
		if(X_CHUNKS*DEFAULT_CHUNK_SIZE < WIDTH)
			X_CHUNKS++;
		if(Y_CHUNKS*DEFAULT_CHUNK_SIZE < HEIGHT)
			Y_CHUNKS++;
		System.out.println("X_CHUNKS: " + X_CHUNKS);
		System.out.println("Y_CHUNKS: " + Y_CHUNKS);
	}

	void panFractal(double panX, double panY) {
		double xOffset = (X_UPPER - X_LOWER) * (panX);
		double yOffset = (Y_UPPER - Y_LOWER) * (panY);

		X_LOWER += xOffset;
		X_UPPER += xOffset;
		Y_LOWER += yOffset;
		Y_UPPER += yOffset;
		CENTER_X = (X_LOWER + X_UPPER) / 2.0;
		CENTER_Y = (Y_LOWER + Y_UPPER) / 2.0;
		System.out.printf("Panning! [X: %f-%f][Y: %f-%f]%n", X_LOWER, X_UPPER, Y_LOWER, Y_UPPER);
		renderFractal();
	}

	public void setCanvas(Canvas canvas) {
		this.canvas = canvas;
	}

	private void renderChunk(int xChunk, int yChunk) {
		Instant start = Instant.now();
//		System.out.println(String.format("renderChunk::Rendering chunk [%d,%d] of [%d,%d] total.", xChunk, yChunk, X_CHUNKS, Y_CHUNKS));
		Fractal fractal = Fractal.fromChunk(xChunk, yChunk, X_CHUNKS, Y_CHUNKS, DEFAULT_CHUNK_SIZE, WIDTH, HEIGHT, X_LOWER, X_UPPER, Y_LOWER, Y_UPPER);
		fractal.calculate();
		final Map<Long, Long> iterationsHistogram = fractal.getIterations();
//		System.out.printf("renderChunk::\t[Entries: %d][%dms]%n", iterationsHistogram.size(), Duration.between(start, Instant.now()).toMillis());

		for(int y=0; y<DEFAULT_CHUNK_SIZE; y++) {
			int curY = y + (yChunk*DEFAULT_CHUNK_SIZE);
			for (int x = 0; x < DEFAULT_CHUNK_SIZE; x++) {
				int curX = x + (xChunk*DEFAULT_CHUNK_SIZE);
				int i = ((curY * WIDTH) + curX) * 3;
				imageData[i] = fractal.pixels[x][y].color[0];
				imageData[i+1] = fractal.pixels[x][y].color[1];
				imageData[i+2] = fractal.pixels[x][y].color[2];
			}
		}
	}


	void renderFractal() {
		Instant veryStart = Instant.now();
		Instant start = Instant.now();
		System.out.printf("Initializing...[W: %d][H: %d][X: %f-%f][Y: %f-%f]", WIDTH, HEIGHT, X_LOWER, X_UPPER, Y_LOWER, Y_UPPER);

		App.setTitleText(String.format("Initializing...[W: %d][H: %d][X: %f-%f][Y: %f-%f]", WIDTH, HEIGHT, X_LOWER, X_UPPER, Y_LOWER, Y_UPPER));

		imageData = new byte[WIDTH*HEIGHT*3];
		Fractal.totalIters = new AtomicLong(0);
		final PixelWriter pw = canvas.getGraphicsContext2D().getPixelWriter();
		PixelFormat<ByteBuffer> pixelFormat = PixelFormat.getByteRgbInstance();
		IntStream.range(0, Y_CHUNKS).parallel().forEach(ychunk -> {
			IntStream.range(0, X_CHUNKS).parallel().forEach(xchunk -> {
				renderChunk(xchunk, ychunk);
			});
		});
//		Fractal fractal = new Fractal(WIDTH, HEIGHT, X_LOWER, X_UPPER, Y_LOWER, Y_UPPER);
		CENTER_X = (X_LOWER + X_UPPER) / 2.0;
		CENTER_Y = (Y_LOWER + Y_UPPER) / 2.0;
		System.out.printf("[%dms]%n", Duration.between(start, Instant.now()).toMillis());
		start = Instant.now();
		System.out.printf("Calculating...[Threads: %d][Escape: %d][MaxIterations: %d]", Runtime.getRuntime().availableProcessors(), Fractal.ESCAPE_LIMIT, Fractal.MAX_ITERATIONS);
		App.setTitleText(String.format("Calculating...[Threads: %d]", Runtime.getRuntime().availableProcessors()));

//		fractal.calculate();

		int i=0;
		System.out.printf("[%dms]%n", Duration.between(start, Instant.now()).toMillis());
		start = Instant.now();
		System.out.println("Calculating Histogram...");
		App.setTitleText("Calculating Histogram...");
//		final Map<Long, Long> iterationsHistogram = fractal.getIterations();
//		System.out.printf("\t[Entries: %d][%dms]%n", iterationsHistogram.size(), Duration.between(start, Instant.now()).toMillis());


		start = Instant.now();
		System.out.print("Buffering...");
		App.setTitleText("Buffering...");

//		imageData = fractal.getImageData(imageData);

//		for(int y=0; y<HEIGHT; y++) {
//			for (int x = 0; x < WIDTH; x++) {
//				imageData[i] = fractal.pixels[x][y].color[0];
//				imageData[i+1] = fractal.pixels[x][y].color[1];
//				imageData[i+2] = fractal.pixels[x][y].color[2];
//				i+=3;
//			}
//		}
		System.out.printf("[%dms]%n", Duration.between(start, Instant.now()).toMillis());
		start = Instant.now();
		System.out.print("Rendering...");
		App.setTitleText("Rendering...");


		pw.setPixels(0, 0, WIDTH, HEIGHT, pixelFormat, imageData, 0, WIDTH*3);
		System.out.printf("[%dms]%n", Duration.between(start, Instant.now()).toMillis());
		System.out.println("Total Iterations: " + Fractal.totalIters.get());
		System.out.println("Iters/Pixel: " + Fractal.totalIters.get() / (1.0f*WIDTH * HEIGHT));
		App.setTitleText("Total Iterations: " + Fractal.totalIters.get());
		System.out.printf("Total Duration: [%dms]%n", Duration.between(veryStart, Instant.now()).toMillis());
//		fractal = null;

		//Only Hints, nothing more
		System.gc();
		System.runFinalization();
		System.out.println("+------------------------------------------------------------------------------------------+");
	}
}

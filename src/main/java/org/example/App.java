package org.example;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.ScrollEvent;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

/**
 * JavaFX App
 */
public class App extends Application {
	public static int WIDTH = 640*2;
	public static int HEIGHT = 480*2;
	public static double X_LOWER = -2.5;
	public static double X_UPPER = 1.0555555;
	public static double Y_LOWER = -1;
	public static double Y_UPPER = 1;
	public static final int MAX_ITERATIONS = 4096;
	public static final int ESCAPE_LIMIT = 123456789;
	private Stage theStage;
	public static AtomicLong totalIters = new AtomicLong(0);
	private Canvas canvas = new Canvas();
	public static Random randomSource = new Random();


	public static void main(String[] args) {
		launch();
	}

	@Override
	public void start(Stage stage) {
		this.theStage = stage;

		this.canvas = new Canvas(WIDTH, HEIGHT);
		final Button btnGo = new Button("GO!");
		btnGo.setOnAction(event -> {
			System.out.println("GOING!");
			renderFractal(canvas);

		});
		var scene = new Scene(new Group(canvas, btnGo));
		stage.setScene(scene);
		stage.addEventHandler(ScrollEvent.SCROLL, getScrollEventEventHandler());
		stage.show();
		stage.widthProperty().addListener((observable, oldValue, newValue) -> {
			WIDTH = newValue.intValue();
			canvas.setWidth(WIDTH);
		});
		stage.heightProperty().addListener((observable, oldValue, newValue) -> {
			HEIGHT = newValue.intValue();
			canvas.setHeight(HEIGHT);
		});
	}

	private void renderFractal(Canvas canvas) {
		Instant veryStart = Instant.now();
		Instant start = Instant.now();
		System.out.printf("Initializing...[W: %d][H: %d][X: %f-%f][Y: %f-%f]", WIDTH, HEIGHT, X_LOWER, X_UPPER, Y_LOWER, Y_UPPER);
		this.theStage.setTitle(String.format("Initializing...[W: %d][H: %d][X: %f-%f][Y: %f-%f]", WIDTH, HEIGHT, X_LOWER, X_UPPER, Y_LOWER, Y_UPPER));

		byte[] imageData = new byte[WIDTH*HEIGHT*3];
		totalIters = new AtomicLong(0);
		final PixelWriter pw = canvas.getGraphicsContext2D().getPixelWriter();
		PixelFormat<ByteBuffer> pixelFormat = PixelFormat.getByteRgbInstance();
		Fractal fractal = new Fractal(WIDTH, HEIGHT);
		System.out.printf("[%dms]%n", Duration.between(start, Instant.now()).toMillis());
		start = Instant.now();
		System.out.printf("Calculating...[Threads: %d][Escape: %d][MaxIterations: %d]", Runtime.getRuntime().availableProcessors(), ESCAPE_LIMIT, MAX_ITERATIONS);
		this.theStage.setTitle(String.format("Calculating...[Threads: %d]", Runtime.getRuntime().availableProcessors()));

		fractal.calculate();

		int i=0;
		System.out.printf("[%dms]%n", Duration.between(start, Instant.now()).toMillis());
		start = Instant.now();
		System.out.println("Calculating Histogram...");
		this.theStage.setTitle("Calculating Histogram...");
		final Map<Long, Long> iterationsHistogram = fractal.getIterations();
		System.out.printf("\t[Entries: %d][%dms]%n", iterationsHistogram.size(), Duration.between(start, Instant.now()).toMillis());


		start = Instant.now();
		System.out.print("Buffering...");
		this.theStage.setTitle("Buffering...");

		for(int y=0; y<HEIGHT; y++) {
				for (int x = 0; x < WIDTH; x++) {
					imageData[i] = fractal.pixels[x][y].color[0];
					imageData[i+1] = fractal.pixels[x][y].color[1];
					imageData[i+2] = fractal.pixels[x][y].color[2];
					i+=3;
				}
		}
		System.out.printf("[%dms]%n", Duration.between(start, Instant.now()).toMillis());
		start = Instant.now();
		System.out.print("Rendering...");
		this.theStage.setTitle("Rendering...");


		pw.setPixels(0, 0, WIDTH, HEIGHT, pixelFormat, imageData, 0, WIDTH*3);
		System.out.printf("[%dms]%n", Duration.between(start, Instant.now()).toMillis());
		System.out.println("Total Iterations: " + totalIters.get());
		System.out.println("Iters/Pixel: " + totalIters.get() / (1.0f*WIDTH * HEIGHT));
		this.theStage.setTitle("Total Iterations: " + totalIters.get());
		System.out.printf("TOTAL DURATION: [%dms]%n", Duration.between(veryStart, Instant.now()).toMillis());
		fractal = null;

		//Only Hints, nothing more
		System.gc();
		System.runFinalization();
		System.out.println("+------------------------------------------------------------------------------------------+");
	}

	public static class Fractal {
		public static Map<Long, byte[]> colors = new ConcurrentHashMap<>();
		public FractalPixel[][] pixels;
		public int width;
		public int height;
		public double x_lower, x_upper, y_lower, y_upper;
		public AtomicLong pixelsCalculated = new AtomicLong(0);
		public IFractalEquation currentEquation = new MandelBrotFractalEquation();

		private byte[] getRandomColor() {
			final byte[] bytes = new byte[3];
			randomSource.nextBytes(bytes);
			return bytes;
		}

		public Map<Long, Long> getIterations() {
			Instant very_start = Instant.now();
			Instant start = Instant.now();
			Map<Long, Long> iterations = new ConcurrentHashMap<>();
			IntStream.range(0,height).parallel().forEach(y -> {
				IntStream.range(0, width).parallel().forEach(x -> {
					iterations.put(pixels[x][y].iterations, 0L);
				});
			});

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

		public Fractal(int width, int height) {
			this.width = width;
			this.height = height;
			this.x_lower = X_LOWER;
			this.x_upper = X_UPPER;
			this.y_lower = Y_LOWER;
			this.y_upper = Y_UPPER;
			pixels = new FractalPixel[this.width][this.height];
			for(int y=0; y<height; y++) {
				for(int x=0; x<width; x++) {
					pixels[x][y] = new FractalPixel(x, y);
				}
			}
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
	}

	public static class FractalPixel {
		public int x;
		public int y;
		public long iterations;
		public byte[] color = new byte[3];


		public FractalPixel(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

	private static double linmap(double val, double lower1, double upper1, double lower2, double upper2) {
		return ((val - lower1) / (upper1 - lower1)) * (upper2 - lower2) + lower2;
	}


	/**
	 * Gets the onScroll event handler. This is used to respond to scroll events and move the camera.
	 * @implNote  Also sets the label's text property to display the camera's coordinates.
	 * @return Returns a {@link EventHandler<ScrollEvent>}.
	 */
	@NotNull
	private EventHandler<ScrollEvent> getScrollEventEventHandler() {
		return event -> {
			double delta = event.getDeltaY();
			if(delta > 0) {
				X_LOWER *= 1.05;
				X_UPPER *= 1.05;
				Y_LOWER *= 1.05;
				Y_UPPER *= 1.05;
			} else {
				X_LOWER /= 1.05;
				X_UPPER /= 1.05;
				Y_LOWER /= 1.05;
				Y_UPPER /= 1.05;
			}
			System.out.println(String.format("X: [%f, %f], Y: [%f, %f]", X_LOWER, X_UPPER, Y_LOWER, Y_UPPER));
			renderFractal(canvas);
		};
	}
}
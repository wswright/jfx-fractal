package org.example;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * JavaFX App
 */
public class App extends Application {
	public static final double SCROLL_ZOOM_FACTOR = 1.2;
	private static final double PAN_AMOUNT = 0.05;
	public static int WIDTH = 640*2;
	public static int HEIGHT = 480*2;
	public static double X_LOWER = -2.5;
	public static double X_UPPER = 1.0555555;
	public static double Y_LOWER = -1;
	public static double Y_UPPER = 1;

	private Stage theStage;
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
		stage.addEventHandler(KeyEvent.KEY_PRESSED, getKeyPressedEventHandler());
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

	private EventHandler<KeyEvent> getKeyPressedEventHandler() {
		return event -> {
			switch (event.getCode()) {
				case W -> panWindow(0, PAN_AMOUNT);
				case S -> panWindow(0, -PAN_AMOUNT);
				case A -> panWindow(-PAN_AMOUNT, 0);
				case D -> panWindow(PAN_AMOUNT, 0);
				default -> {}
			}
		};
	}

	private void panWindow(double panX, double panY) {
		X_LOWER += panX;
		X_UPPER += panX;
		Y_LOWER += panY;
		Y_UPPER += panY;
		System.out.printf("Panning! [X: %f-%f][Y: %f-%f]%n", X_LOWER, X_UPPER, Y_LOWER, Y_UPPER);
		renderFractal(canvas);
	}

	private void renderFractal(Canvas canvas) {
		Instant veryStart = Instant.now();
		Instant start = Instant.now();
		System.out.printf("Initializing...[W: %d][H: %d][X: %f-%f][Y: %f-%f]", WIDTH, HEIGHT, X_LOWER, X_UPPER, Y_LOWER, Y_UPPER);
		this.theStage.setTitle(String.format("Initializing...[W: %d][H: %d][X: %f-%f][Y: %f-%f]", WIDTH, HEIGHT, X_LOWER, X_UPPER, Y_LOWER, Y_UPPER));

		byte[] imageData = new byte[WIDTH*HEIGHT*3];
		Fractal.totalIters = new AtomicLong(0);
		final PixelWriter pw = canvas.getGraphicsContext2D().getPixelWriter();
		PixelFormat<ByteBuffer> pixelFormat = PixelFormat.getByteRgbInstance();
		Fractal fractal = new Fractal(WIDTH, HEIGHT, X_LOWER, X_UPPER, Y_LOWER, Y_UPPER);
		System.out.printf("[%dms]%n", Duration.between(start, Instant.now()).toMillis());
		start = Instant.now();
		System.out.printf("Calculating...[Threads: %d][Escape: %d][MaxIterations: %d]", Runtime.getRuntime().availableProcessors(), Fractal.ESCAPE_LIMIT, Fractal.MAX_ITERATIONS);
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
		System.out.println("Total Iterations: " + Fractal.totalIters.get());
		System.out.println("Iters/Pixel: " + Fractal.totalIters.get() / (1.0f*WIDTH * HEIGHT));
		this.theStage.setTitle("Total Iterations: " + Fractal.totalIters.get());
		System.out.printf("TOTAL DURATION: [%dms]%n", Duration.between(veryStart, Instant.now()).toMillis());
		fractal = null;

		//Only Hints, nothing more
		System.gc();
		System.runFinalization();
		System.out.println("+------------------------------------------------------------------------------------------+");
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
				X_LOWER *= SCROLL_ZOOM_FACTOR;
				X_UPPER *= SCROLL_ZOOM_FACTOR;
				Y_LOWER *= SCROLL_ZOOM_FACTOR;
				Y_UPPER *= SCROLL_ZOOM_FACTOR;
			} else {
				X_LOWER /= SCROLL_ZOOM_FACTOR;
				X_UPPER /= SCROLL_ZOOM_FACTOR;
				Y_LOWER /= SCROLL_ZOOM_FACTOR;
				Y_UPPER /= SCROLL_ZOOM_FACTOR;
			}
			System.out.println(String.format("X: [%f, %f], Y: [%f, %f]", X_LOWER, X_UPPER, Y_LOWER, Y_UPPER));
			renderFractal(canvas);
		};
	}
}
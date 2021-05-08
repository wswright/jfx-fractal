package org.example;

import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.controlsfx.control.PopOver;
import org.example.editor.EditorObjectScreen;
import org.example.editor.EditorUtils;
import org.example.fxui.SmartGroup;
import org.example.fxui.WindowDocker;
import org.example.game.GameObject;
import org.example.world.WorldCreationScreen;
import org.example.world.WorldMapScreen;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

/**
 * JavaFX App
 */
public class App extends Application {
	public static final int CAMERA_FAR_CLIP = 10000;
	public static int WIDTH = 640*2;
	public static int HEIGHT = 480*2;
	public static double X_LOWER = -2.5;
	public static double X_UPPER = 1.0555555;
	public static double Y_LOWER = -1;
	public static double Y_UPPER = 1;
	public static final int MAX_ITERATIONS = 4096;
	public static final int ESCAPE_LIMIT = 123456789;
	private Camera camera;
	private Group content3DArea;
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
		var javaVersion = SystemInfo.javaVersion();
		var javafxVersion = SystemInfo.javafxVersion();

		final boolean is3DSupported = Platform.isSupported(ConditionalFeature.SCENE3D);
		if (!is3DSupported) {
			System.out.println("Sorry, but 3D is not supported in JavaFX on this platform.");
			return;
		}

		this.canvas = new Canvas(WIDTH, HEIGHT);
		final Button btnGo = new Button("GO!");
		btnGo.setOnAction(event -> {
			System.out.println("GOING!");
			renderFractal(canvas);

		});
		var scene = new Scene(new Group(canvas, btnGo));
		stage.setScene(scene);
		stage.addEventHandler(ScrollEvent.SCROLL, getScrollEventEventHandler());
//		stage.addEventHandler(KeyEvent.KEY_PRESSED, getKeyPressedEventHandler());
//		stage.setMaximized(true);
		stage.show();
		stage.widthProperty().addListener((observable, oldValue, newValue) -> {
			WIDTH = newValue.intValue();
			canvas.setWidth(WIDTH);
//			renderFractal(canvas);
		});
		stage.heightProperty().addListener((observable, oldValue, newValue) -> {
			HEIGHT = newValue.intValue();
			canvas.setHeight(HEIGHT);
//			renderFractal(canvas);
		});
//		content3DArea.requestFocus();
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
//		for(var k : iterationsHistogram.entrySet()) {
//			System.out.printf("Key: %d, Value: %d [%f]%n", k.getKey(), k.getValue(), ((double)k.getKey()*k.getValue()) / (totalIters.get()) * 100.0);
//		}
		System.out.printf("\t[Entries: %d][%dms]%n", iterationsHistogram.size(), Duration.between(start, Instant.now()).toMillis());


		start = Instant.now();
		System.out.print("Buffering...");
		this.theStage.setTitle("Buffering...");

		for(int y=0; y<HEIGHT; y++) {
//				System.out.println("PCT: " + (((y*1.0f) / (HEIGHT*1.0f))*100.0f));
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


		public long totalPixels() {
			return (long) width * height;
		}

		public boolean isDone() {
			return totalPixels() == pixelsCalculated.get();
		}

		private byte[] getRandomColor() {
			final byte[] bytes = new byte[3];
			randomSource.nextBytes(bytes);
			return bytes;
		}

//		private byte[] getRandomColor() {
//			return new byte[]{(byte)randomSource.nextInt(256), (byte)randomSource.nextInt(256), (byte)randomSource.nextInt(256)};
//		}

		public Map<Long, Long> getIterations() {
			Instant very_start = Instant.now();
			Instant start = Instant.now();
			Map<Long, Long> iterations = new ConcurrentHashMap<>();
			IntStream.range(0,height).parallel().forEach(y -> {
				IntStream.range(0, width).parallel().forEach(x -> {
					iterations.put(pixels[x][y].iterations, 0L);
				});
			});
//			for(int y=0; y<height; y++) {
//				for (int x = 0; x < width; x++) {
//					iterations.put(pixels[x][y].iterations, 0L);
//				}
//			}
			System.out.println("\tZero HashMap: " + Duration.between(start, Instant.now()).toMillis() + "ms");
			start = Instant.now();

			IntStream.range(0,height).parallel().forEach(y -> {
				IntStream.range(0, width).parallel().forEach(x -> {
					iterations.put(pixels[x][y].iterations, iterations.get(pixels[x][y].iterations)+1L);
				});
			});

//			for(int y=0; y<height; y++) {
//				for(int x=0; x<width; x++) {
//						iterations.put(pixels[x][y].iterations, iterations.get(pixels[x][y].iterations)+1L);
//				}
//			}
			System.out.println("\tPopulate HashMap: " + Duration.between(start, Instant.now()).toMillis() + "ms");
			start = Instant.now();
			iterations.keySet().parallelStream().forEach(i -> colors.put(i, getRandomColor()));
//			for(var i : iterations.entrySet()) {
//				colors.put(i.getKey(), getRandomColor());
//			}
			System.out.println("\tGenerate & Store Colors: " + Duration.between(start, Instant.now()).toMillis() + "ms");
			start = Instant.now();

			IntStream.range(0,height).parallel().forEach(y -> {
				IntStream.range(0, width).parallel().forEach(x -> {
					pixels[x][y].color = colors.get(pixels[x][y].iterations);
				});
			});
//			for(int y=0; y<height; y++) {
//				for(int x=0; x<width; x++) {
//					pixels[x][y].color = colors.get(pixels[x][y].iterations);
//				}
//			}
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

		public Fractal(int width, int height, double x_lower, double x_upper, double y_lower, double y_upper) {
			this(width, height);
			this.x_lower = x_lower;
			this.x_upper = x_upper;
			this.y_lower = y_lower;
			this.y_upper = y_upper;
		}

		public void calculate() {
			pixelsCalculated.set(0);
			IntStream.range(0, height).parallel().forEach(y -> {
//				System.out.println("PCT: " + (((pixelsCalculated.doubleValue()*1.0f) / (totalPixels()*1.0f))*100.0f));
				IntStream.range(0, width).parallel().forEach(x -> {
					calculatePixel(x,y);
				});
			});
//			for(int y=0; y<height; y++) {
//								System.out.println("PCT: " + (((y*1.0f) / (HEIGHT*1.0f))*100.0f));
//				for(int x=0; x<width; x++) {
//
//					calculatePixel(y, x);
////		System.out.println(String.format("R,G,B:  [%f, %f, %f]", r,g,b));
////					return Color.color(r,g,b);
//
//
//				}
//			}
//			System.out.println("DONE!!!");
		}

		private void calculatePixel(int x, int y) {
			pixels[x][y].iterations = iterations(linmap(x, 0, width, X_LOWER, X_UPPER), linmap(y, 0, height, Y_LOWER, Y_UPPER), MAX_ITERATIONS);
			pixels[x][y].color[0] = (byte) (pixels[x][y].iterations % 255);
			pixels[x][y].color[1] = (byte) (pixels[x][y].iterations % 255);
			pixels[x][y].color[2] = (byte) (pixels[x][y].iterations % 255);
			pixelsCalculated.incrementAndGet();
		}

		private Color getColor(int x, int y, int maxX, int maxY) {

			double x1 = linmap(x, 0, maxX, X_LOWER, X_UPPER);
			double y1 = linmap(y, 0, maxY, Y_LOWER, Y_UPPER);
			int its = iterations(x1, y1, MAX_ITERATIONS);
			double r = (its % 255) / 255.0f;
			double g =  (its % 255) / 255.0f;
			double b = g;
//		System.out.println(String.format("R,G,B:  [%f, %f, %f]", r,g,b));
			return Color.color(r,g,b);
//		return Color.color((x % 255.0) / 255, (y % 255.0) / 255,((x + y) % 255.0) / 255);
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

//	private byte[] getImageData() {
//		int i=0;
//		byte[] imageData = new byte[WIDTH*HEIGHT*3];
//		for(int y=0; y<HEIGHT; y++) {
//			for (int x = 0; x < WIDTH; x++) {
//				final Color color = getColor(x, y, WIDTH, HEIGHT);
//				imageData[i] = (byte)(color.getRed()*255.0);
//				imageData[i+1] = (byte)(color.getGreen()*255);
//				imageData[i+2] = (byte)(color.getBlue()*255);
//				i+=3;
//			}
//		}
//		return imageData;
//	}

	/* Calculate the number of iterations a point takes to leave a bound. */



	private static double linmap(double val, double lower1, double upper1, double lower2, double upper2) {
		return ((val - lower1) / (upper1 - lower1)) * (upper2 - lower2) + lower2;
	}

	private EventHandler<KeyEvent> getKeyPressedEventHandler() {
		return event -> {
			switch (event.getCode()) {
				case A -> camera.translateXProperty().set(camera.getTranslateX()-1);
				case D -> camera.translateXProperty().set(camera.getTranslateX()+1);
				case W -> camera.translateZProperty().set(camera.getTranslateZ()+1);
				case S -> camera.translateZProperty().set(camera.getTranslateZ()-1);
				case E -> camera.translateYProperty().set(camera.getTranslateY() - 1);
				case Q -> camera.translateYProperty().set(camera.getTranslateY()+1);
			}
		};
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
//			camera.translateZProperty().set(camera.getTranslateZ() + delta);
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

	private void updateCameraPositionLabel(Label label) {
		label.setText(String.format("Camera [X: %f, Y: %f, Z: %f]", camera.getTranslateX(), camera.getTranslateY(), camera.getTranslateZ()));
	}

	/**
	 * Adds {@link PopOver} support to a {@link Scene}.
	 * Rant: It is crazy that these two classes (Scene and Node) share an interface, but NOT REALLY. This forces
	 * me to write two methods here.
	 *
	 * @param n The {@link Scene} which should support {@link PopOver}s.
	 * @param p The {@link PopOver} to use when wiring events.
	 * @return Returns the same {@link Scene}. Chain away~!
	 */
	public Scene addPopOverSupportToScene(Scene n, PopOver p) {
		n.setOnMouseEntered(getOnMouseEnteredEventHandler(p));
		n.setOnMouseExited(getOnMouseExitedEventHandler(p));
		n.setOnMouseMoved(getOnMouseEnteredEventHandler(p));
		return n;
	}

	/**
	 * Adds {@link PopOver} support to a {@link Node}.
	 * Rant: It is crazy that these two classes (Node and Scene) share an interface, but NOT REALLY. This forces
	 * me to write two methods here.
	 *
	 * @param n The {@link Node} which should support {@link PopOver}s.
	 * @param p The {@link PopOver} to use when wiring events.
	 * @return Returns the same {@link Node}. Chain away~!
	 */
	public Node addPopOverSupportToNode(Node n, PopOver p) {
		n.setOnMouseEntered(getOnMouseEnteredEventHandler(p));
		n.setOnMouseExited(getOnMouseExitedEventHandler(p));
		n.setOnMouseMoved(getOnMouseEnteredEventHandler(p));
		return n;
	}

	private PerspectiveCamera getCamera() {
		final PerspectiveCamera perspectiveCamera = new PerspectiveCamera(true);
		perspectiveCamera.setFarClip(CAMERA_FAR_CLIP);
		perspectiveCamera.setNearClip(0.01);
		return perspectiveCamera;
	}

	private Group getSubSceneGroup(Group root) {
		SubScene subScene = new SubScene(root, 1024, 1024, true, SceneAntialiasing.BALANCED);
		subScene.setFill(Color.BLUEVIOLET);
		subScene.setCamera(camera);
		Group group = new Group();
		group.getChildren().add(subScene);


		Translate pivot = new Translate();
		Rotate yRotate = new Rotate(0, Rotate.Y_AXIS);

		// Create and position camera
//		this.camera.getTransforms().addAll (
//				pivot,
//				yRotate,
//				new Translate(0, 0, -500),
//		new Rotate(-20, Rotate.X_AXIS)
//		);

//		// animate the camera position.
//		Timeline timeline = new Timeline(
//				new KeyFrame(
//						Duration.seconds(0),
//						new KeyValue(yRotate.angleProperty(), 0)
//				),
//				new KeyFrame(
//						Duration.seconds(5),
//						new KeyValue(yRotate.angleProperty(), 360)
//				)
//		);
//		timeline.setCycleCount(Timeline.INDEFINITE);
//		timeline.play();



		return group;
	}

	/**
	 * Gets the OnMouseExited event handler. This is used to hide the PopOver window.
	 *
	 * @param popOver The {@link PopOver} for the scene.
	 * @return Returns an {@link EventHandler<MouseEvent>}.
	 */
	private EventHandler<MouseEvent> getOnMouseExitedEventHandler(PopOver popOver) {
		return mouseEvent -> popOver.hide();
	}

	/**
	 * Gets the OnMouseEntered event handler. This is used to show and set the content for the PopOver
	 * window.
	 *
	 * @param popOver The {@link PopOver} for the scene.
	 * @return Returns an {@link EventHandler<MouseEvent>}.
	 */
	private EventHandler<MouseEvent> getOnMouseEnteredEventHandler(PopOver popOver) {
		return mouseEvent -> {
			final Node intersectedNode = mouseEvent.getPickResult().getIntersectedNode();
			final String intersectedNodeDescription = EditorUtils.describe(intersectedNode);
			popOver.setContentNode(new Label(intersectedNodeDescription));
			if (popOver.getOwnerNode() != null) {
				if (!popOver.getOwnerNode().equals(intersectedNode) && intersectedNode != null)
					popOver.show(intersectedNode);
			} else if (intersectedNode != null) {
				popOver.show(intersectedNode);
			}
		};
	}

	/**
	 * Creates and returns the main Toolbar.
	 * @return A {@link Node} containing the toolbar.
	 */
	public Node getMainToolbar() {
		ToolBar toolBar = new ToolBar();
		final List<Node> toolbarNodes = getToolbarNodes();
		toolBar.getItems().addAll(toolbarNodes);
		return toolBar;
	}

	/**
	 * Creates and returns a {@link List} of the {@link Node} elements in the main toolbar.
	 * @return A {@link List} containing the toolbar elements.
	 */
	public List<Node> getToolbarNodes() {
		List<Node> nodes = new ArrayList<>();
		nodes.add(getButtonObjectList(content3DArea));                 //'Object List'  button
		nodes.add(getButtonCreateWorld());                             //'Create World' button
		nodes.add(getButtonAddTriangle(content3DArea));                //'Add Triangle' button
//		nodes.add(getCameraSlider(camera, Axes.X, -360, 360, 1));      //X-Axis Slider
//		nodes.add(getCameraSlider(camera, Axes.Y, -360, 360, 1));      //Y-Axis Slider
//		nodes.add(getCameraSlider(camera, Axes.Z, -360, 360, 1));      //Z-Axis Slider
		nodes.add(getButtonShowWorldMapScreen());                      //`World Map` button
		return nodes;

	}

	/**
	 * An enum for when you need code to pick an axis.
	 */
	public enum Axes {
		X,Y,Z
	}

	/**
	 * Provides a {@link Slider} which can translate the camera on an Axis.
	 * @param sceneCamera The Camera to translate when controlling the {@link Slider}.
	 * @param axis The Axis to translate the Camera on when adjusting the {@link Slider}.
	 * @param min The minimum value. Usually -360.
	 * @param max The maximum value. Usually 360.
	 * @param initial The initial value. Usually 1.
	 * @return Returns a {@link Slider}.
	 */
	public Slider getCameraSlider(Camera sceneCamera, Axes axis, int min, int max, int initial) {
		final Slider slider = new Slider(min, max, initial);
		slider.setShowTickLabels(true);
		slider.setShowTickMarks(true);
		switch (axis) {
			case X -> slider.valueProperty().addListener((observable, oldValue, newValue) -> sceneCamera.setTranslateX(newValue.doubleValue()));
			case Y -> slider.valueProperty().addListener((observable, oldValue, newValue) -> sceneCamera.setTranslateY(newValue.doubleValue()));
			case Z -> slider.valueProperty().addListener((observable, oldValue, newValue) -> sceneCamera.setTranslateZ(newValue.doubleValue()));
		}
		return slider;
	}

	/**
	 * Provides a {@link Button} which will display the World Map screen when clicked.
	 * @return Returns a {@link Button}.
	 */
	public Button getButtonShowWorldMapScreen() {
		final Button btnShowWorldMapScreen = new Button("World Map");
		btnShowWorldMapScreen.setOnAction(event -> {
			final WorldMapScreen worldMapScreen = new WorldMapScreen();
			WindowDocker.keepDocked(getWindowFromStage(theStage), getWindowFromStage(worldMapScreen), WindowDocker.DockDirection.ParentBottom);
		});
		return btnShowWorldMapScreen;
	}

	private Window getWindowFromStage(Stage theStage) {
		return theStage.getScene().getWindow();
	}

	/**
	 * Provides a {@link Button} which will add a 3D triangle to the User's 3D scene graph when clicked.
	 * @param content3DArea The User's root scene-graph node.
	 * @return Returns a {@link Button}.
	 */
	private Button getButtonAddTriangle(Group content3DArea) {
		final Button btnAddTriangle = new Button("Add Triangle");
		btnAddTriangle.setOnAction(actionEvent -> content3DArea.getChildren().add(getPyramidMeshView(150, 300, Color.RED)));
		return btnAddTriangle;
	}

	/**
	 * Provides a {@link Button} which will display the Create World screen when clicked.
	 * @return Returns a {@link Button}.
	 */
	public Button getButtonCreateWorld() {
		final Button btnCreateWorld = new Button("Create World");
		btnCreateWorld.setOnAction(event -> {
			final WorldCreationScreen worldCreationScreen = new WorldCreationScreen();
			WindowDocker.keepDocked(getWindowFromStage(theStage), getWindowFromStage(worldCreationScreen), WindowDocker.DockDirection.ParentRight);
		});
		return btnCreateWorld;
	}

	public Node getAxes() {
		Cylinder axisX = EditorUtils.createConnection(new Point3D(-CAMERA_FAR_CLIP, 0, 0), new Point3D(CAMERA_FAR_CLIP, 0, 0), 1);
		axisX.setMaterial(new PhongMaterial(Color.RED));
		Cylinder axisY = EditorUtils.createConnection(new Point3D(0, -CAMERA_FAR_CLIP, 0), new Point3D(0, CAMERA_FAR_CLIP, 0), 1);
		axisY.setMaterial(new PhongMaterial(Color.GREEN));
		Cylinder axisZ = EditorUtils.createConnection(new Point3D(0, 0, -CAMERA_FAR_CLIP), new Point3D(0, 0, CAMERA_FAR_CLIP), 1);
		axisZ.setMaterial(new PhongMaterial(Color.BLUE));
		SmartGroup smartGroup = new SmartGroup();
		smartGroup.getChildren().addAll(axisX, axisY, axisZ);
		smartGroup.setUserData(new GameObject(smartGroup, "Axis"));
		return smartGroup;
	}

	/**
	 * Provides a {@link Button} which will display the Object List screen when clicked.
	 *
	 * @param content3DArea The User's root scene-graph node.
	 * @return Returns a {@link Button}.
	 */
	public Button getButtonObjectList(Group content3DArea) {
		final Button btnObjectList = new Button("Object List");
		btnObjectList.setOnAction(event -> {
			final EditorObjectScreen editorObjectScreen = EditorObjectScreen.getInstance(content3DArea);
			WindowDocker.keepDocked(getWindowFromStage(theStage), getWindowFromStage(editorObjectScreen), WindowDocker.DockDirection.ParentLeft);
		});
		btnObjectList.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if(newValue)
				content3DArea.requestFocus();
		});
		return btnObjectList;
	}

	public List<Node> get3DObjectsForMainScene() {
		//Drawing Sphere1
		Sphere sphere1 = new Sphere(50.0);
		sphere1.setTranslateX(100);
		sphere1.setTranslateY(150);
		sphere1.setCullFace(CullFace.BACK);
		sphere1.setUserData(new GameObject(sphere1, "Sphere"));

		final MeshView pyramid = getPyramidMeshView(150, 300, Color.RED);
		return Arrays.asList(sphere1, getAxes());
	}

	private MeshView getPyramidMeshView(float tHeight, float tSideLength, Color tColor) {
		TriangleMesh pyramidMesh = new TriangleMesh();
		pyramidMesh.getTexCoords().addAll(0, 0);
		pyramidMesh.getPoints().addAll(
				0, 0, 0,                //Point 0 - Top
				0, tHeight, -tSideLength / 2,     //Point 1 - Front
				-tSideLength / 2, tHeight, 0,     //Point 2 - Left
				tSideLength / 2, tHeight, 0,      //Point 3 - Back
				0, tHeight, tSideLength / 2       //Point 4 - Right
		);

		pyramidMesh.getFaces().addAll(
				0, 0, 2, 0, 1, 0,          // Front left face
				0, 0, 1, 0, 3, 0,          // Front right face
				0, 0, 3, 0, 4, 0,          // Back right face
				0, 0, 4, 0, 2, 0,          // Back left face
				4, 0, 1, 0, 2, 0,          // Bottom rear face
				4, 0, 3, 0, 1, 0           // Bottom front face
		);
		final MeshView pyramid = new MeshView(pyramidMesh);
		final PhongMaterial phongMaterial = new PhongMaterial(tColor);
		pyramid.setDrawMode(DrawMode.FILL);
		pyramid.setMaterial(phongMaterial);
		pyramid.setCullFace(CullFace.NONE);
		pyramid.setUserData(new GameObject(pyramid, "Pyramid"));
		return pyramid;
	}
}
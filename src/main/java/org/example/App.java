package org.example;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.example.fractal.lib.IFractalEquation;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Set;

public class App extends Application {
	private static final double PAN_AMOUNT = 0.05;
	public static double X_LOWER = -2.5;
	public static double X_UPPER = 1.0555555;
	public static double Y_LOWER = -1;
	public static double Y_UPPER = 1;

	private static Stage theStage;
	private Canvas canvas = new Canvas();
	private final FractalRenderer fractalRenderer = new FractalRenderer();


	public static void main(String[] args) {
		launch();
	}

	public static void setTitleText(String format) {
		theStage.setTitle(format);
	}

	@Override
	public void start(Stage stage) {
		System.out.println("Starting up");
		theStage = stage;
		this.canvas = new Canvas(FractalRenderer.WIDTH, FractalRenderer.HEIGHT);
		fractalRenderer.setCanvas(canvas);
		final Button btnGo = new Button("GO!");
		btnGo.setOnAction(event -> {
			System.out.println("GOING!");
			fractalRenderer.renderFractal();
		});

//		final ScanResult scanResult = new ClassGraph().enableAllInfo().acceptPackages(IFractalEquation.class.getPackageName())
//				.scan();
//		Set<Class<? extends IFractalEquation>> classes = new HashSet<>();
//		for(ClassInfo classInfo : scanResult.getClassesImplementing(IFractalEquation.class.getName())) {
//			System.out.println(classInfo);
//			classes.add((Class<? extends IFractalEquation>) classInfo.loadClass());
//		}
		System.out.println("Looking for Equation Services...");
		Set<IFractalEquation> loadedClasses = new HashSet<>();
		Iterable<IFractalEquation> services = ServiceLoader.load(IFractalEquation.class);
		final Iterator<IFractalEquation> iterator = services.iterator();
		while(iterator.hasNext()) {
			final IFractalEquation next = iterator.next();
			System.out.println("Equation Service Found: " + next.getDisplayName());
			loadedClasses.add(next);
		}
		System.out.printf("Found %d Equation Services.", loadedClasses.size());


		ComboBox<IFractalEquation> cmbEquations = new ComboBox<>();
		cmbEquations.setItems(FXCollections.observableList(loadedClasses.stream().toList()));
		final Callback<ListView<IFractalEquation>, ListCell<IFractalEquation>> factory = lv -> new ListCell<IFractalEquation>() {
			@Override
			protected void updateItem(IFractalEquation item, boolean empty) {
				super.updateItem(item, empty);
				if(item == null || empty)
					return;
				setText(item.getDisplayName());
			}
		};
		cmbEquations.setCellFactory(factory);
		cmbEquations.setButtonCell(factory.call(null));
		cmbEquations.setOnAction(e -> {
			System.out.println("COMBO ON ACTION!: " + e.toString());
			final IFractalEquation value = cmbEquations.getValue();
			fractalRenderer.setEquation(value);
			fractalRenderer.renderFractal();
		});

		var scene = new Scene(new Group(canvas, new VBox(btnGo, cmbEquations)));
		stage.setScene(scene);
		stage.addEventHandler(ScrollEvent.SCROLL, getScrollEventEventHandler());
		stage.addEventHandler(KeyEvent.KEY_PRESSED, getKeyPressedEventHandler());
		stage.show();
		stage.widthProperty().addListener((observable, oldValue, newValue) -> {
			final int width = newValue.intValue();
			canvas.setWidth(width);
			fractalRenderer.setWidth(width);
		});
		stage.heightProperty().addListener((observable, oldValue, newValue) -> {
			final int height = newValue.intValue();
			canvas.setHeight(height);
			fractalRenderer.setHeight(height);
		});
	}

	private EventHandler<KeyEvent> getKeyPressedEventHandler() {
		return event -> {
			switch (event.getCode()) {
				case W -> fractalRenderer.panFractalByPercentage(0, -PAN_AMOUNT);
				case S -> fractalRenderer.panFractalByPercentage(0, PAN_AMOUNT);
				case A -> fractalRenderer.panFractalByPercentage(-PAN_AMOUNT, 0);
				case D -> fractalRenderer.panFractalByPercentage(PAN_AMOUNT, 0);
				default -> {}
			}
		};
	}

	@NotNull
	private EventHandler<ScrollEvent> getScrollEventEventHandler() {
		return event -> {
			System.out.printf("Zooming... BEFORE - X: [%f, %f], Y: [%f, %f]%n", FractalRenderer.X_LOWER, FractalRenderer.X_UPPER, FractalRenderer.Y_LOWER, FractalRenderer.Y_UPPER);
			double delta = event.getDeltaY();
			double curWidth = fractalRenderer.getWidth();
			double curHeight = fractalRenderer.getHeight();
			double x_offset, y_offset;

			if(delta > 0) {
				x_offset = (curWidth / FractalRenderer.SCROLL_ZOOM_FACTOR) / 2.0;
				y_offset = (curHeight / FractalRenderer.SCROLL_ZOOM_FACTOR) / 2.0;
			} else {
				x_offset = (curWidth * FractalRenderer.SCROLL_ZOOM_FACTOR) / 2.0;
				y_offset = (curHeight * FractalRenderer.SCROLL_ZOOM_FACTOR) / 2.0;
			}
			FractalRenderer.X_LOWER = fractalRenderer.CENTER_X - x_offset;
			FractalRenderer.X_UPPER = fractalRenderer.CENTER_X + x_offset;
			FractalRenderer.Y_LOWER = fractalRenderer.CENTER_Y - y_offset;
			FractalRenderer.Y_UPPER = fractalRenderer.CENTER_Y + y_offset;

			System.out.printf("Zooming... AFTER - X: [%f, %f], Y: [%f, %f]%n", FractalRenderer.X_LOWER, FractalRenderer.X_UPPER, FractalRenderer.Y_LOWER, FractalRenderer.Y_UPPER);
			fractalRenderer.renderFractal();
		};
	}
}
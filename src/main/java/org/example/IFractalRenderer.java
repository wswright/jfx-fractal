package org.example;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;

public interface IFractalRenderer {
	void panFractalToPoint(Point2D p);

	void panFractalByPercentage(double panX, double panY);

	void setCanvas(Canvas canvas);

	void renderFractal();

	void initializeImageData();

	void setWidth(int width);

	void setHeight(int height);
}

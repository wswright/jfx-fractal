package org.example;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import org.example.equations.IFractalEquation;

public interface IFractalRenderer {
	void setEquation(IFractalEquation equation);
	void panFractalToPoint(Point2D p);

	void panFractalByPercentage(double panX, double panY);

	void setCanvas(Canvas canvas);

	void renderFractal();

	void initializeImageData();

	void setWidth(int width);

	void setHeight(int height);

	double getWidth();

	double getHeight();
}

package org.example;

import javafx.scene.canvas.Canvas;

public interface IFractalRenderer {
	void panFractalByPercentage(double panX, double panY);

	void setCanvas(Canvas canvas);

	void renderFractal();

	void initializeImageData();

	void setWidth(int width);

	void setHeight(int height);
}

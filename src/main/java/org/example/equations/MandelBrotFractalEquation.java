package org.example.equations;

import org.example.ComplexAlgebraicForm;
import org.jetbrains.annotations.NotNull;

public class MandelBrotFractalEquation implements IFractalEquation {
	double X_LOWER = -2.5;
	double X_UPPER = 1.0555555;
	double Y_LOWER = -1;
	double Y_UPPER = 1;

	@Override
	public @NotNull ComplexAlgebraicForm calculateFractalIteration(ComplexAlgebraicForm z, ComplexAlgebraicForm c) {
		return z.times(z).plus(c);
	}

	@Override
	public String getDisplayName() {
		return "Mandelbrot";
	}
}

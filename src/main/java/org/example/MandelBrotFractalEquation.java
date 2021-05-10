package org.example;

import org.jetbrains.annotations.NotNull;

public class MandelBrotFractalEquation implements IFractalEquation {
	public static double X_LOWER = -2.5;
	public static double X_UPPER = 1.0555555;
	public static double Y_LOWER = -1;
	public static double Y_UPPER = 1;
	@Override
	public @NotNull Complex calculateFractalIteration(Complex z, Complex c) {
		return z.times(z).plus(c);
	}
}

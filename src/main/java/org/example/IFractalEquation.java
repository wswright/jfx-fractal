package org.example;

import org.jetbrains.annotations.NotNull;

public interface IFractalEquation {
	double X_LOWER = -2.5;
	double X_UPPER = 1.0555555;
	double Y_LOWER = -1;
	double Y_UPPER = 1;
	/**
	 * Calculate the number of iterations a point takes to leave a bound.
	 * @param z
	 * @param c
	 * @return
	 */
	@NotNull
	Complex calculateFractalIteration(Complex z, Complex c);
}

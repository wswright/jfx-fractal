package org.example;

import org.jetbrains.annotations.NotNull;

public interface IFractalEquation {
	/**
	 * Calculate the number of iterations a point takes to leave a bound.
	 * @param z
	 * @param c
	 * @return
	 */
	@NotNull
	Complex calculateFractalIteration(Complex z, Complex c);
}

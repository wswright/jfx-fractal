package org.example;

import org.jetbrains.annotations.NotNull;

public class MandelBrotFractalEquation implements IFractalEquation {
	@Override
	public @NotNull Complex calculateFractalIteration(Complex z, Complex c) {
		return z.times(z).plus(c).minus(z);
	}
}

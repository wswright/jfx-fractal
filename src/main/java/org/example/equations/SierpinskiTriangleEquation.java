package org.example.equations;

import org.example.Complex;
import org.jetbrains.annotations.NotNull;

public class SierpinskiTriangleEquation implements IFractalEquation {
	@Override
	public @NotNull Complex calculateFractalIteration(Complex z, Complex c) {
			return z.times(z).times(z).minus(z.times(z)).plus(c);
	}
}

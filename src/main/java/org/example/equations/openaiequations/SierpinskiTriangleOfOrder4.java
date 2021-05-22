package org.example.equations.openaiequations;

import org.example.Complex;
import org.example.equations.IFractalEquation;

public class SierpinskiTriangleOfOrder4 implements IFractalEquation {

	public Complex calculateFractalIteration(Complex z, Complex c) {
		return z.times(z).plus(z).times(z).minus(z).plus(c);
	}
}
package org.example;

import org.jetbrains.annotations.NotNull;

public interface IFractalEquation {
	@NotNull
	Complex calculateFractalIteration(Complex z, Complex c);
}

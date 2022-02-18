package org.example.equations;

import org.example.ComplexAlgebraicForm;
import org.jetbrains.annotations.NotNull;

public class ZSquaredMinusC implements IFractalEquation{
	@Override
	public @NotNull ComplexAlgebraicForm calculateFractalIteration(ComplexAlgebraicForm z, ComplexAlgebraicForm c) {
		return z.times(z).minus(c);
	}

	@Override
	public String getDisplayName() {
		return "Z^2 - C";
	}
}

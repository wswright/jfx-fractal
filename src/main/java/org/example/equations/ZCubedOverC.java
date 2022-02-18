package org.example.equations;

import org.example.ComplexAlgebraicForm;
import org.jetbrains.annotations.NotNull;

public class ZCubedOverC implements IFractalEquation {
	@Override
	public @NotNull ComplexAlgebraicForm calculateFractalIteration(ComplexAlgebraicForm z, ComplexAlgebraicForm c) {
		return z.times(z).times(z).divides(c);
	}

	@Override
	public String getDisplayName() {
		return "Z^3 / C";
	}
}

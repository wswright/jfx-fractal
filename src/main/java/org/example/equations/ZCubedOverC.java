package org.example.equations;

import org.example.MyComplexClass;
import org.jetbrains.annotations.NotNull;

public class ZCubedOverC implements IFractalEquation {
	@Override
	public @NotNull MyComplexClass calculateFractalIteration(MyComplexClass z, MyComplexClass c) {
		return z.times(z).times(z).divides(c);
	}

	@Override
	public String getDisplayName() {
		return "Z^3 / C";
	}
}

package org.example.equations;

import org.example.MyComplexClass;
import org.jetbrains.annotations.NotNull;

public interface IFractalEquation {
	/**
	 * Calculate the number of iterations a point takes to leave a bound.
	 * @param z
	 * @param c
	 * @return
	 */
	@NotNull
	MyComplexClass calculateFractalIteration(MyComplexClass z, MyComplexClass c);
	String getDisplayName();
}

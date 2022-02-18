package org.example;

public record ComplexTrigForm(double p, double theta){
	public static ComplexTrigForm fromAlgebraicForm(ComplexAlgebraicForm complex) {
		double _p = complex.abs();
		double _theta = Math.atan(complex.im() / complex.re());

//		System.out.println(String.format("sin(theta): %f", Math.sin(_theta)));
//		System.out.println(String.format("b / p: %f", complex.im() / _p));
//		if(Math.sin(_theta) == complex.im() / _p)
//			System.out.println("sin(θ)=b/ρ");
//		else
//			System.out.println("NOT sin(θ)!");
//
//		if(Math.cos(_theta) == complex.re() / _p)
//			System.out.println("cos(θ)=a/ρ");
//		else
//			System.out.println("NOT cos(θ)!");

		final int loopMax = 100;
		if(Math.sin(_theta) == complex.im() / _p && Math.cos(_theta) == complex.re() / _p) {
			return new ComplexTrigForm(_p, _theta);
		}
		else {
			_theta += Math.PI;
			if(Math.sin(_theta) == complex.im() / _p && Math.cos(_theta) == complex.re() / _p) {
				return new ComplexTrigForm(_p, _theta);
			}

			int loops = 0;
			while(Math.sin(_theta) != complex.im() / _p && Math.cos(_theta) != complex.re() / _p && loops++ < loopMax) {
				_theta -= Math.PI;
			}
			if(loops >= loopMax)
				System.out.printf(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>: %d%n", loops);
			else System.out.printf("LOOPS: %d%n", loops);
			return new ComplexTrigForm(_p, _theta);

//			throw new RuntimeException("COULD NOT FIND VALUE FOR THETA! SEE: https://www.quora.com/How-do-you-define-the-log-of-a-complex-number");
		}
//		return new ComplexTrigForm(0,0);//just plain wrong
	}
}

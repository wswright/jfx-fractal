package org.example;

import java.util.Objects;

public record ComplexAlgebraicForm(double re, double im) {
	public String toString() {
		if (im == 0) return re + "";
		if (re == 0) return im + "i";
		if (im <  0) return re + " - " + (-im) + "i";
		return re + " + " + im + "i";
	}

	// return abs/modulus/magnitude
	public double abs() {
		return Math.hypot(re, im);
	}

	// return angle/phase/argument, normalized to be between -pi and pi
	public double phase() {
		return Math.atan2(im, re);
	}

	// return a new Complex object whose value is (this + b)
	public ComplexAlgebraicForm plus(ComplexAlgebraicForm b) {
		return new ComplexAlgebraicForm(this.re + b.re, this.im + b.im);
	}

	// return a new Complex object whose value is (this - b)
	public ComplexAlgebraicForm minus(ComplexAlgebraicForm b) {
		return new ComplexAlgebraicForm(this.re - b.re, this.im - b.im);
	}

	// return a new Complex object whose value is (this * b)
	public ComplexAlgebraicForm times(ComplexAlgebraicForm b) {
		return new ComplexAlgebraicForm(this.re * b.re - this.im * b.im, this.re * b.im + this.im * b.re);
	}

	// return a new object whose value is (this * alpha)
	public ComplexAlgebraicForm scale(double alpha) {
		return new ComplexAlgebraicForm(alpha * re, alpha * im);
	}

	// return a new Complex object whose value is the conjugate of this
	public ComplexAlgebraicForm conjugate() {
		return new ComplexAlgebraicForm(re, -im);
	}

	// return a new Complex object whose value is the reciprocal of this
	public ComplexAlgebraicForm reciprocal() {
		return new ComplexAlgebraicForm(re / (re*re + im*im), -im / (re*re + im*im));
	}

	// return the real or imaginary part
	public double re() { return re; }
	public double im() { return im; }

	// return a / b
	public ComplexAlgebraicForm divides(ComplexAlgebraicForm b) {
		return this.times(b.reciprocal());
	}

	// return a new Complex object whose value is the complex exponential of this
	public ComplexAlgebraicForm exp() {
		return new ComplexAlgebraicForm(Math.exp(re) * Math.cos(im), Math.exp(re) * Math.sin(im));
	}

	// return a new Complex object whose value is the complex sine of this
	public ComplexAlgebraicForm sin() {
		return new ComplexAlgebraicForm(Math.sin(re) * Math.cosh(im), Math.cos(re) * Math.sinh(im));
	}

	// return a new Complex object whose value is the complex cosine of this
	public ComplexAlgebraicForm cos() {
		return new ComplexAlgebraicForm(Math.cos(re) * Math.cosh(im), -Math.sin(re) * Math.sinh(im));
	}

	// return a new Complex object whose value is the complex tangent of this
	public ComplexAlgebraicForm tan() {
		return sin().divides(cos());
	}

//	public MyComplexClass pow(double exponent) {
//		//DeMoivre's Theorem
//		//z^n = (r ^ n) * (e^(i*n*theta))
//		//r = mag(z), sqrt(x^2 + y^2)
//		//theta = arctan(y/x)
//		double r = this.abs();
//		double theta = Math.atan(this.im / this.re);
//		return Math.pow(r, exponent) * (Math.cos(exponent * theta) +  Math.asin(exponent * theta));
//
//	}



	// a static version of plus
	public static ComplexAlgebraicForm plus(ComplexAlgebraicForm a, ComplexAlgebraicForm b) {
		return new ComplexAlgebraicForm(a.re + b.re, a.im + b.im);
	}

	// See Section 3.3.
	public boolean equals(Object x) {
		if (x == null) return false;
		if (this.getClass() != x.getClass()) return false;
		ComplexAlgebraicForm that = (ComplexAlgebraicForm) x;
		return (this.re == that.re) && (this.im == that.im);
	}

	// See Section 3.3.
	public int hashCode() {
		return Objects.hash(re, im);
	}

	// sample client for testing
	public static void main(String[] args) {
		ComplexAlgebraicForm a = new ComplexAlgebraicForm(5.0, 6.0);
		ComplexAlgebraicForm b = new ComplexAlgebraicForm(-3.0, 4.0);

		System.out.println("a            = " + a);
		System.out.println("b            = " + b);
		System.out.println("Re(a)        = " + a.re());
		System.out.println("Im(a)        = " + a.im());
		System.out.println("b + a        = " + b.plus(a));
		System.out.println("a - b        = " + a.minus(b));
		System.out.println("a * b        = " + a.times(b));
		System.out.println("b * a        = " + b.times(a));
		System.out.println("a / b        = " + a.divides(b));
		System.out.println("(a / b) * b  = " + a.divides(b).times(b));
		System.out.println("conj(a)      = " + a.conjugate());
		System.out.println("|a|          = " + a.abs());
		System.out.println("tan(a)       = " + a.tan());
	}

}
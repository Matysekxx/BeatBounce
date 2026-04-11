package cz.matysekxx.beatbounce.util;

public class Complex {
    private double real;
    private double imaginary;

    public Complex(double real, double imaginary) {
        this.real = real;
        this.imaginary = imaginary;
    }

    public static Complex of(double real, double imaginary) {
        return new Complex(real, imaginary);
    }

    public Complex addAndGet(Complex other) {
        this.real += other.real;
        this.imaginary += other.imaginary;
        return this;
    }

    public Complex subtractAndGet(Complex other) {
        this.real -= other.real;
        this.imaginary -= other.imaginary;
        return this;
    }

    public Complex multiplyAndGet(Complex other) {
        final double oldReal = this.real;
        this.real = oldReal * other.real - this.imaginary * other.imaginary;
        this.imaginary = oldReal * other.imaginary + this.imaginary * other.real;
        return this;
    }

    public double getReal() {
        return real;
    }

    public double getImaginary() {
        return imaginary;
    }

    @Override
    public String toString() {
        if (imaginary == 0) return real + "";

        if (real == 0) return imaginary + "i";

        if (imaginary < 0) return real + " - " + -imaginary + "i";

        return real + " + " + imaginary + "i";
    }
}

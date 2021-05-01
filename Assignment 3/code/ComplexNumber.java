import java.util.ArrayList;

public class ComplexNumber
{
    double Pi = Math.PI;
    /**
     * The real, Re(z), part of the <code>ComplexNumber</code>.
     */
    private double real;
    /**
     * The imaginary, Im(z), part of the <code>ComplexNumber</code>.
     */
    private double imaginary;

    /**
     * Constructs a new <code>ComplexNumber</code> object with both real and imaginary parts 0 (z = 0 + 0i).
     */
    public ComplexNumber()
    {
        real = 0.0;
        imaginary = 0.0;
    }

    /**
     * Constructs a new <code>ComplexNumber</code> object.
     * @param real the real part, Re(z), of the complex number
     * @param imaginary the imaginary part, Im(z), of the complex number
     */
    public ComplexNumber(double real, double imaginary)
    {
        this.real = real;
        this.imaginary = imaginary;
    }

    /**
     * Sets the value of current complex number to the passed complex number.
     * @param z the complex number
     */
    public void set(ComplexNumber z)
    {
        this.real = z.real;
        this.imaginary = z.imaginary;
    }

    /**
     * The real part of <code>ComplexNumber</code>
     * @return the real part of the complex number
     */
    public double getRe()
    {
        return this.real;
    }

    /**
     * The imaginary part of <code>ComplexNumber</code>
     * @return the imaginary part of the complex number
     */
    public double getIm()
    {
        return this.imaginary;
    }

    /**
     * The modulus, magnitude or the absolute value of current complex number.
     * @return the magnitude or modulus of current complex number
     */
    public double mod()
    {
        return Math.sqrt(Math.pow(this.real,2) + Math.pow(this.imaginary,2));
    }

    /**
     * @return the complex number in x + yi format
     */
    @Override
    public String toString()
    {
        String re = this.real+"";
        String im = "";
        if(this.imaginary < 0)
            im = this.imaginary+"i";
        else
            im = "+"+this.imaginary+"i";
        return re+im;
    }

    /**
     * Checks if the passed <code>ComplexNumber</code> is equal to the current.
     * @param z the complex number to be checked
     * @return true if they are equal, false otherwise
     */
    @Override
    public final boolean equals(Object z)
    {
        if (!(z instanceof ComplexNumber))
            return false;
        ComplexNumber a = (ComplexNumber) z;
        return (real == a.real) && (imaginary == a.imaginary);
    }
    public final ComplexNumber addComplex(ComplexNumber a, ComplexNumber b)
    {
        ComplexNumber sum = new ComplexNumber();
        sum.real = a.real + b.real;
        sum.imaginary = a.imaginary + b.imaginary;
        set(sum);
        return sum;
    }

    public final ComplexNumber subtractComplex(ComplexNumber a, ComplexNumber b)
    {
        ComplexNumber diff = new ComplexNumber();
        diff.real = a.real - b.real;
        diff.imaginary = a.imaginary - b.imaginary;
        set(diff);
        return diff;
    }

    public final ComplexNumber multiplyComplex(ComplexNumber a, ComplexNumber b)
    {
        ComplexNumber product = new ComplexNumber();
        product.real = a.real*b.real - (a.imaginary*b.imaginary);
        product.imaginary = a.imaginary*b.real + b.imaginary*a.real;
//        set(product);
//        System.out.println("product " + product.toString());
        return product;
    }

    public final ComplexNumber lnComplex(int n, int k, int N, boolean isInverse)
    {
        double power = -n*k*2*Pi/N;
        if (isInverse){
            power = -power;
        }
        ComplexNumber ln = new ComplexNumber();
        ln.real = Math.cos(power);
        ln.imaginary = Math.sin(power);
        return ln;
    }

    public final ArrayList<ComplexNumber> doTransform (ArrayList<ComplexNumber> x, boolean isInverse)
    {
        ArrayList<ComplexNumber> X = new ArrayList<>();
        ComplexNumber ln;
        ComplexNumber product;
        int N = x.size();
        for(int k = 0; k < N; k++){
            ComplexNumber cn = new ComplexNumber(0, 0);
            for(int n = 0; n < N; n++){
                ln = lnComplex(n, k, N, isInverse); // e to the i k b pi/2
                product = multiplyComplex(x.get(n), ln);
                cn = addComplex(cn, product);
            }
            if(isInverse){
                cn.real = cn.real/(double)N;
                cn.imaginary = cn.imaginary/(double)N;
            }
            X.add(k,cn);
//            System.out.println("Spectrum " + X.toString());
        }
        return X;
    }
    public final boolean isPowerOfTwo(int num)
    {
        return num > 0 && (num & (num - 1)) == 0;
    }
    public final int maxPowerof2(int S)
    {
        double maxPowD = Math.log10(S)/Math.log10(2);
        int maxPow = (int)Math.floor(maxPowD);
        System.out.println("Size: " + S + " Num: " + (int) Math.pow(2, maxPow) + " maxPow: " + maxPow);
        return (int) Math.pow(2, maxPow);

//        double percent = N*100/S;
//        System.out.println("Size: " + S + " Num: " + N + " maxPowD: " + maxPowD + " maxPow: " + maxPow  + " percentage: " + percent);
    };

    // Fill in the operations between complex numbers used for DFT/IDFT/FFT/IFFT.
}
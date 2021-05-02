import java.util.ArrayList;

public class Fourier {
    public ComplexNumber complex= new ComplexNumber();

    public final ArrayList<ComplexNumber> doTransform (ArrayList<ComplexNumber> x, boolean isInverse)
    {
        ArrayList<ComplexNumber> X = new ArrayList<>();
        ComplexNumber ln;
        ComplexNumber product;
        int N = x.size();
        for(int k = 0; k < N; k++){
            ComplexNumber cn = new ComplexNumber(0, 0);
            for(int n = 0; n < N; n++){
                ln = complex.lnComplex(n, k, N, isInverse); // e to the i k b pi/2
                product = complex.multiplyComplex(x.get(n), ln);
                cn = complex.addComplex(cn, product);
            }
            if(isInverse){
                cn = new ComplexNumber(cn.getRe()/(double)N, cn.getIm()/(double)N);
            }
            X.add(k,cn);
//            System.out.println("Spectrum " + X.toString());
        }
        return X;
    }

    public ComplexNumber[] FFT(ComplexNumber[] x, boolean isInverse) {
        int N = x.length;
        ComplexNumber[] X = new ComplexNumber[N]; // frequency output
        if(N == 1){
            return x;
        }
//        recursively separate the wave form into even and odd time stamps
        ComplexNumber[] xeven = new ComplexNumber[N/2];
        ComplexNumber[] xodd = new ComplexNumber[N/2];
        for (int n = 0; n < N/2; n ++){
            xeven[n] = (x[2*n]);
            xodd[n] = x[2*n+1];
        }
// new arrays of even and odd spectrum elements
        ComplexNumber[] Xeven = FFT(xeven, isInverse);
        ComplexNumber[] Xodd = FFT(xodd, isInverse);

//          calculate W(k,N) = e to the -i*2pi*k/N
        ComplexNumber[] W = new ComplexNumber[N];

//        e to the 0 = 1, so cut out any zero n
        ComplexNumber one = new ComplexNumber(1.0,0.0);
        for (int k = 0; k < N/2; k++){
            if(k != 0) {
//          calculate W(k,N) = e to the i*2pi*k/N (positive for isInverse = false,  negative for isInverse = true)
                W[k] = complex.lnComplex(1, k, N, isInverse);
            }
            else W[k] = one;
            W[(k + N/2)] = complex.lnComplex(1, k+N/2, N, isInverse);

            X[k] = complex.addComplex(Xeven[k], complex.multiplyComplex(Xodd[k], W[k]));
            X[k + N/2] = complex.addComplex(Xeven[k], complex.multiplyComplex(Xodd[k], W[(k+N/2)]));
        }
        return X;
    }
    //    I used this to throw exception on the initial test cases - a very cool one liner from StackOverflow using a bitwise operator
    public final boolean isPowerOfTwo(int num)
    {
        return num > 0 && (num & (num - 1)) == 0;
    }

    // this finds the maximum power of 2 to truncate S, the input, buy finding 2 to the power of S and cutting off the integer part
    public final int maxPowerof2(int S)
    {
        double maxPowD = Math.log10(S)/Math.log10(2);
        int maxPow = (int)Math.floor(maxPowD);

// I found that for some samples, nearly half is cut off
// that is, adding a very short sample (eg a repeat from the start of the waveform) would return a more accurate transform than truncating
        int N = (int) Math.pow(2, maxPow);
        double percent = N*100/S;
        System.out.println("Start size: " + S + " End size: " + N + " maxPower: " + maxPowD + " maxPow int: " + maxPow  + " percentage: " + percent);
        return (int) Math.pow(2, maxPow);
    }

}

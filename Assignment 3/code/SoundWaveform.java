

// DO NOT DISTRIBUTE THIS FILE TO STUDENTS
import ecs100.UI;
import ecs100.UIMouseListener;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.time.Duration;
import java.time.Instant;
import java.awt.event.*;

/*
  getAudioInputStream
  -> getframelength,
  -> read into byteArray of 2x that many bytes
  -> convert to array of doubles in reversed pairs of bytes (signed)
  -> scale #FFFF to +/- 300

  array of doubles
   -> unscale  +/- 300  to #FFFF (
   -> convert to array of bytes (pairs little endian, signed)
   -> convert to inputStream
   -> convert to AudioInputStream
   -> write to file.
 */

public class SoundWaveform {

    public static final double MAX_VALUE = 300;
    public static final int SAMPLE_RATE = 44100;
    public static final int MAX_SAMPLES = SAMPLE_RATE / 100;   // samples in 1/100 sec

    public static final int GRAPH_LEFT = 10;
    public static final int ZERO_LINE = 310;
    public static final int X_STEP = 2;            //pixels between samples
    public static final int GRAPH_WIDTH = MAX_SAMPLES * X_STEP;

    private ArrayList<Double> waveform = new ArrayList<Double>();   // the displayed waveform
    private ArrayList<ComplexNumber> spectrum = new ArrayList<ComplexNumber>(); // the spectrum: length/mod of each X(k)
    public ComplexNumber complex = new ComplexNumber();
    public Fourier fourier = new Fourier();
    public ArrayList<ComplexNumber> newSpectrum = new ArrayList<ComplexNumber>();
    ;
    private ArrayList<Double> ifftWaveform = new ArrayList<Double>();   // the displayed waveform

    /**
     * Displays the waveform.
     */
    public void displayWaveform() {
        if (this.waveform == null) { //there is no data to display
            UI.println("No waveform to display");
            return;
        }
        UI.clearText();
        UI.println("Printing, please wait...");

        UI.clearGraphics();

        // draw x axis (showing where the value 0 will be)
        UI.setColor(Color.black);
        UI.drawLine(GRAPH_LEFT, ZERO_LINE, GRAPH_LEFT + GRAPH_WIDTH, ZERO_LINE);

        // plot points: blue line between each pair of values
        UI.setColor(Color.blue);

        double x = GRAPH_LEFT;
        for (int i = 1; i < this.waveform.size(); i++) {
            double y1 = ZERO_LINE - this.waveform.get(i - 1);
            double y2 = ZERO_LINE - this.waveform.get(i);
            if (i > MAX_SAMPLES) {
                UI.setColor(Color.red);
            }
            UI.drawLine(x, y1, x + X_STEP, y2);
            x = x + X_STEP;
        }

        UI.println("Printing completed!");
    }

    /**
     * Displays the spectrum. Scale to the range of +/- 300.
     */
    public void displaySpectrum() {
        if (this.spectrum == null) { //there is no data to display
            UI.println("No spectrum to display");
            return;
        }
        UI.clearText();
        UI.println("Printing, please wait...");
        UI.clearGraphics();

        // calculate the mode of each element
        ArrayList<Double> spectrumMod = new ArrayList<Double>();
        double max = 0;
        for (int i = 0; i < spectrum.size(); i++) {
            if (i == MAX_SAMPLES)
                break;

            double value = spectrum.get(i).mod();
            max = Math.max(max, value);
            spectrumMod.add(spectrum.get(i).mod());
        }

        double scaling = 300 / max;
        for (int i = 0; i < spectrumMod.size(); i++) {
            spectrumMod.set(i, spectrumMod.get(i) * scaling);
        }

        // draw x axis (showing where the value 0 will be)
        UI.setColor(Color.black);
        UI.drawLine(GRAPH_LEFT, ZERO_LINE, GRAPH_LEFT + GRAPH_WIDTH, ZERO_LINE);

        // plot points: blue line between each pair of values
        UI.setColor(Color.blue);

        double x = GRAPH_LEFT;
        for (int i = 1; i < spectrumMod.size(); i++) {
            double y1 = ZERO_LINE;
            double y2 = ZERO_LINE - spectrumMod.get(i);
            if (i > MAX_SAMPLES) {
                UI.setColor(Color.red);
            }
            UI.drawLine(x, y1, x + X_STEP, y2);
            x = x + X_STEP;
        }

        UI.println("Printing completed!");
    }
    public void doMouse(String s, double v, double v1){
        if (this.spectrum == null) { //there is no data to display
            UI.println("No spectrum  - create a spectrum to alter");
            return;
        }
        if(s.equalsIgnoreCase("clicked")){
            // give the pixel height from top of the mouse click
            UI.println("Clicked! x v: " + v + "y v1: " + v1);
            // calculate the mode of each element

            ArrayList<Double> spectrumMod = new ArrayList<Double>();
            double max = 0;
            for (int i = 0; i < spectrum.size(); i++) {
                if (i == MAX_SAMPLES)
                    break;
                double value = spectrum.get(i).mod();
                max = Math.max(max, value);
                spectrumMod.add(spectrum.get(i).mod());
            }
//            min v = 10 max v = 1585
//            min v1 = 0 = top of screen, max v1 = 1050
//            index has to fit in spectrumMod

//            Calculate x index of spectrumMod and make sure its within the array bounds
            double xRatio = (v-GRAPH_LEFT)/(double)X_STEP; // where the zero index is at GRAPH LEFT 10, and there are 2 pixel steps per index
            int xIndex = (int) Math.floor(xRatio);
            if(xIndex < 0) xIndex = 0;
            else if(xIndex >= spectrumMod.size()) xIndex = spectrumMod.size()-1;
            UI.println("xIndex: " + xIndex + " of 441");
            double scaling = 300 / max; // could i pass max from above? public? pix/ampl
            double diff_y = ZERO_LINE - v1; //pix
            if (diff_y<0) diff_y = 0; // spectrum has to be non-negative
            else if (diff_y > 300) diff_y = 300; // don't allow it to scale over the maximum

            UI.println("scaling: " +  scaling + " max " + (int)max + " diff_y: " + diff_y);
            for (int i = 0; i < spectrumMod.size(); i++) {
                spectrumMod.set(i, spectrumMod.get(i) * scaling);
            }
//           when diffy = 0 scaleY = 0; when diffy = 300 result = 1; when diffy = 150 result = 1/2
            double scaleY = diff_y/300;
            double oldAmpl = spectrumMod.get(xIndex);
            double newAmpl = spectrumMod.get(xIndex)*diff_y/300;
            UI.println(" diff_y: " + diff_y + " scaleY:" + scaleY + " oldAmpl:" + oldAmpl + " max:" + max);

            spectrumMod.set(xIndex, max*scaling*diff_y/300);
            // draw x axis (showing where the value 0 will be)
            UI.clearGraphics();
            UI.setColor(Color.black);
            UI.drawLine(GRAPH_LEFT, ZERO_LINE, GRAPH_LEFT + GRAPH_WIDTH, ZERO_LINE);

            // plot points: blue line between each pair of values
            UI.setColor(Color.pink);

            double x = GRAPH_LEFT;
            for (int i = 1; i < spectrumMod.size(); i++) {
                double y1 = ZERO_LINE;
                double y2 = ZERO_LINE - spectrumMod.get(i);
                if (i > MAX_SAMPLES) {
                    UI.setColor(Color.red);
                }
                UI.drawLine(x, y1, x + X_STEP, y2);
                x = x + X_STEP;
            }

            UI.println("Printing completed!");
        }

        }

    public void dft() {
        UI.clearText();
        UI.println("DFT in process, please wait...");
        Instant start = Instant.now();
//      Test cases
//        ArrayList<Double> case1 = new ArrayList(Arrays.asList(1.0 ,2.0 ,3.0 ,4.0 ,5.0, 6.0, 7.0, 8.0));
//        ArrayList<Double> case2 = new ArrayList(Arrays.asList(1.0 ,2.0 ,1.0 ,2.0 ,1.0 ,2.0 ,1.0 ,2.0));
//        ArrayList<Double> case3 = new ArrayList(Arrays.asList(1.0 ,2.0 ,3.0 ,4.0 , 4.0 ,3.0, 2.0, 1.0));
//        waveform = case1;

//         waveform is an array of doubles, size N
//         Convert the waveform from a double list to a complex number list to prepare for DFT/FFT

        ArrayList<ComplexNumber> x = new ArrayList<ComplexNumber>();
        int N = waveform.size();
        System.out.println("Waveform size " + waveform.size());
        for (int i = 0; i < N; i++) {
            x.add(new ComplexNumber(waveform.get(i), 0.0));
        }
        boolean isInverse = false;
        spectrum = fourier.doTransform(x, isInverse);
        newSpectrum = spectrum;
//      Duration code from StackOverflow https://stackoverflow.com/questions/4927856/how-to-calculate-time-difference-in-java/54428410
        Instant end = Instant.now();
        Duration timeElapsed = Duration.between(start, end);
        double deltaT = (double) timeElapsed.toMillis() / 60000;
        System.out.println("Time taken: " + deltaT + " minutes");
        UI.println("DFT completed!");
        waveform.clear();
    }

    public void idft() {
        UI.clearText();
        UI.println("IDFT in process, please wait...");
        Instant start = Instant.now();
        ArrayList<ComplexNumber> X = new ArrayList<ComplexNumber>();

//        Test cases
//        ArrayList<ComplexNumber> case1sp = new ArrayList<ComplexNumber>();
//          convert the example spectra to complex number ArrayLists
//        ArrayList<Double> specReal = new ArrayList(Arrays.asList(36.0 ,-4.00 ,-4.00,-4.00,-4.00, -4.00, -4.00, -4.00));
//        ArrayList<Double> specImag = new ArrayList(Arrays.asList(0.0 ,9.65685424949238 , 4.00,1.6568542494923788,0.00, -1.656854249492381, -4.00, -9.65685424949238));
//        ArrayList<Double> specReal = new ArrayList(Arrays.asList(12.0 ,0.00 ,0.00 ,0.00 ,-4.00, 0.00 , 0.00 , 0.00));
//        ArrayList<Double> specImag = new ArrayList(Arrays.asList(0.0 ,0.0 , 0.0 ,0.0 ,0.00, 0.0 , 0.0 , 0.0));
//        ArrayList<Double> specReal = new ArrayList(Arrays.asList(20.0 ,-5.82842712474619 ,0.00 ,-0.1715728752538097 ,0.00, -0.1715728752538097 , 0.00 , -5.828427124746191));
//        ArrayList<Double> specImag = new ArrayList(Arrays.asList(0.0 ,-2.414213562373096, 0.0 ,-0.4142135623730945 ,0.00, 0.41421356237309537, 0.0 , 2.4142135623730936));
//
//        for (int i = 0; i < specReal.size(); i++) {
//            ComplexNumber cn = new ComplexNumber(specReal.get(i), specImag.get(i));
//            case1sp.add(cn);
//        }

        spectrum = newSpectrum;
        boolean isInverse = true;
        int N = spectrum.size();
        System.out.println("Spectrum size N: " + N);
        waveform.clear();
        X = fourier.doTransform(spectrum, isInverse);
//         convert the complex number list to a double list to restore th waveform
//        Since the waveform is real, the real portion of the complex number list is the waveform
        for (int n = 0; n < N; n++) {
            waveform.add(n, X.get(n).getRe());
        }

        Instant end = Instant.now();
        Duration timeElapsed = Duration.between(start, end);
        double deltaT = (double) timeElapsed.toMillis() / 60000;
        System.out.println("Time taken: " + deltaT + " minutes");
        UI.println("IDFT completed!");
//        System.out.println("Inverted waveform " + waveform);
//      Waveform displays the new waveform when the GUI button is pressed
        ArrayList<Double> waveformOut = waveform;
        spectrum.clear();
    }

    public void fft() {
//      Test cases
//        ArrayList<Double> case1 = new ArrayList(Arrays.asList(1.0 ,2.0 ,3.0 ,4.0 ,5.0, 6.0, 7.0, 8.0));
//        ArrayList<Double> case2 = new ArrayList(Arrays.asList(1.0 ,2.0 ,1.0 ,2.0 ,1.0 ,2.0 ,1.0 ,2.0));
//        ArrayList<Double> case3= new ArrayList(Arrays.asList(1.0 ,2.0 ,3.0 ,4.0 ,4.0 ,3.0 ,2.0 ,1.0));
//        ArrayList<Double> case4 = new ArrayList(Arrays.asList(1.0 ,2.0 ,3.0 ,4.0 ));
//        waveform = case4;

        UI.clearText();
        UI.println("FFT in process, please wait...");
        Instant start = Instant.now();
        int S = waveform.size();

//      Trim the waveform to a power of 2 and call it x
        if (S > 0) {
            int N = fourier.maxPowerof2(S);
//          shorten time signal waveform to a power of 2
            ComplexNumber[] xc = new ComplexNumber[N];
            for (int i = 0; i < N; i++) {
//              Convert to an array of complex numbers, to enable same method for FFT and IFFT
                xc[i] = new ComplexNumber(waveform.get(i), 0.0);
            }
//          now do the recursive FFT
            boolean isInverse = false;
            ComplexNumber[] X = fourier.FFT(xc, isInverse);  // X = frequency output from the time signal input
            spectrum.addAll(Arrays.asList(X));
            newSpectrum = spectrum;
        } else {
            System.err.println("waveform is empty: " + S);
        }
//      Timer
        Instant end = Instant.now();
        Duration timeElapsed = Duration.between(start, end);
        double deltaT = (double) timeElapsed.toMillis() / 1000;
        System.out.println("Time taken: " + deltaT + " seconds");
        UI.println("FFT completed!");
        waveform.clear();
    }

    public void ifft() {
        UI.clearText();
        UI.println("IFFT in process, please wait...");
        Instant start = Instant.now();
        spectrum = newSpectrum;
        int S = spectrum.size();
//        check spectrum is not empty and is a  power of 2 (should not need this but worth a check)
        if (S <= 0 || fourier.isPowerOfTwo(S)) {
//        change spectrum list to an array X of complex numbers so I can use the same method
            int N = S;
            ComplexNumber[] X = new ComplexNumber[N]; // spectrum shortened to a power of 2
            for (int i = 0; i < N; i++) {
                X[i] = spectrum.get(i);
            }
//          now do the recursive FFT, but with exponent +ve not -ve via isInverse
            boolean isInverse = true;
            ComplexNumber[] xComp = fourier.FFT(X, isInverse);
            waveform.clear();
            for (int n = 0; n < N; n++) {
                double z = xComp[n].getRe() / N; // z = the time signal output (created from the frequency spectrum input)
                waveform.add(z);
            }
            ifftWaveform = waveform;
        } else {
            System.err.println("spectrum is empty or not power of 2 - size: " + S);
        }

        //      Timer
        Instant end = Instant.now();
        Duration timeElapsed = Duration.between(start, end);
        double deltaT = (double) timeElapsed.toMillis() / 1000;
        System.out.println("Time taken: " + deltaT + " seconds");
        UI.println("IFFT completed!");
        spectrum.clear();
    }

    /**
     * Save the wave form to a WAV file
     */
    public void doSave() {
        WaveformLoader.doSave(waveform, WaveformLoader.scalingForSavingFile);
    }

    public void doSaveS() {
        WaveformLoader.doSaveS(spectrum);
    }

    /**
     * Load the WAV file.
     */
    public void doLoad() {
        UI.clearText();
        UI.println("Loading...");

        waveform = WaveformLoader.doLoad();

        this.displayWaveform();

        UI.println("Loading completed!");
    }

    public static void main(String[] args) {
        SoundWaveform wfm = new SoundWaveform();
        //core
        UI.addButton("Display Waveform", wfm::displayWaveform);
        UI.addButton("Display Spectrum", wfm::displaySpectrum);
        UI.addButton("DFT", wfm::dft);
        UI.addButton("IDFT", wfm::idft);
        UI.addButton("FFT", wfm::fft);
        UI.addButton("IFFT", wfm::ifft);
        UI.addButton("Save wav", wfm::doSave);
        UI.addButton("Save txt", wfm::doSaveS);
        UI.addButton("Load", wfm::doLoad);
        UI.addButton("Quit", UI::quit);
        UI.setMouseMotionListener(wfm::doMouse);
        UI.setWindowSize(950, 630);
    }

}

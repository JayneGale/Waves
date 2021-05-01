

// DO NOT DISTRIBUTE THIS FILE TO STUDENTS
import ecs100.UI;
import ecs100.UIFileChooser;

import javax.sound.sampled.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.time.Duration;
import java.time.Instant;

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

public class SoundWaveform{

    public static final double MAX_VALUE = 300;
    public static final int SAMPLE_RATE = 44100;
    public static final int MAX_SAMPLES = SAMPLE_RATE/100;   // samples in 1/100 sec

    public static final int GRAPH_LEFT = 10;
    public static final int ZERO_LINE = 310;
    public static final int X_STEP = 2;            //pixels between samples
    public static final int GRAPH_WIDTH = MAX_SAMPLES*X_STEP;

    private ArrayList<Double> waveform = new ArrayList<Double>();   // the displayed waveform
    private ArrayList<ComplexNumber> spectrum = new ArrayList<ComplexNumber>(); // the spectrum: length/mod of each X(k)
    public ComplexNumber complex= new ComplexNumber();
    public ArrayList<ComplexNumber> newSpectrum = new ArrayList<ComplexNumber>();;

    /**
     * Displays the waveform.
     */
    public void displayWaveform(){
        if (this.waveform == null){ //there is no data to display
            UI.println("No waveform to display");
            return;
        }
        UI.clearText();
        UI.println("Printing, please wait...");

        UI.clearGraphics();

        // draw x axis (showing where the value 0 will be)
        UI.setColor(Color.black);
        UI.drawLine(GRAPH_LEFT, ZERO_LINE, GRAPH_LEFT + GRAPH_WIDTH , ZERO_LINE);

        // plot points: blue line between each pair of values
        UI.setColor(Color.blue);

        double x = GRAPH_LEFT;
        for (int i=1; i<this.waveform.size(); i++){
            double y1 = ZERO_LINE - this.waveform.get(i-1);
            double y2 = ZERO_LINE - this.waveform.get(i);
            if (i>MAX_SAMPLES){UI.setColor(Color.red);}
            UI.drawLine(x, y1, x+X_STEP, y2);
            x = x + X_STEP;
        }

        UI.println("Printing completed!");
    }

    /**
     * Displays the spectrum. Scale to the range of +/- 300.
     */
    public void displaySpectrum() {
        if (this.spectrum == null){ //there is no data to display
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

        double scaling = 300/max;
        for (int i = 0; i < spectrumMod.size(); i++) {
            spectrumMod.set(i, spectrumMod.get(i)*scaling);
        }

        // draw x axis (showing where the value 0 will be)
        UI.setColor(Color.black);
        UI.drawLine(GRAPH_LEFT, ZERO_LINE, GRAPH_LEFT + GRAPH_WIDTH , ZERO_LINE);

        // plot points: blue line between each pair of values
        UI.setColor(Color.blue);

        double x = GRAPH_LEFT;
        for (int i=1; i<spectrumMod.size(); i++){
            double y1 = ZERO_LINE;
            double y2 = ZERO_LINE - spectrumMod.get(i);
            if (i>MAX_SAMPLES){UI.setColor(Color.red);}
            UI.drawLine(x, y1, x+X_STEP, y2);
            x = x + X_STEP;
        }

        UI.println("Printing completed!");
    }

    public void dft() {
        UI.clearText();
        UI.println("DFT in process, please wait...");
        Instant start = Instant.now();
        ArrayList<Double> case1 = new ArrayList(Arrays.asList(1.0 ,2.0 ,3.0 ,4.0 ,5.0, 6.0, 7.0, 8.0));
        ArrayList<Double> case2 = new ArrayList(Arrays.asList(1.0 ,2.0 ,1.0 ,2.0 ,1.0 ,2.0 ,1.0 ,2.0));
        ArrayList<Double> case3 = new ArrayList(Arrays.asList(1.0 ,2.0 ,3.0 ,4.0 , 4.0 ,3.0, 2.0, 1.0));
        ArrayList<Double> case4 = new ArrayList(Arrays.asList(1.0 ,2.0 ,3.0 ,4.0 ));
        ArrayList<ComplexNumber> x = new ArrayList<ComplexNumber>();
//        waveform = case1;
        // waveform is an array of doubles, size N
        int N = waveform.size();
        System.out.println("Waveform size " + waveform.size());
//          convert the waveform from a simple double to a complex number
        for (int i = 0; i < N; i++){
            ComplexNumber cn = new ComplexNumber(waveform.get(i), 0.0);
            x.add(i,cn);
        }
        boolean isInverse = false;
        spectrum = complex.doTransform(x, isInverse);
        newSpectrum = spectrum;
//      Duration code from StackOverflow https://stackoverflow.com/questions/4927856/how-to-calculate-time-difference-in-java/54428410
        Instant end = Instant.now();
        Duration timeElapsed = Duration.between(start, end);
        double deltaT = (double)timeElapsed.toMillis()/60000;
        System.out.println("Time taken: "+ deltaT +" minutes");
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
        X = complex.doTransform(spectrum, isInverse);
        for(int n = 0; n < N; n++){
            waveform.add(n, X.get(n).getRe());
        }

        Instant end = Instant.now();
        Duration timeElapsed = Duration.between(start, end);
        double deltaT = (double)timeElapsed.toMillis()/60000;
        System.out.println("Time taken: "+ deltaT +" minutes");
        UI.println("IDFT completed!");
        System.out.println("Inverted waveform " + waveform);
        ArrayList<Double> waveformOut = waveform;
        spectrum.clear();
    }

    public void fft() {
        UI.clearText();
        UI.println("FFT in process, please wait...");
        Instant start = Instant.now();
        ArrayList<Double> x = new ArrayList<Double>(); // time signal waveform
        ArrayList<ComplexNumber> X = new ArrayList<ComplexNumber>(); // frequency output
        int S = waveform.size();
//        trim the waveform to a power of 2 and call it x
        if(S > 0){
            int N = complex.maxPowerof2(S);
            for (int i = 0; i < N; i++){
                x.add(waveform.get(i));
            }
//            now do the recursive FFT
            X = FFT(x);

        }

        else {
            System.err.println("waveform is empty: " + S);
        }

        // Add your code here: you should transform from the waveform to the spectrum
        Instant end = Instant.now();
        Duration timeElapsed = Duration.between(start, end);
        double deltaT = (double)timeElapsed.toMillis()/60000;
        System.out.println("Time taken: "+ deltaT +" minutes");
        UI.println("FFT completed!");

        waveform.clear();
    }

    private ArrayList<ComplexNumber> FFT(ArrayList<Double> x) {
        ArrayList<ComplexNumber> X = new ArrayList<ComplexNumber>(); // frequency output
        int N = x.size();
        if(N == 1){
            ArrayList<ComplexNumber> X1 = new ArrayList<ComplexNumber>();
            ComplexNumber x1 = new ComplexNumber(x.get(0), 0.0);
            X1.add(x1);
            return X1;
        }
        ArrayList<Double> xeven = new ArrayList<>();
        ArrayList<Double> xodd = new ArrayList<>();
        for (int i = 0; i < N; i+=2){
            xeven.add(x.get(i));
            xodd.add(x.get(i+1));
//            int odd = i+1;
//            System.out.println("i: "+ i + " i+1" + odd);
        }
// TODO up to here now implement the rest of the FFT

        return X;
    }

    public void ifft() {
        UI.clearText();
        UI.println("IFFT in process, please wait...");

        // TODO
        // Add your code here: you should transform from the spectrum to the waveform

        UI.println("IFFT completed!");
        spectrum.clear();
    }

    /**
     * Save the wave form to a WAV file
     */
    public void doSave() {
        WaveformLoader.doSave(waveform, WaveformLoader.scalingForSavingFile);
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

    public static void main(String[] args){
        SoundWaveform wfm = new SoundWaveform();
        //core
        UI.addButton("Display Waveform", wfm::displayWaveform);
        UI.addButton("Display Spectrum", wfm::displaySpectrum);
        UI.addButton("DFT", wfm::dft);
        UI.addButton("IDFT", wfm::idft);
        UI.addButton("FFT", wfm::fft);
        UI.addButton("IFFT", wfm::ifft);
        UI.addButton("Save", wfm::doSave);
        UI.addButton("Load", wfm::doLoad);
        UI.addButton("Quit", UI::quit);
//        UI.setMouseMotionListener(wfm::doMouse);
        UI.setWindowSize(950, 630);
    }
}
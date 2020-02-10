package uk.co.iseeshapes.capture.controller.listener;

import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;

public class CCDExposureListener implements INDIPropertyListener, INDIElementListener {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(CCDExposureListener.class);

    private INDINumberProperty exposureProperty;
    private INDINumberElement exposureElement;
    private boolean verbose;
    private PrintStream out;
    private double exposure;
    private String filename;
    private int lineLength;
    private int numberOfFrames;
    private int imageNumber;

    private boolean complete;
    private boolean displayFrames;

    private CCDExposureListener(INDINumberProperty exposureProperty, INDINumberElement exposureElement, PrintStream out,
                                double exposure, String filename, int lineLength, int imageNumber, int numberOfFrames,
                                boolean verbose, boolean displayFrames) {

        this.exposureProperty = exposureProperty;
        this.exposureElement = exposureElement;
        this.imageNumber = imageNumber;
        this.numberOfFrames = numberOfFrames;
        this.out = out;
        this.exposure = exposure;
        this.filename = filename;
        this.lineLength = lineLength;
        this.verbose = verbose;
        this.displayFrames = displayFrames;

        exposureProperty.addINDIPropertyListener(this);
        exposureElement.addINDIElementListener(this);

        complete = false;
    }

    public CCDExposureListener(INDINumberProperty exposureProperty, INDINumberElement exposureElement, PrintStream out,
                               double exposure, String filename, int lineLength, int imageNumber, int numberOfFrames) {
        this (exposureProperty, exposureElement, out, exposure, filename, lineLength, imageNumber, numberOfFrames, true, true);
    }

    public CCDExposureListener(INDINumberProperty exposureProperty, INDINumberElement exposureElement, PrintStream out,
                               double exposure, String filename, int lineLength) {
        this (exposureProperty, exposureElement, out, exposure, filename, lineLength, 0, 1, true, false);
    }

    public CCDExposureListener(INDINumberProperty exposureProperty, INDINumberElement exposureElement) {
        this (exposureProperty, exposureElement, null, 1, "None", 1, 0, 1, false, false);
    }


    public boolean isComplete() {
        return complete;
    }

    public boolean isVerbose() {
        return verbose;
    }

    @Override
    public void propertyChanged(INDIProperty indiProperty) {
        INDINumberProperty numberProperty = (INDINumberProperty)indiProperty;
        if (numberProperty.getState() == PropertyStates.OK) {
            exposureProperty.removeINDIPropertyListener(this);
            exposureElement.removeINDIElementListener(this);
            complete = true;
        }
    }

    @Override
    public void elementChanged(INDIElement indiElement) {
        if (!exposureElement.equals(indiElement) || !verbose) {
            return;
        }
        double time = exposureElement.getValue();
        double displayExposure = Math.ceil(exposure);

        String lineStart;
        if (displayFrames) {
            lineStart = String.format("\r%3d of %3d => %s ", imageNumber, numberOfFrames, filename);
        } else {
            lineStart = String.format("\r%s ", filename);
        }

        int percent = (int)Math.round((time * 100)/displayExposure);
        String lineEnd = String.format(" %3d/%3d (%2d%%)", (int)Math.round(displayExposure - time), (int)displayExposure, percent);

        out.print(lineStart);

        int steps = lineLength - lineStart.length() - lineEnd.length();
        double step = displayExposure / steps;
        double position = 0.0;
        while (position < displayExposure - time) {
            out.print("=");
            position += step;
        }
        if (position < displayExposure) {
            out.print(">");
            position += step;
        }
        while (position < displayExposure) {
            out.print(" ");
            position += step;
        }
        out.print(lineEnd);
    }
}

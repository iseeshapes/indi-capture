package uk.co.iseeshapes.capture.controller;

import org.indilib.i4j.Constants;
import org.indilib.i4j.client.*;
import org.indilib.i4j.protocol.NewNumberVector;
import org.indilib.i4j.protocol.OneNumber;
import org.indilib.i4j.protocol.api.INDIConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.iseeshapes.capture.controller.listener.CCDDownloadListener;
import uk.co.iseeshapes.capture.controller.listener.CCDExposureListener;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class CCDExposureController {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(CCDExposureController.class);

    private static final String exposurePropertyName = "CCD_EXPOSURE";
    private static final String exposureElementName = "CCD_EXPOSURE_VALUE";
    private static final String downloadPropertyName = "CCD1";
    private static final String downloadElementName = "CCD1";

    private INDIConnection indiConnection;
    private INDIServerConnection indiServerConnection;
    private PrintStream out;
    private int lineLength;
    private final String cameraName;

    public CCDExposureController(INDIConnection indiConnection, INDIServerConnection indiServerConnection,
                                 PrintStream out, String cameraName, int lineLength) {
        this.indiConnection = indiConnection;
        this.indiServerConnection = indiServerConnection;
        this.out = out;
        this.cameraName = cameraName;
        this.lineLength = lineLength;
    }

    private void sendCapture (double time) throws IOException {
        NewNumberVector vector = new NewNumberVector();
        vector.setDevice(cameraName);
        vector.setName(exposurePropertyName);
        vector.setState(Constants.getPropertyStateAsString(Constants.PropertyStates.BUSY));

        OneNumber number = new OneNumber();
        number.setDevice(cameraName);
        number.setName(exposureElementName);
        number.setTextContent(Double.toString(time));
        vector.addElement(number);

        indiConnection.getINDIOutputStream().writeObject(vector);
    }

    public void capture (double exposure, File file, int imageNumber, int numberOfFrames, boolean verbose) throws IOException {
        INDINumberElement exposureElement = (INDINumberElement)indiServerConnection.getElement(cameraName,
                exposurePropertyName, exposureElementName);
        INDINumberProperty exposureProperty = (INDINumberProperty)exposureElement.getProperty();
        CCDExposureListener exposureListener;
        if (verbose) {
            if (file == null) {
                exposureListener = new CCDExposureListener(exposureProperty, exposureElement, out, exposure,
                        "Preview", lineLength);
            } else if (numberOfFrames == 0){
                exposureListener = new CCDExposureListener(exposureProperty, exposureElement, out, exposure,
                        file.getName(), lineLength);
            } else {
                exposureListener = new CCDExposureListener(exposureProperty, exposureElement, out, exposure,
                        file.getName(), lineLength, imageNumber, numberOfFrames);
            }
        } else {
            exposureListener = new CCDExposureListener(exposureProperty, exposureElement);
        }
        sendCapture(exposure);
        while (!exposureListener.isComplete()) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                log.error("Thread interrupted", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    public void capture (double exposure, boolean verbose) throws IOException {
        capture(exposure, null, 0, 0, verbose);
    }

    private CCDDownloadListener createDownloadListener (File file, int imageNumber, int numberOfFrames)
            throws IOException {
        INDIBLOBElement downloadElement = null;

        while (downloadElement == null) {
            downloadElement = (INDIBLOBElement)indiServerConnection.getElement(cameraName,
                    downloadPropertyName, downloadElementName);
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        CCDDownloadListener downloadListener;
        if (numberOfFrames > 0) {
            downloadListener = new CCDDownloadListener(downloadElement, out, file, lineLength, numberOfFrames,
                    imageNumber);
        } else {
            downloadListener = new CCDDownloadListener(downloadElement, out, file, lineLength);
        }

        downloadElement.getProperty().getDevice().blobsEnable(Constants.BLOBEnables.ALSO);

        return downloadListener;
    }

    private void download (CCDDownloadListener downloadListener) {
        while (!downloadListener.isComplete()) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void captureAndDownload (double exposure, File file, int imageNumber)
            throws IOException {
        CCDDownloadListener downloadListener = createDownloadListener(file, 0, 0);
        capture(exposure, file, imageNumber, 0, true);
        download(downloadListener);
    }

    public void captureAndDownload (double exposure, File file, int imageNumber, int numberOfFrames)
            throws IOException {
        CCDDownloadListener downloadListener = createDownloadListener(file, imageNumber, numberOfFrames);
        capture(exposure, file, imageNumber, numberOfFrames, true);
        download(downloadListener);
    }
}

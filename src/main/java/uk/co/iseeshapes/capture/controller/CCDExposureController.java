package uk.co.iseeshapes.capture.controller;

import org.indilib.i4j.Constants;
import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.client.*;
import org.indilib.i4j.protocol.NewNumberVector;
import org.indilib.i4j.protocol.OneNumber;
import org.indilib.i4j.protocol.api.INDIConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.iseeshapes.capture.configuration.ApplicationConfiguration;
import uk.co.iseeshapes.capture.device.UploadMode;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class CCDExposureController implements INDIPropertyListener, INDIElementListener {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(CCDExposureController.class);

    private static final String exposurePropertyName = "CCD_EXPOSURE";
    private static final String exposureElementName = "CCD_EXPOSURE_VALUE";
    private static final String downloadPropertyName = "CCD1";
    private static final String downloadElementName = "CCD1";

    private enum ExposureStatus { waiting, acquiring, downloading, completed, aborted }

    private INDIConnection indiConnection;
    private INDIServerConnection indiServerConnection;
    private PrintStream out;
    private ApplicationConfiguration applicationConfiguration;
    private final double exposure;
    private final int numberOfFrames;
    private final File file;
    private final int imageNumber;
    private final String cameraName;
    private final UploadMode uploadMode;
    private final boolean silent;

    private ExposureStatus exposureStatus;

    private INDINumberProperty exposureProperty;
    private INDINumberElement exposureElement;
    private INDIElement downloadElement;

    public CCDExposureController(INDIConnection indiConnection, INDIServerConnection indiServerConnection,
                                 PrintStream out, ApplicationConfiguration applicationConfiguration,
                                 double exposure, int numberOfFrames, File file, int imageNumber,
                                 String cameraName, UploadMode uploadMode, boolean silent) {
        this.indiConnection = indiConnection;
        this.indiServerConnection = indiServerConnection;
        this.out = out;
        this.applicationConfiguration = applicationConfiguration;
        this.exposure = exposure;
        this.numberOfFrames = numberOfFrames;
        this.file = file;
        this.imageNumber = imageNumber;
        this.cameraName = cameraName;
        this.uploadMode = uploadMode;
        this.silent = silent;

        exposureStatus = ExposureStatus.waiting;
    }


    private void sendCapture (double time) throws IOException {
        exposureElement = (INDINumberElement)indiServerConnection.getElement(cameraName, exposurePropertyName, exposureElementName);
        exposureProperty = (INDINumberProperty)exposureElement.getProperty();

        NewNumberVector vector = new NewNumberVector();
        vector.setDevice(cameraName);
        vector.setName(exposurePropertyName);
        vector.setState(Constants.getPropertyStateAsString(Constants.PropertyStates.BUSY));

        OneNumber number = new OneNumber();
        number.setDevice(cameraName);
        number.setName(exposureElementName);
        number.setTextContent(Double.toString(time));
        vector.addElement(number);

        if (uploadMode != UploadMode.local) {
            exposureElement.addINDIElementListener(this);
        }
        exposureProperty.addINDIPropertyListener(this);

        exposureStatus = ExposureStatus.acquiring;
        indiConnection.getINDIOutputStream().writeObject(vector);
    }

    public void start () throws IOException {
        downloadElement = null;

        sendCapture(exposure);

        while (exposureStatus != ExposureStatus.completed && exposureStatus != ExposureStatus.aborted) {
            if (uploadMode != UploadMode.local && downloadElement == null) {
                downloadElement = indiServerConnection.getElement(cameraName, downloadPropertyName, downloadElementName);
                if (downloadElement != null) {
                    downloadElement.addINDIElementListener(this);
                    downloadElement.getProperty().getDevice().blobsEnable(Constants.BLOBEnables.ALSO);
                }
            }
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error ("Exposure thread interrupted", e);
            }
        }
        exposureProperty.removeINDIPropertyListener(this);
        exposureElement.removeINDIElementListener(this);
        if (downloadElement != null) {
            downloadElement.removeINDIElementListener(this);
        }
    }

    @Override
    public void propertyChanged(INDIProperty indiProperty) {
        INDINumberProperty numberProperty = (INDINumberProperty)indiProperty;
        if (numberProperty.getState() == PropertyStates.OK) {
            exposureProperty.removeINDIPropertyListener(this);
            exposureElement.removeINDIElementListener(this);
            if (uploadMode == UploadMode.local) {
                exposureStatus = ExposureStatus.completed;
            }
        }
    }

    private void updateExposureTime (double time) {
        double displayExposure = Math.ceil(exposure);

        String lineStart = String.format("\r%3d of %3d => %s ", imageNumber, numberOfFrames, file.getName());

        int percent = (int)Math.round((time * 100)/displayExposure);
        String lineEnd = String.format(" %3d/%3d (%2d%%)", (int)Math.round(displayExposure - time), (int)displayExposure, percent);

        out.print(lineStart);

        int steps = applicationConfiguration.getLineLength() - lineStart.length() - lineEnd.length();
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

    @Override
    public void elementChanged(INDIElement indiElement) {
        if (exposureElement.equals(indiElement)) {
            if (exposureStatus == ExposureStatus.acquiring && !silent) {
                INDINumberElement numberElement = (INDINumberElement) indiElement;
                updateExposureTime(numberElement.getValue());
            }
        } else if (downloadElement != null && downloadElement.equals(indiElement)) {
            exposureStatus = ExposureStatus.downloading;
            indiElement.removeINDIElementListener(this);
            INDIBLOBElement blob = (INDIBLOBElement)indiElement;
            try {
                blob.getValue().saveBLOBData(file);
            } catch (IOException e) {
                out.print('\n');
                log.error("Unable to download file", e);
                exposureStatus = ExposureStatus.aborted;
                return;
            }
            if (!silent) {
                out.print('\r');
                for (int i = 0; i < applicationConfiguration.getLineLength(); i++) {
                    out.print(' ');
                }
                out.printf("\rCompleted - %d of %d - %s%n", imageNumber, numberOfFrames, file.getName());
            }
            exposureStatus = ExposureStatus.completed;
        }
    }
}

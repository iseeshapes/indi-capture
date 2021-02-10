package uk.co.iseeshapes.capture.controller;

import org.indilib.i4j.Constants;
import org.indilib.i4j.client.INDIServerConnection;
import org.indilib.i4j.client.INDISwitchProperty;
import org.indilib.i4j.protocol.NewSwitchVector;
import org.indilib.i4j.protocol.OneSwitch;
import org.indilib.i4j.protocol.api.INDIConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.iseeshapes.capture.AbortException;
import uk.co.iseeshapes.capture.device.UploadMode;

import java.io.IOException;
import java.util.Date;

public class CCDUploadController {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(CCDUploadController.class);

    private static final String uploadPropertyName = "UPLOAD_MODE";
    private static final String uploadClientElementName = "UPLOAD_CLIENT";
    private static final String uploadLocalElementName = "UPLOAD_LOCAL";
    private static final String uploadBothElementName = "UPLOAD_BOTH";

    private static final long timeout = 50000;

    private final INDIConnection indiConnection;
    private final INDIServerConnection indiServerConnection;
    private final String cameraName;

    public CCDUploadController(INDIConnection indiConnection, INDIServerConnection indiServerConnection,
                               String cameraName) {
        this.indiConnection = indiConnection;
        this.indiServerConnection = indiServerConnection;
        this.cameraName = cameraName;
    }

    private UploadMode getUploadMode () throws AbortException {
        INDISwitchProperty uploadProperty = (INDISwitchProperty) indiServerConnection.getProperty(cameraName, uploadPropertyName);
        if (uploadProperty.getElement(uploadClientElementName).getValue() == Constants.SwitchStatus.ON) {
            return UploadMode.client;
        }
        if (uploadProperty.getElement(uploadLocalElementName).getValue() == Constants.SwitchStatus.ON) {
            return UploadMode.local;
        }
        if (uploadProperty.getElement(uploadBothElementName).getValue() == Constants.SwitchStatus.ON) {
            return UploadMode.both;
        }
        throw new AbortException("Unknown upload state");
    }

    public void sendUploadMode (UploadMode uploadMode) throws IOException, AbortException {
        if (uploadMode == getUploadMode()) {
            return;
        }
        NewSwitchVector vector = new NewSwitchVector();
        vector.setDevice(cameraName);
        vector.setName(uploadPropertyName);

        OneSwitch oneSwitch = new OneSwitch();
        oneSwitch.setDevice(cameraName);
        oneSwitch.setTextContent("On");

        vector.addElement(oneSwitch);

        switch (uploadMode) {
            case client:
                oneSwitch.setName(uploadClientElementName);
                break;
            case local:
                oneSwitch.setName(uploadLocalElementName);
                break;
            case both:
                oneSwitch.setName(uploadBothElementName);
                break;
        }
        indiConnection.getINDIOutputStream().writeObject(vector);

        long start = new Date().getTime();
        while (uploadMode != getUploadMode()) {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (new Date().getTime() - start > timeout) {
                throw new AbortException("Timeout waiting for Upload Mode to change");
            }
        }
    }

}

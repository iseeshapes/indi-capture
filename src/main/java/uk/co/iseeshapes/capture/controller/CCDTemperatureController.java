package uk.co.iseeshapes.capture.controller;

import org.indilib.i4j.Constants;
import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.client.INDINumberElement;
import org.indilib.i4j.client.INDIServerConnection;
import org.indilib.i4j.protocol.NewNumberVector;
import org.indilib.i4j.protocol.OneNumber;
import org.indilib.i4j.protocol.api.INDIConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.iseeshapes.capture.controller.listener.CCDTemperatureListener;

import java.io.IOException;
import java.io.PrintStream;

public class CCDTemperatureController {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(CCDTemperatureController.class);

    private static final String temperaturePropertyName = "CCD_TEMPERATURE";
    private static final String temperatureElementName = "CCD_TEMPERATURE_VALUE";

    private INDIServerConnection indiServerConnection;
    private INDIConnection indiConnection;
    private PrintStream out;
    private String cameraName;
    private int lineLength;

    public CCDTemperatureController(INDIServerConnection indiServerConnection, INDIConnection indiConnection,
                                    PrintStream out, String cameraName, int lineLength) {
        this.indiServerConnection = indiServerConnection;
        this.indiConnection = indiConnection;
        this.out = out;
        this.cameraName = cameraName;
        this.lineLength = lineLength;
    }

    private void clearLine () {
        out.print('\r');
        for (int i=0;i<lineLength;i++) {
            out.print(' ');
        }
    }

    public void setTemperature (final double targetTemperature, final double tolerance) throws IOException {
        INDINumberElement numberElement = (INDINumberElement)indiServerConnection.getElement(cameraName,
                temperaturePropertyName, temperatureElementName);

        CCDTemperatureListener temperatureListener = new CCDTemperatureListener(numberElement, out, targetTemperature,
                tolerance, lineLength);
        if (temperatureListener.isComplete()) {
            return;
        }

        NewNumberVector vector = new NewNumberVector();
        vector.setDevice(cameraName);
        vector.setName(temperaturePropertyName);
        vector.setState(Constants.getPropertyStateAsString(PropertyStates.BUSY));

        OneNumber temperatureElement = new OneNumber();
        temperatureElement.setDevice(cameraName);
        temperatureElement.setName(temperatureElementName);
        temperatureElement.setTextContent(Double.toString(targetTemperature));

        vector.addElement(temperatureElement);

        indiConnection.getINDIOutputStream().writeObject(vector);

        while (!temperatureListener.isComplete()) {
            try {
                Thread.sleep(200L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        clearLine();
        out.printf("\rTemperature set to %+4.1fC%n", targetTemperature);
    }

}

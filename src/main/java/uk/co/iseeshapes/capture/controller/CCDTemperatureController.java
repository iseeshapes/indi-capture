package uk.co.iseeshapes.capture.controller;

import org.indilib.i4j.Constants;
import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.client.INDIElement;
import org.indilib.i4j.client.INDIElementListener;
import org.indilib.i4j.client.INDINumberElement;
import org.indilib.i4j.client.INDIServerConnection;
import org.indilib.i4j.protocol.NewNumberVector;
import org.indilib.i4j.protocol.OneNumber;
import org.indilib.i4j.protocol.api.INDIConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.iseeshapes.capture.configuration.ApplicationConfiguration;

import java.io.IOException;
import java.io.PrintStream;

public class CCDTemperatureController implements INDIElementListener {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(CCDTemperatureController.class);

    private static final String temperaturePropertyName = "CCD_TEMPERATURE";
    private static final String temperatureElementName = "CCD_TEMPERATURE_VALUE";

    private INDIServerConnection indiServerConnection;
    private INDIConnection indiConnection;
    private ApplicationConfiguration applicationConfiguration;
    private PrintStream out;
    private String cameraName;

    private double startTemperature;
    private double currentTemperature;
    private double targetTemperature;
    private double tolerance;

    public CCDTemperatureController(INDIServerConnection indiServerConnection, INDIConnection indiConnection,
                                    ApplicationConfiguration applicationConfiguration, PrintStream out,
                                    String cameraName, double targetTemperature, double tolerance) {
        this.indiServerConnection = indiServerConnection;
        this.indiConnection = indiConnection;
        this.applicationConfiguration = applicationConfiguration;
        this.out = out;
        this.cameraName = cameraName;
        this.targetTemperature = targetTemperature;
        this.tolerance = tolerance;
    }

    private void clearLine () {
        out.print('\r');
        for (int i=0;i<applicationConfiguration.getLineLength();i++) {
            out.print(' ');
        }
    }

    private boolean isInRange (double temperature) {
        return targetTemperature - tolerance < temperature && temperature < targetTemperature + tolerance;
    }

    public void start () throws IOException {
        INDINumberElement numberElement = (INDINumberElement)indiServerConnection.getElement(cameraName,
                temperaturePropertyName, temperatureElementName);

        startTemperature = numberElement.getValue();
        currentTemperature = startTemperature;
        if (isInRange(startTemperature)) {
            return;
        }

        numberElement.addINDIElementListener(this);

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

        while (!isInRange(currentTemperature)) {
            try {
                Thread.sleep(200L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        clearLine();
        out.printf("\rTemperature set to %+4.1fC%n", currentTemperature);
    }

    @Override
    public void elementChanged(INDIElement indiElement) {
        INDINumberElement temperatureElement = (INDINumberElement)indiElement;
        currentTemperature = temperatureElement.getValue();
        String start = String.format("\rTarget Temperature %+4.1fC (+/-%3.1fC) ", targetTemperature, tolerance);
        String end = String.format(" %+4.1fC", currentTemperature);
        int remainingLineSize = applicationConfiguration.getLineLength() - start.length() - end.length();

        out.print(start);

        double totalRange = Math.abs(startTemperature - targetTemperature);
        double currentPoint = Math.abs(startTemperature - currentTemperature);
        double step = totalRange / remainingLineSize;

        double point = 0;
        while(point < currentPoint) {
            point += step;
            out.print('=');
        }
        out.print('>');
        point += step;
        while (point < totalRange) {
            out.print(' ');
            point += step;
        }

        out.print(end);
    }
}

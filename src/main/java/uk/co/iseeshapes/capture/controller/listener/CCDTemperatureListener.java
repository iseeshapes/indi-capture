package uk.co.iseeshapes.capture.controller.listener;

import org.indilib.i4j.client.INDIElement;
import org.indilib.i4j.client.INDIElementListener;
import org.indilib.i4j.client.INDINumberElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;

public class CCDTemperatureListener implements INDIElementListener {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(CCDTemperatureListener.class);

    private final INDINumberElement numberElement;
    private final PrintStream out;

    private final double startTemperature;
    private final double targetTemperature;
    private final double tolerance;
    private double currentTemperature;
    private final int lineLength;

    public CCDTemperatureListener(INDINumberElement numberElement, PrintStream out, double targetTemperature,
                                  double tolerance, int lineLength) {
        this.numberElement = numberElement;
        this.out = out;
        this.targetTemperature = targetTemperature;
        this.tolerance = tolerance;
        this.lineLength = lineLength;

        numberElement.addINDIElementListener(this);
        currentTemperature = numberElement.getValue();
        startTemperature = currentTemperature;
    }

    public boolean isComplete () {
        if (targetTemperature - tolerance < currentTemperature && currentTemperature < targetTemperature + tolerance) {
            numberElement.removeINDIElementListener(this);
            return true;
        }
        return false;
    }

    @Override
    public void elementChanged(INDIElement indiElement) {
        INDINumberElement temperatureElement = (INDINumberElement)indiElement;
        currentTemperature = temperatureElement.getValue();
        String start = String.format("\rTarget Temperature %+4.1fC (+/-%3.1fC) ", targetTemperature, tolerance);
        String end = String.format(" %+4.1fC", currentTemperature);
        int remainingLineSize = lineLength - start.length() - end.length();

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

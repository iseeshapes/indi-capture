package uk.co.iseeshapes.capture.controller;

import org.indilib.i4j.Constants;
import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.Constants.SwitchStatus;
import org.indilib.i4j.client.*;
import org.indilib.i4j.protocol.NewSwitchVector;
import org.indilib.i4j.protocol.OneSwitch;
import org.indilib.i4j.protocol.api.INDIConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DeviceConnectionController implements INDIPropertyListener {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(DeviceConnectionController.class);

    private static final String propertyName = "CONNECTION";
    private static final String connectElementName = "CONNECT";
    private static final String disconnectElementName = "DISCONNECT";

    private INDIConnection indiConnection;
    private INDIServerConnection indiServerConnection;
    private String deviceName;

    public DeviceConnectionController(INDIConnection indiConnection, INDIServerConnection indiServerConnection,
                                      String deviceName) {
        this.indiConnection = indiConnection;
        this.indiServerConnection = indiServerConnection;
        this.deviceName = deviceName;
    }

    public boolean isConnected () {
        INDISwitchElement element = (INDISwitchElement)indiServerConnection.getElement(deviceName, propertyName, connectElementName);
        status = element.getValue();
        return status == Constants.SwitchStatus.ON;
    }

    public void connect () throws IOException {
        if (status == SwitchStatus.ON) {
            log.info("{} already connected", deviceName);
            return;
        }
        log.info("{} connecting", deviceName);
        states = PropertyStates.BUSY;
        NewSwitchVector vector = new NewSwitchVector();
        vector.setDevice(deviceName);
        vector.setName(propertyName);
        vector.setState(Constants.getPropertyStateAsString(states));

        OneSwitch connect = new OneSwitch();
        connect.setDevice(deviceName);
        connect.setName(connectElementName);
        connect.setTextContent(Constants.getSwitchStatusAsString(Constants.SwitchStatus.ON));
        vector.addElement(connect);

        OneSwitch disconnect = new OneSwitch();
        disconnect.setDevice(deviceName);
        disconnect.setName(disconnectElementName);
        disconnect.setTextContent(Constants.getSwitchStatusAsString(Constants.SwitchStatus.OFF));
        vector.addElement(disconnect);

        INDIDevice device = indiServerConnection.getDevice(deviceName);
        INDIProperty<?> property = device.getProperty(propertyName);
        property.addINDIPropertyListener(this);

        indiConnection.getINDIOutputStream().writeObject(vector);

        while (states != PropertyStates.OK) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private SwitchStatus status;
    private PropertyStates states;

    @Override
    public void propertyChanged(INDIProperty<?> indiProperty) {
        states = indiProperty.getState();
    }
}

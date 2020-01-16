package uk.co.iseeshapes.capture.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.iseeshapes.capture.AbortException;
import uk.co.iseeshapes.capture.configuration.ApplicationConfiguration;
import uk.co.iseeshapes.capture.xml.AbstractVector;
import uk.co.iseeshapes.capture.xml.State;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Client {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(Client.class);

    private Connection connection;
    private ResponseParser responseParser;
    private boolean initialised;

    public Client(ApplicationConfiguration applicationConfiguration) throws IOException {
        connection = new Connection();
        connection.connect(applicationConfiguration);
        initialise();
    }

    public void initialise () throws IOException {
        responseParser = new ResponseParser();
        refresh();
    }

    public void refresh () throws IOException {
        initialised = false;
        connection.send ("<getProperties />", responseParser);
        initialised = true;
    }

    public void disconnect () throws IOException {
        connection.disconnect();
    }

    private void initialised () throws AbortException {
        if (!initialised) {
            throw new AbortException("Server in unknown state");
        }
    }

    public boolean isPrintXML () {
        return connection.isPrintXML();
    }

    public void setPrintXML(boolean printXML) {
        connection.setPrintXML(printXML);
    }

    public Set<String> getDeviceNames () throws AbortException {
        initialised();
        Set<String> deviceName = new HashSet<>();
        for (AbstractVector<?> vector : responseParser.getVectors().values()) {
            deviceName.add(vector.getDevice());
        }
        return deviceName;
    }

    public Set<String> getPropertyNames (String deviceName) throws AbortException {
        initialised();
        Set<String> propertyNames = new HashSet<>();
        for (AbstractVector<?> vector : responseParser.getVectors().values()) {
            if (deviceName.equals(vector.getDevice())) {
                propertyNames.add(vector.getName());
            }
        }
        return propertyNames;
    }

    private AbstractVector<?> getVector (String deviceName, String propertyName) throws AbortException {
        initialised();
        for (AbstractVector<?> vector : responseParser.getVectors().values()) {
            if (deviceName.equals(vector.getDevice()) && propertyName.equals(vector.getName())) {
                return vector;
            }
        }
        return null;
    }

    public Set<String> getValueNames (String deviceName, String propertyName) throws AbortException {
        initialised();
        Set<String> valueNames = new HashSet<>();
        AbstractVector<?> vector = getVector(deviceName, propertyName);
        if (vector != null) {
            valueNames.addAll(vector.getValueNames());
        }
        return valueNames;
    }

    public <T> T getValue (String deviceName, String propertyName, String valueName) throws AbortException {
        return getValue(deviceName, propertyName, valueName, false);
    }

    private void refreshProperty (String deviceName, String propertyName) throws AbortException {
        String xml = "<getProperties version=\"1.7\" device=\"" + deviceName + "\" name=\"" + propertyName + "\" />";
        try {
            connection.send(xml, responseParser);
        } catch (IOException e) {
            initialised = false;
            throw new AbortException("Unable to refresh " + deviceName + "." + propertyName + " (" + xml + ")", e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue (String deviceName, String propertyName, String valueName, boolean refresh) throws AbortException {
        initialised();
        if (refresh) {
            refreshProperty(deviceName, propertyName);
        }
        AbstractVector<?> vector = getVector(deviceName, propertyName);
        if (vector != null) {
            try {
                return (T)vector.getValue(valueName);
            } catch (ClassCastException e) {
                throw new AbortException(String.format("%s.%s.%s cannot be cast", deviceName, propertyName, valueName));
            }
        }
        throw new AbortException(String.format("%s.%s.%s not found", deviceName, propertyName, valueName));
    }

    @SuppressWarnings("unchecked")
    public <T> T setValue (String deviceName, String propertyName, String valueName, T value, boolean update) throws AbortException {
        initialised();
        refreshProperty(deviceName, propertyName);
        AbstractVector<?> vector = getVector(deviceName, propertyName);
        if (vector == null) {
            throw new AbortException("No property " + deviceName + "." + propertyName);
        }
        try {
            ((AbstractVector<T>)vector).setValue(valueName, value);
        } catch (AbortException e) {
            throw new AbortException("Cannot find value of " + valueName + " in " + deviceName + "." + propertyName);
        } catch (ClassCastException e) {
            throw new AbortException("Cannot cast value", e);
        }
        /*
        if (vector.getState() == State.Busy) {
            throw new AbortException(deviceName + "." + propertyName + " is busy");
        }
        */
        if (update) {
            vector.setState(State.Busy);
        } else {
            vector.setState(State.Ok);
        }
        try {
            connection.send(vector.newPropertyXML(valueName), responseParser);
        } catch (IOException e) {
            throw new AbortException("Cannot set " + deviceName + "." + propertyName);
        }
        return getValue(deviceName, propertyName, valueName);
    }

    public void printValues () {
        if (log.isInfoEnabled()) {
            StringBuilder print = new StringBuilder();
            for (AbstractVector<?> vector : responseParser.getVectors().values()) {
                vector.buildMessage(print);
            }
            log.info(print.toString());
        }
    }
}

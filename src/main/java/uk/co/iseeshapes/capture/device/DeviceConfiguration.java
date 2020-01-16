package uk.co.iseeshapes.capture.device;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.iseeshapes.capture.AbortException;
import uk.co.iseeshapes.capture.client.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class DeviceConfiguration {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(DeviceConfiguration.class);

    private static final Pattern abortPattern = Pattern.compile("^[qQ].*$");

    private static final String connectionPropertyName = "CONNECTION";
    private static final String connectValueName = "CONNECT";
    private static final String disconnectValueName = "DISCONNECT";

    @JsonIgnore
    private PrintStream out;

    @JsonIgnore
    private BufferedReader reader;

    @JsonIgnore
    protected Client client;

    @JsonProperty
    protected String deviceName = null;

    public void setStreams(PrintStream out, BufferedReader reader, Client client) {
        this.out = out;
        this.reader = reader;
        this.client = client;
    }

    protected abstract String getDeviceType ();

    public void assignDevice () throws AbortException {
        out.println("The following devices are connected to the server:");
        List<String> devices = new ArrayList<>(client.getDeviceNames());
        Integer selected = null;
        for (int i=0;i<devices.size();i++) {
            out.printf("%3d: %s%n", i+1, devices.get(i));
            if (deviceName != null && deviceName.equals(devices.get(i))) {
                selected = i+1;
            }
        }
        String rawValue;
        while (true) {
            out.printf("Please select %s", getDeviceType());
            if (selected != null) {
                out.printf(" (%d)", selected);
            }
            out.print(": ");
            try {
                rawValue = reader.readLine();
            } catch (IOException e) {
                throw new AbortException("Cannot get input", e);
            }
            Matcher matcher = abortPattern.matcher(rawValue);
            if (matcher.matches()) {
                throw new AbortException();
            }
            if (selected != null && rawValue.length() == 0) {
                this.deviceName = devices.get(selected - 1);
                return;
            }
            try {
                selected = Integer.parseInt(rawValue);
                if (1 <= selected && selected <= devices.size()) {
                    this.deviceName = devices.get(selected - 1);
                    return;
                }
            } catch (NumberFormatException e) {
                continue;
            }
            out.printf ("\rInvalid value (%s) ", rawValue);
        }
    }

    @JsonIgnore
    public boolean isConnected () throws AbortException {
        return client.getValue(deviceName, connectionPropertyName, connectValueName);
    }

    @JsonIgnore
    public void setConnected (boolean connected) throws AbortException {
        if (connected) {
            client.setValue(deviceName, connectionPropertyName, connectValueName, true, false);
        } else {
            client.setValue(deviceName, connectionPropertyName, disconnectValueName, false, false);
        }
    }
}

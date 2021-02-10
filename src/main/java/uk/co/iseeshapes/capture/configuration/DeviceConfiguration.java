package uk.co.iseeshapes.capture.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.iseeshapes.capture.AbortException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeviceConfiguration {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(DeviceConfiguration.class);

    private static final Pattern abortPattern = Pattern.compile("^[qQ].*$");

    private static final String deviceTypeKey = "device-type";
    private static final String deviceNameKey = "device-name";

    @JsonIgnore
    private PrintStream out;

    @JsonIgnore
    private BufferedReader reader;

    @JsonProperty (deviceTypeKey)
    private final String deviceType;

    @JsonProperty (deviceNameKey)
    private String deviceName = null;

    public DeviceConfiguration (String deviceType) {
        this.deviceType = deviceType;
    }

    @JsonCreator
    public DeviceConfiguration (@JsonProperty(deviceTypeKey) String deviceType, @JsonProperty(deviceNameKey) String deviceName) {
        this(deviceType);

        this.deviceName = deviceName;
    }

    @JsonIgnore
    public String getDeviceName() {
        return deviceName;
    }

    public void setStreams(PrintStream out, BufferedReader reader) {
        this.out = out;
        this.reader = reader;
    }

    public void assignDevice (String[] deviceNames) throws AbortException {
        if (deviceNames.length == 0) {
            throw new AbortException("No devices connected to the server");
        }
        out.println("The following devices are connected to the server:");
        Integer selected = null;
        for (int i=0;i<deviceNames.length;i++) {
            out.printf("%3d: %s%n", i+1, deviceNames[i]);
            if (deviceName != null && deviceName.equals(deviceNames[i])) {
                selected = i+1;
            }
        }
        String rawValue;
        while (true) {
            out.printf("Please select %s", deviceType);
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
                this.deviceName = deviceNames[selected - 1];
                return;
            }
            try {
                selected = Integer.parseInt(rawValue);
                if (1 <= selected && selected <= deviceNames.length) {
                    this.deviceName = deviceNames[selected - 1];
                    return;
                }
            } catch (NumberFormatException e) {
                continue;
            }
            out.printf ("\rInvalid value (%s) ", rawValue);
        }
    }
}

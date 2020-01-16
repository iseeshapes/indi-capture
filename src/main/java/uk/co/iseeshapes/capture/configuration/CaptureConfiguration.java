package uk.co.iseeshapes.capture.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.iseeshapes.capture.AbortException;

import java.io.IOException;

public class CaptureConfiguration extends AbstractConfiguration {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(CaptureConfiguration.class);

    private static final String prefixKey = "filename-prefix";
    private static final String temperatureKey = "temperature";
    private static final String toleranceKey = "temperature-tolerance";
    private static final String exposureKey = "exposure";
    private static final String noOfFramesKey = "no-of-frames";

    @JsonProperty(prefixKey)
    private String prefix;

    @JsonProperty(temperatureKey)
    private Double temperature;

    @JsonProperty(toleranceKey)
    private Double tolerance;

    @JsonProperty(exposureKey)
    private Double exposure;

    @JsonProperty(noOfFramesKey)
    private Integer noOfFrames;

    @JsonCreator
    public CaptureConfiguration(@JsonProperty(prefixKey) String prefix,
                                @JsonProperty(temperatureKey) Double temperature,
                                @JsonProperty(toleranceKey) Double tolerance,
                                @JsonProperty(exposureKey) Double exposure,
                                @JsonProperty(noOfFramesKey) Integer noOfFrames) {
        this.prefix = prefix;
        this.temperature = temperature;
        this.tolerance = tolerance;
        this.exposure = exposure;
        this.noOfFrames = noOfFrames;
    }

    public CaptureConfiguration () {
        prefix = null;
        temperature = null;
        tolerance = null;
        exposure = null;
        noOfFrames = null;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String message, String defaultValue) throws IOException, AbortException {
        prefix = readValue(message, defaultValue, (String rawValue) -> rawValue);
    }

    public double getTolerance() {
        return tolerance;
    }

    public void setTolerance (String message, Double defaultValue) throws IOException, AbortException {
        tolerance = readValue(message, defaultValue, (String rawValue) -> {
            try {
                return Double.parseDouble(rawValue);
            } catch (NumberFormatException e) {
                return null;
            }
        });
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature (String message, Double defaultValue) throws IOException, AbortException {
        temperature = readValue(message, defaultValue, (String rawValue) -> {
            try {
                return Double.parseDouble(rawValue);
            } catch (NumberFormatException e) {
                return null;
            }
        });
    }

    public double getExposure() {
        return exposure;
    }

    public void setExposure (String message, Double defaultValue) throws IOException, AbortException {
        exposure = readValue(message, defaultValue, (String rawValue) -> {
            try {
                double value = Double.parseDouble(rawValue);
                if (value < 0.0)
                    return null;
                return value;
            } catch (NumberFormatException e) {
                return null;
            }
        });
    }

    public int getNoOfFrames() {
        return noOfFrames;
    }

    public void setNoOfFrames (String message, Integer defaultValue) throws IOException, AbortException {
        noOfFrames = readValue(message, defaultValue, (String rawValue) -> {
            try {
                int value = Integer.parseInt(rawValue);
                if (value < 1)
                    return null;
                return value;
            } catch (NumberFormatException e) {
                return null;
            }
        });
    }

    @Override
    public void fillInBlanks() throws IOException, AbortException {
        if (prefix == null) {
            setPrefix("Missing Filename Prefix", null);
        }
        if (temperature == null) {
            setTemperature("Missing Temperature", null);
        }
        if (tolerance == null) {
            setTolerance("Missing Temperature Tolerance", null);
        }
        if (exposure == null) {
            setExposure("Missing Exposure", null);
        }
        if (noOfFrames == null) {
            setNoOfFrames("Missing No Of Frames", null);
        }
    }

    @Override
    public void ask() throws IOException, AbortException {
        setPrefix ("Filename", prefix);
        setTemperature("Temperature", temperature);
        setTolerance("Temperature Tolerance", tolerance);
        setExposure("Exposure", exposure);
        setNoOfFrames("No of frames", noOfFrames);
    }
}

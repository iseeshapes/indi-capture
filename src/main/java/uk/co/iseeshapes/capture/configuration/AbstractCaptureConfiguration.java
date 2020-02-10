package uk.co.iseeshapes.capture.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.iseeshapes.capture.AbortException;

import java.io.IOException;

public abstract class AbstractCaptureConfiguration extends AbstractConfiguration {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(AbstractCaptureConfiguration.class);

    protected static final String prefixKey = "filename-prefix";
    protected static final String temperatureKey = "temperature";
    protected static final String toleranceKey = "temperature-tolerance";
    protected static final String exposureKey = "exposure";

    @JsonProperty(prefixKey)
    protected String prefix;

    @JsonProperty(temperatureKey)
    protected Double temperature;

    @JsonProperty(toleranceKey)
    protected Double tolerance;

    @JsonProperty(exposureKey)
    protected Double exposure;

    @JsonCreator
    public AbstractCaptureConfiguration(@JsonProperty(prefixKey) String prefix,
                                        @JsonProperty(temperatureKey) Double temperature,
                                        @JsonProperty(toleranceKey) Double tolerance,
                                        @JsonProperty(exposureKey) Double exposure) {
        this.prefix = prefix;
        this.temperature = temperature;
        this.tolerance = tolerance;
        this.exposure = exposure;
    }

    public AbstractCaptureConfiguration() {
        prefix = null;
        temperature = null;
        tolerance = null;
        exposure = null;
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
    }

    @Override
    public void ask() throws IOException, AbortException {
        setPrefix("Filename", prefix);
        setTemperature("Temperature", temperature);
        setTolerance("Temperature Tolerance", tolerance);
        setExposure("Exposure", exposure);
    }
}

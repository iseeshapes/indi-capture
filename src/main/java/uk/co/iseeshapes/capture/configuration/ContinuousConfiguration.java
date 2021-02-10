package uk.co.iseeshapes.capture.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.iseeshapes.capture.AbortException;

import java.io.IOException;

public class ContinuousConfiguration extends AbstractCaptureConfiguration {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(ContinuousConfiguration.class);

    @JsonIgnore
    private boolean saveFile;

    @JsonIgnore
    private boolean continuous;

    public ContinuousConfiguration() {
        saveFile = false;
        continuous = false;
    }

    @JsonCreator
    public ContinuousConfiguration(@JsonProperty(prefixKey) String prefix,
                                   @JsonProperty(filterKey) String filter,
                                   @JsonProperty(temperatureKey) Double temperature,
                                   @JsonProperty(toleranceKey) Double tolerance,
                                   @JsonProperty(exposureKey) Double exposure,
                                   @JsonProperty(pingKey) Boolean ping) {
        super(prefix, filter, temperature, tolerance, exposure, ping);

        saveFile = false;
        continuous = false;
    }

    @JsonIgnore
    public boolean isSaveFile() {
        return saveFile;
    }

    @JsonIgnore
    public boolean isContinuous() {
        return continuous;
    }

    public void setPreview ()  throws IOException, AbortException {
        saveFile = readValue("Save File [y/n]", true, this::parseBoolean);
    }

    public void setContinuous ()  throws IOException, AbortException {
        continuous = readValue("Continuous [y/n]", true, this::parseBoolean);
    }

    @Override
    public void fillInBlanks() throws IOException, AbortException {
        setPreview();

        if (saveFile && prefix == null) {
            setPrefix("Missing Filename Prefix", null);
        }
        if (saveFile && filter == null) {
            setFilter("Missing Filter", null);
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

        setContinuous();
    }

    @Override
    public void ask() throws IOException, AbortException {
        setPreview();

        if (saveFile) {
            setPrefix("Filename", prefix);
            setFilter("Filter", filter);
        }
        setTemperature("Temperature", temperature);
        setTolerance("Temperature Tolerance", tolerance);
        setExposure("Exposure", exposure);

        setContinuous();
    }
}

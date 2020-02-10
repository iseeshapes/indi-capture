package uk.co.iseeshapes.capture.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.iseeshapes.capture.AbortException;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContinuousConfiguration extends AbstractCaptureConfiguration {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(ContinuousConfiguration.class);

    private static final Pattern yesPattern = Pattern.compile("[yY]|[yY]es");
    private static final Pattern noPattern = Pattern.compile("[nN]|[nN]o");

    @JsonIgnore
    private Boolean saveFile;

    @JsonIgnore
    private Boolean continuous;

    public ContinuousConfiguration() {
        saveFile = false;
        continuous = false;
    }

    @JsonCreator
    public ContinuousConfiguration(@JsonProperty(prefixKey) String prefix,
                                   @JsonProperty(temperatureKey) Double temperature,
                                   @JsonProperty(toleranceKey) Double tolerance,
                                   @JsonProperty(exposureKey) Double exposure) {
        super(prefix, temperature, tolerance, exposure);

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
        saveFile = readValue("Save File [y/n]", false, (String rawMessage) -> {
            Matcher matcher = yesPattern.matcher(rawMessage);
            if (matcher.matches()) {
                return true;
            }
            matcher = noPattern.matcher(rawMessage);
            if (matcher.matches()) {
                return false;
            }
            return null;
        });
    }

    public void setContinuous ()  throws IOException, AbortException {
        continuous = readValue("Continuous [y/n]", true, (String rawMessage) -> {
            Matcher matcher = yesPattern.matcher(rawMessage);
            if (matcher.matches()) {
                return true;
            }
            matcher = noPattern.matcher(rawMessage);
            if (matcher.matches()) {
                return false;
            }
            return null;
        });
    }

    @Override
    public void fillInBlanks() throws IOException, AbortException {
        setPreview();

        if (saveFile && prefix == null) {
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

        setContinuous();
    }

    @Override
    public void ask() throws IOException, AbortException {
        setPreview();

        if (saveFile) {
            setPrefix("Filename", prefix);
        }
        setTemperature("Temperature", temperature);
        setTolerance("Temperature Tolerance", tolerance);
        setExposure("Exposure", exposure);

        setContinuous();
    }
}

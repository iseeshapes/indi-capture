package uk.co.iseeshapes.capture.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.iseeshapes.capture.AbortException;

import java.io.IOException;

public class CaptureConfiguration extends AbstractCaptureConfiguration {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(CaptureConfiguration.class);

    private static final String noOfFramesKey = "no-of-frames";

    @JsonProperty(noOfFramesKey)
    private Integer noOfFrames;

    public CaptureConfiguration () {
        noOfFrames = null;
    }

    @JsonCreator
    public CaptureConfiguration(@JsonProperty(prefixKey) String prefix,
                                @JsonProperty(temperatureKey) Double temperature,
                                @JsonProperty(toleranceKey) Double tolerance,
                                @JsonProperty(exposureKey) Double exposure,
                                @JsonProperty(noOfFramesKey) Integer noOfFrames) {
        super(prefix, temperature, tolerance, exposure);

        this.noOfFrames = noOfFrames;
    }

    @JsonIgnore
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
        super.fillInBlanks();

        if (/*!isPreview() &&*/ noOfFrames == null) {
            setNoOfFrames("Missing No Of Frames", null);
        }
    }

    @Override
    public void ask() throws IOException, AbortException {
        super.ask();

        //if (!isPreview()) {
            setNoOfFrames("No of frames", noOfFrames);
        //}
    }
}

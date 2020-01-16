package uk.co.iseeshapes.capture.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.iseeshapes.capture.AbortException;

import java.io.IOException;

public class ApplicationConfiguration extends AbstractConfiguration{
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(ApplicationConfiguration.class);

    private static final String lineLengthKey = "line-length";
    private static final String urlKey = "server-url";
    private static final String portKey = "port";

    @JsonProperty(lineLengthKey)
    private Integer lineLength;

    @JsonProperty(urlKey)
    private String url;

    @JsonProperty(portKey)
    private Integer port;

    @JsonCreator
    public ApplicationConfiguration(@JsonProperty(lineLengthKey) Integer lineLength,
                                    @JsonProperty(urlKey) String url,
                                    @JsonProperty(portKey) Integer port) {
        this.lineLength = lineLength;
        this.url = url;
        this.port = port;
    }

    public ApplicationConfiguration () {
        lineLength = null;
        url = null;
        port = null;
    }

    @JsonIgnore
    public int getLineLength() {
        return lineLength;
    }

    public void setLineLength (String message, Integer defaultValue) throws IOException, AbortException {
        lineLength = readValue(message, defaultValue, (String rawValue) -> {
            try {
                int value = Integer.parseInt(rawValue);
                if (value < 30) {
                    return null;
                }
                return value;
            } catch (NumberFormatException e) {
                return null;
            }
        });
    }

    @JsonIgnore
    public String getUrl() {
        return url;
    }

    public void setUrl (String message, String defaultValue) throws IOException, AbortException {
        url = readValue(message, defaultValue, (String rawValue) -> rawValue);
    }

    @JsonIgnore
    public int getPort() {
        return port;
    }

    public void setPort (String message, Integer defaultValue) throws IOException, AbortException {
        port = readValue(message, defaultValue, (String rawValue) -> {
            try {
                int value = Integer.parseInt(rawValue);
                if (value < 0) {
                    return null;
                }
                return value;
            } catch (NumberFormatException e) {
                return null;
            }
        });
    }

    @Override
    public void fillInBlanks() throws IOException, AbortException {
        if (lineLength == null) {
            setLineLength("Missing Line Length", 128);
        }
        if (url == null) {
            setUrl("Missing Server URL", "localhost");
        }
        if (port == null) {
            setPort("Missing Server Port", 2000);
        }
    }

    @Override
    public void ask() throws IOException, AbortException {
        setLineLength("Line Length", lineLength);
        setUrl("Server URL", url);
        setPort("Server Port", port);
    }
}

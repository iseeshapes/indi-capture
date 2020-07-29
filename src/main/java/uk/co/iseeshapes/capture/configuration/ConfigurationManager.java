package uk.co.iseeshapes.capture.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.iseeshapes.capture.AbortException;

import java.io.File;
import java.io.IOException;

public class ConfigurationManager {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(ConfigurationManager.class);

    private final ObjectMapper objectMapper;

    public ConfigurationManager (ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public File findArgument (String[] args, String shortArgument, String longArgument) throws AbortException {
        File file;
        for (int i=0;i<args.length;i++) {
            if (shortArgument.equals(args[i]) || longArgument.equals(args[i])) {
                if (i == args.length -1) {
                    throw new AbortException("No file name after " + shortArgument + " / " + longArgument);
                }
                file = new File(args[i+1]);
                if (!file.exists()) {
                    throw new AbortException("Cannot find configuration file \"" + args[i+1] + "\" width argument \""
                            + args[i] + "\"");
                }
                return file;
            }
        }
        return null;
    }

    public File findEnvironmentVariable (String environmentVariable) {
        if (environmentVariable != null) {
            String filename = System.getenv(environmentVariable);
            if (filename != null) {
                File file = new File(filename);
                if (!file.exists()) {
                    log.info ("Cannot find configuration file \"{}\" with environment variable \"{}\"", filename,
                            environmentVariable);
                    return null;
                }
                return file;
            }
        }
        return null;
    }

    private File findLocalFile (String localFilename) {
        File file = new File(localFilename);
        if (!file.exists()) {
            log.info ("Cannot find local configuration file - {}", localFilename);
            return null;
        }
        return file;
    }

    public <T> T findConfiguration (String[] args, String shortArgument, String longArgument,
                                    String environmentVariable, String localFilename, Class<T> clazz)
            throws AbortException {
        File file = findArgument(args, shortArgument, longArgument);
        if (file == null) {
            file = findLocalFile(localFilename);
        }
        if (file == null) {
            file = findEnvironmentVariable(environmentVariable);
        }
        if (file == null) {
            return null;
        }
        try {
            return objectMapper.readValue(file, clazz);
        } catch (IOException e) {
            throw new AbortException("Failed read configuration file: " + file.getName(), e);
        }
    }

    public void writeConfiguration (String localFilename, Object configuration) throws IOException {
        objectMapper.writeValue(new File (localFilename), configuration);
    }
}

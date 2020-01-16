package uk.co.iseeshapes.capture.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class ConfigurationManager {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(ConfigurationManager.class);

    private ObjectMapper objectMapper;

    public ConfigurationManager (ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> T findConfiguration (String[] args, String shortArgument, String longArgument,
                                            String environmentVariable, String localFilename, Class<T> clazz) {
        File file = null;
        String filename = null;
        for (int i=0;i<args.length;i++) {
            if (shortArgument.equals(args[i]) || longArgument.equals(args[i])) {
                if (i == args.length -1) {
                    log.error("No file name after {} / {}", shortArgument, longArgument);
                    return null;
                }
                filename = args[i+1];
                break;
            }
        }
        if (environmentVariable != null && filename == null) {
            filename = System.getenv(environmentVariable);
            if (filename != null) {
                file = new File(filename);
                if (!file.exists()) {
                    log.error("Cannot find configuration file - {}", filename);
                    return null;
                }
            }
        }
        if (filename == null) {
            file = new File(localFilename);
            if (!file.exists()) {
                return null;
            }
        }
        T configuration;
        try {
            configuration = objectMapper.readValue(file, clazz);
        } catch (IOException e) {
            log.error("Failed read configuration file: {}\nError: {}", filename, e.getMessage());
            return null;
        }
        return configuration;
    }

    public void writeConfiguration (String localFilename, Object configuration) throws IOException {
        objectMapper.writeValue(new File (localFilename), configuration);
    }
}

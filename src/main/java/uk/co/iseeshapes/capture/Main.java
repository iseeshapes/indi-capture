package uk.co.iseeshapes.capture;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.iseeshapes.capture.client.Client;
import uk.co.iseeshapes.capture.configuration.ApplicationConfiguration;
import uk.co.iseeshapes.capture.configuration.CaptureConfiguration;
import uk.co.iseeshapes.capture.device.Camera;
import uk.co.iseeshapes.capture.script.CaptureScript;

import java.io.*;

public class Main {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private static final String shortApplicationArgument = "-a";
    private static final String longApplicationArgument = "--application";
    private static final String environmentApplicationVariable = "CAPTURE_APPLICATION_CONFIGURATION";
    private static final String localApplicationFilename = "application.json";

    private static final String shortCameraArgument = "-c";
    private static final String longCameraArgument = "--capture";
    private static final String environmentCameraVariable = null;
    private static final String localCameraFilename = "camera.json";

    private static final String shortCaptureArgument = "-c";
    private static final String longCaptureArgument = "--capture";
    private static final String environmentCaptureVariable = null;
    private static final String localCaptureFilename = "capture.json";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static void printUsage (String message) {
        String printMessage = message +
                "\n capture [options]" +
                "options:" +
                "\n" + shortApplicationArgument + " | " + longApplicationArgument + ": Connection file";
        log.error(printMessage);
    }

    private static <T> T findConfiguration (String[] args, String shortArgument, String longArgument,
                                            String environmentVariable, String localFilename, Class<T> clazz) {
        File file = null;
        String filename = null;
        for (int i=0;i<args.length;i++) {
            if (shortArgument.equals(args[i]) || longArgument.equals(args[i])) {
                if (i == args.length -1) {
                    printUsage("No file name after " + shortArgument + " / " + longArgument);
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
                    printUsage("Cannot find connection configuration file - " + filename);
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
            printUsage("Failed read connection configuration file:" + filename + "\nError: " + e.getMessage());
            return null;
        }
        return configuration;
    }

    public static void main (String[] args) {
        Client client = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            ApplicationConfiguration applicationConfiguration = findConfiguration(args, shortApplicationArgument,
                    longApplicationArgument, environmentApplicationVariable, localApplicationFilename,
                    ApplicationConfiguration.class);
            if (applicationConfiguration == null) {
                applicationConfiguration = new ApplicationConfiguration();
            }
            applicationConfiguration.setStreams(System.out, reader);
            applicationConfiguration.fillInBlanks();
            objectMapper.writeValue(new File(localApplicationFilename), applicationConfiguration);

            client = new Client(applicationConfiguration);

            Camera camera = findConfiguration(args, shortCameraArgument, longCameraArgument, environmentCameraVariable,
                    localCameraFilename, Camera.class);
            if (camera == null) {
                camera = new Camera();
            }
            camera.setStreams(System.out, reader, client);
            camera.assignDevice();
            objectMapper.writeValue(new File(localCameraFilename), camera);

            CaptureConfiguration captureConfiguration = findConfiguration(args, shortCaptureArgument,
                    longCaptureArgument, environmentCaptureVariable, localCaptureFilename,
                    CaptureConfiguration.class);
            if (captureConfiguration == null) {
                captureConfiguration = new CaptureConfiguration();
            }
            captureConfiguration.setStreams(System.out, reader);
            captureConfiguration.ask();
            objectMapper.writeValue(new File(localCaptureFilename), captureConfiguration);

            CaptureScript captureScript = new CaptureScript(applicationConfiguration, camera, System.out);
            captureScript.run(captureConfiguration);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AbortException e) {
            System.out.println("Aborted Capture: " + e.getMessage());
            e.printStackTrace();
        }

        if (client != null) {
            try {
                //client.printValues();
                client.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

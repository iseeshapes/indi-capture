package uk.co.iseeshapes.capture.script;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.iseeshapes.capture.AbortException;
import uk.co.iseeshapes.capture.configuration.*;
import uk.co.iseeshapes.capture.controller.DeviceConnectionController;
import uk.co.iseeshapes.capture.device.UploadMode;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class ContinuousCapture extends AbstractCaptureScript {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(ContinuousCapture.class);

    private static final String shortCaptureArgument = "-n";
    private static final String longCaptureArgument = "--continuous";
    private static final String environmentCaptureVariable = null;
    private static final String localCaptureFilename = "continuous.json";

    private ContinuousConfiguration captureConfiguration;

    public ContinuousCapture(ConfigurationManager configurationManager) {
        super(configurationManager);
    }

    @Override
    public void initialise(String[] args, BufferedReader reader, PrintStream out) throws IOException, AbortException {
        super.initialise(args, reader, out);

        captureConfiguration = configurationManager.findConfiguration(args, shortCaptureArgument,
                longCaptureArgument, environmentCaptureVariable, localCaptureFilename, ContinuousConfiguration.class);
        if (captureConfiguration == null) {
            captureConfiguration = new ContinuousConfiguration();
        }
        captureConfiguration.setStreams(out, reader);
        captureConfiguration.ask();
        configurationManager.writeConfiguration(localCaptureFilename, captureConfiguration);
    }

    @Override
    protected AbstractCaptureConfiguration getAbstractCaptureConfiguration() {
        return captureConfiguration;
    }

    @Override
    public void run() throws IOException, AbortException {
        DeviceConnectionController deviceConnectionController = new DeviceConnectionController(indiConnection,
                indiServerConnection, camera.getDeviceName());
        if (!deviceConnectionController.isConnected()) {
            deviceConnectionController.connect();
        }

        ccdTemperatureController.setTemperature(captureConfiguration.getTemperature(),
                captureConfiguration.getTolerance());

        File directory = new File(System.getProperty("user.dir"));

        int imageNumber = 1;
        while (true) {
            if (captureConfiguration.getExposure() > 2.0) {
                ccdUploadController.sendUploadMode(UploadMode.local);
                ccdExposureController.capture(0.25, false);
            }
            ccdUploadController.sendUploadMode(UploadMode.client);
            if (captureConfiguration.isSaveFile()) {
                File file = createFilename(directory, captureConfiguration.getPrefix(), captureConfiguration.getExposure());
                ccdExposureController.captureAndDownload(captureConfiguration.getExposure(), file, imageNumber);
                imageNumber++;
            } else {
                ccdExposureController.capture(captureConfiguration.getExposure(), true);
            }
            if (!captureConfiguration.isContinuous()) {
                break;
            }
        }
    }
}

package uk.co.iseeshapes.capture.script;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.iseeshapes.capture.AbortException;
import uk.co.iseeshapes.capture.configuration.AbstractCaptureConfiguration;
import uk.co.iseeshapes.capture.configuration.CaptureConfiguration;
import uk.co.iseeshapes.capture.configuration.ConfigurationManager;
import uk.co.iseeshapes.capture.controller.DeviceConnectionController;
import uk.co.iseeshapes.capture.device.UploadMode;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class Capture extends AbstractCaptureScript{
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(Capture.class);

    private static final String shortCaptureArgument = "-c";
    private static final String longCaptureArgument = "--capture";
    private static final String environmentCaptureVariable = null;
    private static final String localCaptureFilename = "capture.json";

    private CaptureConfiguration captureConfiguration;

    public Capture(ConfigurationManager configurationManager) {
        super(configurationManager);
    }

    @Override
    public void initialise(String[] args, BufferedReader reader, PrintStream out) throws IOException, AbortException {
        super.initialise(args, reader, out);

        captureConfiguration = configurationManager.findConfiguration(args, shortCaptureArgument,
                longCaptureArgument, environmentCaptureVariable, localCaptureFilename, CaptureConfiguration.class);
        if (captureConfiguration == null) {
            captureConfiguration = new CaptureConfiguration();
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
    public void run () throws AbortException, IOException {
        DeviceConnectionController deviceConnectionController = new DeviceConnectionController(indiConnection,
                indiServerConnection, camera.getDeviceName());
        if (!deviceConnectionController.isConnected()) {
            deviceConnectionController.connect();
        }
        ccdTemperatureController.setTemperature(captureConfiguration.getTemperature(),
                captureConfiguration.getTolerance());

        File directory = new File(System.getProperty("user.dir"));

        for (int i = 0; i < captureConfiguration.getNoOfFrames(); i++) {
            if (captureConfiguration.getExposure() > 2.0) {
                ccdUploadController.sendUploadMode(UploadMode.local);
                ccdExposureController.capture(0.25, false);
            }
            File file = createFilename(directory, captureConfiguration.getPrefix(), captureConfiguration.getExposure());
            ccdUploadController.sendUploadMode(UploadMode.client);
            ccdExposureController.captureAndDownload(captureConfiguration.getExposure(), file, i+1,
                    captureConfiguration.getNoOfFrames());
        }
    }
}

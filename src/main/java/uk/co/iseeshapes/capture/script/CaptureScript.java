package uk.co.iseeshapes.capture.script;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.iseeshapes.capture.AbortException;
import uk.co.iseeshapes.capture.configuration.CaptureConfiguration;
import uk.co.iseeshapes.capture.configuration.ConfigurationManager;
import uk.co.iseeshapes.capture.configuration.DeviceConfiguration;
import uk.co.iseeshapes.capture.controller.CCDExposureController;
import uk.co.iseeshapes.capture.controller.CCDTemperatureController;
import uk.co.iseeshapes.capture.controller.CCDUploadController;
import uk.co.iseeshapes.capture.controller.DeviceConnectionController;
import uk.co.iseeshapes.capture.device.UploadMode;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CaptureScript extends AbstractScript {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(CaptureScript.class);

    private static final String shortCameraArgument = "-k";
    private static final String longCameraArgument = "--camera";
    private static final String environmentCameraVariable = null;
    private static final String localCameraFilename = "camera.json";

    private static final String shortCaptureArgument = "-c";
    private static final String longCaptureArgument = "--capture";
    private static final String environmentCaptureVariable = null;
    private static final String localCaptureFilename = "capture.json";

    private static final Pattern filenamePattern = Pattern.compile("(.*)-(\\d+)s([+-]\\d+)c-(\\d+)-(\\d{8}-\\d{6})\\.fits");

    private final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-hhmmss");

    private DeviceConfiguration camera;
    private CaptureConfiguration captureConfiguration;

    public CaptureScript(ConfigurationManager configurationManager) {
        super(configurationManager);
    }

    @Override
    public void initialise(String[] args, BufferedReader reader, PrintStream out) throws IOException, AbortException {
        super.initialise(args, reader, out);

        camera = configurationManager.findConfiguration(args, shortCameraArgument,
                longCameraArgument, environmentCameraVariable, localCameraFilename, DeviceConfiguration.class);
        if (camera == null) {
            camera = new DeviceConfiguration("Camera");
        }
        camera.setStreams(out, reader);
        camera.assignDevice(indiServerConnection.getDeviceNames());
        configurationManager.writeConfiguration(localCameraFilename, camera);

        captureConfiguration = configurationManager.findConfiguration(args, shortCaptureArgument,
                longCaptureArgument, environmentCaptureVariable, localCaptureFilename,
                CaptureConfiguration.class);
        if (captureConfiguration == null) {
            captureConfiguration = new CaptureConfiguration();
        }
        captureConfiguration.setStreams(out, reader);
        captureConfiguration.ask();
        configurationManager.writeConfiguration(localCaptureFilename, captureConfiguration);
    }

    private File createFilename (File directory, CaptureConfiguration captureConfiguration) {
        int frameNumber = 0;

        String prefix = captureConfiguration.getPrefix();
        int exposure = (int)Math.ceil(captureConfiguration.getExposure());

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                Matcher matcher = filenamePattern.matcher(file.getName());
                if (matcher.matches()) {
                    try {
                        int duration = Integer.parseInt(matcher.group(2));
                        if (!prefix.equals(matcher.group(1)) || duration != exposure) {
                            continue;
                        }
                        int fileFrameNumber = Integer.parseInt(matcher.group(4));
                        if (fileFrameNumber > frameNumber) {
                            frameNumber = fileFrameNumber;
                        }
                    } catch (NumberFormatException e) {
                        //do nothing
                    }
                }
            }
        }
        frameNumber++;

        String filename = String.format("%s-%ds%+02dc-%03d-%s.fits", prefix, exposure,
                (int)captureConfiguration.getTemperature(), frameNumber, format.format(new Date()));

        return new File(directory, filename);
    }

    private void captureImage (File directory, CaptureConfiguration captureConfiguration, int imageNumber)
            throws AbortException, IOException {

        File file = createFilename(directory, captureConfiguration);
        CCDExposureController ccdExposureController;
        CCDUploadController ccdUploadController = new CCDUploadController(indiConnection, indiServerConnection,
                camera.getDeviceName());
        if (captureConfiguration.getExposure() > 2.0) {
            ccdUploadController.sendUploadMode(UploadMode.local);
            ccdExposureController = new CCDExposureController(indiConnection, indiServerConnection,
                    out, applicationConfiguration, 0.25, 0, file, imageNumber,
                    camera.getDeviceName(), UploadMode.local, true);
            ccdExposureController.start();
        }
        ccdUploadController.sendUploadMode(UploadMode.client);
        ccdExposureController = new CCDExposureController(indiConnection, indiServerConnection,
                out, applicationConfiguration, captureConfiguration.getExposure(), captureConfiguration.getNoOfFrames(),
                file, imageNumber, camera.getDeviceName(), UploadMode.client, false);
        ccdExposureController.start();
    }

    @Override
    public void run () throws AbortException, IOException {
        DeviceConnectionController deviceConnectionController = new DeviceConnectionController(indiConnection,
                indiServerConnection, camera.getDeviceName());
        if (!deviceConnectionController.isConnected()) {
            deviceConnectionController.connect();
        }
        File directory = new File(System.getProperty("user.dir"));

        CCDTemperatureController ccdTemperatureController = new CCDTemperatureController(indiServerConnection,
                indiConnection, applicationConfiguration, out, camera.getDeviceName(),
                captureConfiguration.getTemperature(), captureConfiguration.getTolerance());
        ccdTemperatureController.start();

        for (int i = 0; i < captureConfiguration.getNoOfFrames(); i++) {
            captureImage(directory, captureConfiguration, i + 1);
        }
    }
}

package uk.co.iseeshapes.capture.script;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.iseeshapes.capture.AbortException;
import uk.co.iseeshapes.capture.configuration.AbstractCaptureConfiguration;
import uk.co.iseeshapes.capture.configuration.ConfigurationManager;
import uk.co.iseeshapes.capture.configuration.DeviceConfiguration;
import uk.co.iseeshapes.capture.controller.CCDExposureController;
import uk.co.iseeshapes.capture.controller.CCDTemperatureController;
import uk.co.iseeshapes.capture.controller.CCDUploadController;
import uk.co.iseeshapes.capture.device.UploadMode;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractCaptureScript extends AbstractScript {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(AbstractCaptureScript.class);

    private static final String shortCameraArgument = "-k";
    private static final String longCameraArgument = "--camera";
    private static final String environmentCameraVariable = null;
    private static final String localCameraFilename = "camera.json";

    private static final Pattern filenamePattern = Pattern.compile("(.*)-(\\d+)s-(\\d+)-(\\d{8}T\\d{6})\\.fits");

    private final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd'T'hhmmss");

    protected CCDTemperatureController ccdTemperatureController;
    protected CCDExposureController ccdExposureController;
    protected CCDUploadController ccdUploadController;

    protected DeviceConfiguration camera;

    public AbstractCaptureScript(ConfigurationManager configurationManager) {
        super(configurationManager);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    protected abstract AbstractCaptureConfiguration getAbstractCaptureConfiguration();

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

        ccdUploadController = new CCDUploadController(indiConnection, indiServerConnection, camera.getDeviceName());

        ccdExposureController = new CCDExposureController(indiConnection, indiServerConnection, out,
                camera.getDeviceName(), applicationConfiguration.getLineLength());

        ccdTemperatureController = new CCDTemperatureController(indiServerConnection, indiConnection, out,
                camera.getDeviceName(), applicationConfiguration.getLineLength());
    }

    protected File createFilename (File directory, String prefix, double exposure) {
        int roundedExposure = (int)Math.ceil(exposure);
        int frameNumber = 0;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                Matcher matcher = filenamePattern.matcher(file.getName());
                if (matcher.matches()) {
                    try {
                        int duration = Integer.parseInt(matcher.group(2));
                        if (!prefix.equals(matcher.group(1)) || duration != roundedExposure) {
                            continue;
                        }
                        int fileFrameNumber = Integer.parseInt(matcher.group(3));
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

        String filename = String.format("%s-%ds-%03d-%s.fits", prefix, roundedExposure, frameNumber, format.format(new Date()));

        return new File(directory, filename);
    }

    protected void captureImage (File file, double exposure) throws AbortException, IOException {
    }
}

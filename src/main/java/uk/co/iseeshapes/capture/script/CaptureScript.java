package uk.co.iseeshapes.capture.script;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.iseeshapes.capture.AbortException;
import uk.co.iseeshapes.capture.configuration.ApplicationConfiguration;
import uk.co.iseeshapes.capture.configuration.CaptureConfiguration;
import uk.co.iseeshapes.capture.device.Camera;
import uk.co.iseeshapes.capture.device.UploadMode;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CaptureScript {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(CaptureScript.class);

    private static final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-hhmmss");
    private static final double timeout = 10.0;
    private static final long exposureSleepTime = 500;
    private static final long downloadSleepTime = 100;

    private static Pattern filenamePattern = Pattern.compile("(.*)-(\\d+)s([+-]\\d+)c-(\\d+)-(\\d{8}-\\d{6})\\.fits");

    private ApplicationConfiguration applicationConfiguration;
    private Camera camera;
    private PrintStream out;

    public CaptureScript(ApplicationConfiguration applicationConfiguration, Camera camera, PrintStream out) {
        this.applicationConfiguration = applicationConfiguration;
        this.camera = camera;
        this.out = out;
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
                    if (!prefix.equals(matcher.group(1))) {
                        continue;
                    }
                    try {
                        int duration = Integer.parseInt(matcher.group(2));
                        if (duration != exposure) {
                            continue;
                        }
                        int fileFrameNumber = Integer.parseInt(matcher.group(4));
                        if (fileFrameNumber > frameNumber) {
                            frameNumber = fileFrameNumber;
                        }
                    } catch (NumberFormatException e) {
                        continue;
                    }
                }
            }
        }
        frameNumber++;

        String filename = String.format("%s-%ds%+02dc-%03d-%s.fits", prefix, exposure,
                (int)captureConfiguration.getTemperature(), frameNumber, format.format(new Date()));

        return new File(directory, filename);
    }

    private int calculateExposureSeconds (double exposure) {
        return (int)Math.ceil(exposure);
    }

    private long calculateExposureMilliseconds (double exposure) {
        return calculateExposureSeconds(exposure) * 1000L;
    }

    private void updateConsole (File file, long exposure, long exposureMilliseconds,
                                int noOfFrames, int imageNumber, long time) {
        String lineStart = String.format("\r%3d of %3d => %s ", imageNumber, noOfFrames, file.getName());

        int percent = (int)Math.round((double)(time * 100)/exposureMilliseconds);
        String lineEnd = String.format(" %3d/%3d (%2d%%)", time / 1000, exposure, percent);

        out.print(lineStart);

        int steps = applicationConfiguration.getLineLength() - lineStart.length() - lineEnd.length();
        long step = exposureMilliseconds / steps;
        long position = 0;
        while (position < time) {
            out.print("=");
            position += step;
        }
        if (position < exposureMilliseconds) {
            out.print(">");
            position += step;
        }
        while (position < exposureMilliseconds) {
            out.print(" ");
            position += step;
        }
        out.print(lineEnd);
    }

    private void clearLine () {
        out.print("\r");
        for (int i=0;i<applicationConfiguration.getLineLength();i++) {
            out.print(" ");
        }
    }

    private void downloadImage (int imageNumber, CaptureConfiguration captureConfiguration, File file) throws AbortException {
        clearLine();
        long start = new Date().getTime();
        double time;
        while (true) {
            time = (double)(new Date().getTime() - start) / 1000;
            out.printf("\rDownloading %s (%5.2fs)", file.getName(), time);
            if (file.exists()) {
                break;
            }
            if (time > timeout) {
                //camera.getImageData();
                throw new AbortException(String.format("Failed to download %s in %2.1f", file.getName(), timeout));
            }
            try {
                Thread.sleep(downloadSleepTime);
            } catch (InterruptedException e) {
                //do nothing
            }
        }
        clearLine();
        out.printf("\rCompleted - %d of %d - %s%n", imageNumber, captureConfiguration.getNoOfFrames(), file.getName());
    }

    private void captureImage (File directory, CaptureConfiguration captureConfiguration, int imageNumber)
            throws AbortException {
        long exposure = calculateExposureSeconds(captureConfiguration.getExposure());
        long exposureMilliseconds = calculateExposureMilliseconds(captureConfiguration.getExposure());
        long exposureRemaining;

        File file = createFilename(directory, captureConfiguration);
        camera.setFile(file);
        camera.setExposure(captureConfiguration.getExposure(), true);

        while(true) {
            exposureRemaining = calculateExposureMilliseconds(camera.getExposure());
            log.info ("Remaining: {}", exposureRemaining);
            updateConsole(file, exposure, exposureMilliseconds,
                    captureConfiguration.getNoOfFrames(), imageNumber, exposureMilliseconds - exposureRemaining);
            if (exposureRemaining - exposureSleepTime > 0) {
                try {
                    Thread.sleep(exposureSleepTime);
                } catch (InterruptedException e) {
                    break;
                }
            } else {
                break;
            }
        }
        //camera.getImageData();
        downloadImage(imageNumber, captureConfiguration, file);
    }

    public void run (CaptureConfiguration captureConfiguration) throws AbortException {
        if (!camera.isConnected()) {
            camera.setConnected(true);
        }
        camera.setUploadMode(UploadMode.local);
        File directory = new File(System.getProperty("user.dir"));
        //File directory = new File("/home/eliot/indi-test");
        //camera.setTemperature();
        for (int i = 0; i < captureConfiguration.getNoOfFrames(); i++) {
            captureImage(directory, captureConfiguration, i + 1);
        }
    }
}

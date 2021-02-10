package uk.co.iseeshapes.capture.script;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.iseeshapes.capture.AbortException;
import uk.co.iseeshapes.capture.audio.CaptureSounds;
import uk.co.iseeshapes.capture.configuration.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

public class ContinuousCapture extends AbstractCaptureScript {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(ContinuousCapture.class);

    private static final String shortCaptureArgument = "-n";
    private static final String longCaptureArgument = "--continuous";
    private static final String environmentCaptureVariable = null;
    private static final String localCaptureFilename = "continuous.json";

    public ContinuousCapture(ConfigurationManager configurationManager, CaptureSounds captureSounds) {
        super(configurationManager, captureSounds);
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
    public void captureImages() throws IOException, AbortException {
        int imageNumber = 1;
        while (true) {
            captureImage(imageNumber, imageNumber);
            if (!((ContinuousConfiguration)captureConfiguration).isContinuous()) {
                break;
            }
            imageNumber++;
        }
    }
}

package uk.co.iseeshapes.capture.script;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.iseeshapes.capture.AbortException;
import uk.co.iseeshapes.capture.audio.CaptureSounds;
import uk.co.iseeshapes.capture.configuration.AbstractCaptureConfiguration;
import uk.co.iseeshapes.capture.configuration.CaptureConfiguration;
import uk.co.iseeshapes.capture.configuration.ConfigurationManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

public class Capture extends AbstractCaptureScript {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(Capture.class);

    private static final String shortCaptureArgument = "-c";
    private static final String longCaptureArgument = "--capture";
    private static final String environmentCaptureVariable = null;
    private static final String localCaptureFilename = "capture.json";


    public Capture(ConfigurationManager configurationManager, CaptureSounds captureSounds) {
        super(configurationManager, captureSounds);
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
    public void captureImages () throws IOException, AbortException {
        CaptureConfiguration config = (CaptureConfiguration)captureConfiguration;
        for (int i = 0; i < config.getNoOfFrames(); i++) {
            captureImage(i+1, config.getNoOfFrames());
        }
    }
}

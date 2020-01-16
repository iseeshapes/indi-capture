package uk.co.iseeshapes.capture.script;

import org.indilib.i4j.client.INDIServerConnection;
import org.indilib.i4j.protocol.api.INDIConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.iseeshapes.capture.AbortException;
import uk.co.iseeshapes.capture.configuration.ApplicationConfiguration;
import uk.co.iseeshapes.capture.configuration.ConfigurationManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;

public abstract class AbstractScript {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(AbstractScript.class);

    private static final String shortApplicationArgument = "-a";
    private static final String longApplicationArgument = "--application";
    private static final String environmentApplicationVariable = "CAPTURE_APPLICATION_CONFIGURATION";
    private static final String localApplicationFilename = "application.json";

    protected ConfigurationManager configurationManager;

    protected INDIConnection indiConnection;
    protected INDIServerConnection indiServerConnection;
    protected ApplicationConfiguration applicationConfiguration;
    protected PrintStream out;

    public AbstractScript(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    public void initialise (String[] args, BufferedReader reader, PrintStream out) throws IOException, AbortException {
        applicationConfiguration = configurationManager.findConfiguration(args, shortApplicationArgument, longApplicationArgument,
                environmentApplicationVariable, localApplicationFilename, ApplicationConfiguration.class);
        if (applicationConfiguration == null) {
            applicationConfiguration = new ApplicationConfiguration();
        }
        applicationConfiguration.setStreams(out, reader);
        applicationConfiguration.fillInBlanks();

        configurationManager.writeConfiguration(localApplicationFilename, applicationConfiguration);

        this.out = out;

        URL indiUrl = new URL("indi", applicationConfiguration.getUrl(), applicationConfiguration.getPort(), "/");
        indiConnection = (INDIConnection)indiUrl.openConnection();

        indiServerConnection = new INDIServerConnection(indiConnection);
        indiServerConnection.connect();
        indiServerConnection.askForDevices();

        try {
            Thread.sleep(500L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AbortException("Wait for device information interupted", e);
        }
    }

    public abstract void run () throws IOException, AbortException;

    public void finish () {
        try {
            indiConnection.close();
        } catch (IOException e) {
            log.debug("Error closing connection", e);
        }
    }
}

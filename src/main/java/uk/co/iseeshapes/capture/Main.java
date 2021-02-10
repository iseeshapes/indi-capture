package uk.co.iseeshapes.capture;

import org.indilib.i4j.protocol.url.INDIURLStreamHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import uk.co.iseeshapes.capture.script.AbstractScript;
import uk.co.iseeshapes.capture.spring.SpringConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main (String[] args) {
        INDIURLStreamHandlerFactory.init();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(SpringConfiguration.class);
        AbstractScript script = applicationContext.getBean(args[0], AbstractScript.class);

        try {
            script.initialise(args, reader, System.out);
            script.run();
        } catch (AbortException | IOException e) {
            log.error("Script Failed", e);
        }
        System.exit(0);
    }
}

package uk.co.iseeshapes.capture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbortException extends Exception {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(AbortException.class);

    public AbortException() {
    }

    public AbortException(String message) {
        super(message);
    }

    public AbortException(String message, Throwable cause) {
        super(message, cause);
    }
}

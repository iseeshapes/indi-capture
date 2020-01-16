package uk.co.iseeshapes.capture.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

public class SwitchVector extends AbstractVector<Boolean> {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(SwitchVector.class);

    public static final String type = "Switch";

    public SwitchVector(Attributes attributes) {
        super(attributes, type);
    }

    public SwitchVector(String device, String name) {
        super(device, name, type);
    }
}

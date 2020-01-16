package uk.co.iseeshapes.capture.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

public class NumberVector extends AbstractVector<Double> {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(NumberVector.class);

    public static final String type = "Number";

    public NumberVector(Attributes attributes) {
        super(attributes, type);
    }

    public NumberVector(String device, String name) {
        super(device, name, type);
    }
}

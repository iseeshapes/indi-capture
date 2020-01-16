package uk.co.iseeshapes.capture.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

public class TextVector extends AbstractVector<String> {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(TextVector.class);

    public static final String type = "Text";

    public TextVector(Attributes attributes) {
        super(attributes, type);
    }

    public TextVector(String device, String name) {
        super(device, name, type);
    }
}

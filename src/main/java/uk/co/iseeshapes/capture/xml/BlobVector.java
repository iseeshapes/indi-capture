package uk.co.iseeshapes.capture.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

public class BlobVector extends AbstractVector<byte[]> {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(BlobVector.class);

    public static final String type = "BLOB";

    public BlobVector(String device, String name) {
        super(device, name, type);
    }

    public BlobVector(Attributes attributes) {
        super(attributes, type);
    }
}

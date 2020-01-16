package uk.co.iseeshapes.capture.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

public class BlobValue extends AbstractValue<byte[]> {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(BlobValue.class);

    public static final String type = "BLOB";

    public BlobValue(String name) {
        super(name);
    }

    public BlobValue(Attributes attributes) {
        super(attributes);
    }

    @Override
    public byte[] getValue() {
        return new byte[0];
    }

    @Override
    public String getXMLValue() {
        return null;
    }

    @Override
    public void setValue(byte[] value) {
        //do nothing for the moment
    }

    @Override
    public void setValue(char[] ch, int start, int length) {
        System.out.println();
        System.out.printf("Blob: %s%n", getName());
        for (int i=0;i<length;i++) {
            System.out.print(ch[start+i]);
        }
    }
}

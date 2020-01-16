package uk.co.iseeshapes.capture.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

public class TextValue extends AbstractValue<String> {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(TextValue.class);

    public static final String type = "Text";

    private String value;

    public TextValue (String name) {
        super(name);

        value = null;
    }

    public TextValue(Attributes attributes) {
        super(attributes);

        value = null;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getXMLValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void setValue(char[] ch, int start, int length) {
        value = new String(ch, start, length).trim();
    }
}

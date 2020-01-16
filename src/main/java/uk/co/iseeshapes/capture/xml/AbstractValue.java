package uk.co.iseeshapes.capture.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

public abstract class AbstractValue<T> {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(AbstractValue.class);

    private String name;
    private String label;

    public AbstractValue(String name) {
        this.name = name;
    }

    public AbstractValue(Attributes attributes) {
        name = attributes.getValue("name");
        label = attributes.getValue("label");
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public abstract T getValue();

    public abstract String getXMLValue ();

    public abstract void setValue (T value);

    public abstract void setValue(char[] ch, int start, int length);

    public void buildMessage (StringBuilder message, String deviceName, String vectorName) {
        message.append('\n')
                .append(deviceName)
                .append(".").append(vectorName)
                .append(".").append(name)
                .append("=").append(getValue());
    }
}

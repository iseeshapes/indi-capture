package uk.co.iseeshapes.capture.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SwitchValue extends AbstractValue<Boolean> {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(SwitchValue.class);

    private static final Pattern onPattern = Pattern.compile(".*On.*", Pattern.DOTALL);

    public static final String type = "Switch";

    private boolean on = false;

    public SwitchValue(String name) {
        super(name);
    }

    public SwitchValue(Attributes attributes) {
        super(attributes);
    }

    @Override
    public Boolean getValue() {
        return on;
    }

    @Override
    public String getXMLValue() {
        return on ? "On" : "Off";
    }

    @Override
    public void setValue(Boolean value) {
        on = value;
    }

    @Override
    public void setValue(char[] ch, int start, int length) {
        String value = new String(ch, start, length);
        Matcher matcher = onPattern.matcher(value);
        on = matcher.matches();
    }
}

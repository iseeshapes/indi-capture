package uk.co.iseeshapes.capture.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumberValue extends AbstractValue<Double> {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(NumberValue.class);

    private static final Pattern valuePattern = Pattern.compile(".*(\\d+(\\.\\d)?).*", Pattern.DOTALL);

    public static final String type = "Number";

    private Double value = null;
    private Double min;
    private Double max;
    private String format;

    public NumberValue(String name) {
        super(name);
    }

    public NumberValue(Attributes attributes) {
        super(attributes);

        min = parseNumber(attributes.getValue("min"));
        max = parseNumber(attributes.getValue("max"));
        format = attributes.getValue("format");
    }

    private Double parseNumber (String rawValue) {
        if (rawValue == null) {
            return null;
        }
        Matcher matcher = valuePattern.matcher(rawValue);
        if (matcher.matches()) {
            try {
                return Double.parseDouble(matcher.group(0));
            } catch (Exception e) {
                //Do nothing will return null at the end
            }
        }
        return null;
    }

    @Override
    public Double getValue() {
        return value;
    }

    @Override
    public String getXMLValue() {
        return value == null ? "null" : Double.toString(value);
    }

    @Override
    public void setValue(Double value) {
        this.value = value;
    }

    @Override
    public void setValue(char[] ch, int start, int length) {
        String rawValue = new String(ch, start, length);
        value = parseNumber(rawValue);
    }

    public Double getMin() {
        return min;
    }

    public Double getMax() {
        return max;
    }

    public String getFormat() {
        return format;
    }
}

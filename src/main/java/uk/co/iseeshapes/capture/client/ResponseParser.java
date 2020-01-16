package uk.co.iseeshapes.capture.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import uk.co.iseeshapes.capture.xml.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResponseParser extends DefaultHandler {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(ResponseParser.class);

    private static final Pattern vectorPattern = Pattern.compile("^(def|one|set)(.*)Vector$");
    private static final Pattern valuePattern = Pattern.compile("^(def|one|set)(.*)$");

    private static final String delProperty = "delProperty";
    private static final String contentWrapper = "content-wrapper";
    private static final String message = "message";

    private Map<String, AbstractVector<?>> vectors = new HashMap<>();

    private AbstractVector<?> currentVector;
    private AbstractValue<?> currentValue;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (delProperty.equals(qName) || contentWrapper.equals(qName)) {
            return;
        }

        if (message.equals(qName)) {
            log.debug ("Message: {}", attributes.getValue(message));
            return;
        }

        Matcher matcher = vectorPattern.matcher(qName);
        if (matcher.matches()) {
            String type = matcher.group(2);
            if (NumberVector.type.equals(type)) {
                currentVector = new NumberVector(attributes);
            } else if (SwitchVector.type.equals(type)) {
                currentVector = new SwitchVector(attributes);
            } else if (TextVector.type.equals(type)) {
                currentVector = new TextVector(attributes);
            } else if (BlobVector.type.equals(type)) {
                currentVector = new BlobVector(attributes);
            } else {
                if (log.isErrorEnabled()) {
                    StringBuilder entries = new StringBuilder();
                    for (int i = 0; i < attributes.getLength(); i++) {
                        if (i != 0) {
                            entries.append(", ");
                        }
                        entries.append(attributes.getQName(i)).append("=").append(attributes.getValue(i));
                    }
                    log.error("Unknown vector {} attributes => {}", qName, entries);
                }
                currentVector = null;
            }
            if (currentVector != null) {
                vectors.put(currentVector.getName(), currentVector);
            }
            return;
        }

        matcher = valuePattern.matcher(qName);
        if (matcher.matches()) {
            String type = matcher.group(2);
            if (NumberValue.type.equals(type)) {
                NumberValue value = new NumberValue(attributes);
                ((NumberVector)currentVector).addValue(value);
                currentValue = value;
            } else if (SwitchValue.type.equals(type)) {
                SwitchValue value = new SwitchValue(attributes);
                ((SwitchVector)currentVector).addValue(value);
                currentValue = value;
            } else if (TextValue.type.equals(type)) {
                TextValue value = new TextValue(attributes);
                ((TextVector)currentVector).addValue(value);
                currentValue = value;
            } else if (BlobValue.type.equals(type)) {
                BlobValue value = new BlobValue(attributes);
                ((BlobVector)currentVector).addValue(value);
                currentValue = value;
            } else {
                if (log.isErrorEnabled()) {
                    StringBuilder entries = new StringBuilder();
                    for (int i = 0; i < attributes.getLength(); i++) {
                        if (i != 0) {
                            entries.append(", ");
                        }
                        entries.append(attributes.getQName(i)).append("=").append(attributes.getValue(i));
                    }
                    log.error("Unknown value {} attributes => {}", qName, entries);
                }
                currentValue = null;
            }
            return;
        }

        log.error("Unknown tag: {}", qName);
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        if (currentValue != null) {
            currentValue.setValue(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        Matcher matcher = vectorPattern.matcher(qName);
        if (matcher.matches()) {
            currentVector = null;
            currentValue = null;
        }
        matcher = valuePattern.matcher(qName);
        if (matcher.matches()) {
            currentValue = null;
        }
    }

    public Map<String, AbstractVector<?>> getVectors() {
        return vectors;
    }
}

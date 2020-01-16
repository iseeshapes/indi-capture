package uk.co.iseeshapes.capture.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import uk.co.iseeshapes.capture.AbortException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class AbstractVector<T> {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(AbstractVector.class);

    private String device;
    private String name;
    private String label;
    private String group;
    private State state;
    private String permission;
    private String timeout;
    private String timestamp;

    private String type;

    private Map<String, AbstractValue<T>> values;

    protected AbstractVector(String device, String name, String type) {
        this (type);
        this.device = device;
        this.name = name;
        this.state = State.Unknown;
    }

    protected AbstractVector(Attributes attributes, String type) {
        this (type);
        device = attributes.getValue("device");
        name = attributes.getValue("name");
        label = attributes.getValue("label");
        group = attributes.getValue("group");
        state = State.valueOf(attributes.getValue("state"));
        permission = attributes.getValue("perm");
        timeout = attributes.getValue("timeout");
        timestamp = attributes.getValue("timestamp");
    }

    private AbstractVector(String type) {
        this.type = type;
        values = new HashMap<>();
    }

    public String getDevice() {
        return device;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public String getGroup() {
        return group;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getPermission() {
        return permission;
    }

    public String getTimeout() {
        return timeout;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Set<String> getValueNames () {
        return values.keySet();
    }

    public T getValue(String valueName) {
        AbstractValue<T> value = values.get(valueName);
        if (value == null) {
            return null;
        }
        return value.getValue();
    }

    public void setValue (String valueName, T value) throws AbortException {
        AbstractValue<T> abstractValue = values.get(valueName);
        if (abstractValue == null) {
            throw new AbortException("Cannot find value " + valueName);
        }
        abstractValue.setValue(value);
    }

    public void addValue (AbstractValue<T> value) {
        values.put(value.getName(), value);
    }

    public void buildMessage(StringBuilder message) {
        for (AbstractValue<T> abstractValue : values.values()) {
            abstractValue.buildMessage(message, device, name);
        }
    }

    public String newPropertyXML (String valueName) {
        String vectorQName = "new" + type + "Vector";
        String valueQName = "one" + type;

        return getSetPropertyXML(vectorQName, valueQName, valueName);
    }

    public String updatePropertyXML (String valueName) {
        String vectorQName = "set" + type + "Vector";
        String valueQName = "def" + type;

        return getSetPropertyXML(vectorQName, valueQName, valueName);
    }

    private String getSetPropertyXML (String vectorQName, String valueQName, String valueName) {
        String xml = "<" + vectorQName + " ";
        xml += "device=\"" + device + "\" ";
        xml += "name=\"" + name + "\" ";
        xml += "state=\"" + state.name() + "\">";
        xml += "\n\t<" + valueQName + " ";
        xml += "name=\"" + valueName + "\">";
        xml += "\n" + values.get(valueName).getXMLValue();
        xml += "\n\t</" + valueQName + ">";
        xml += "\n</" + vectorQName + ">";
        return xml;
    }
}

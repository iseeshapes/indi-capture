package uk.co.iseeshapes.capture;

import org.indilib.i4j.client.INDIServerConnection;
import org.indilib.i4j.protocol.*;
import org.indilib.i4j.protocol.api.INDIConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyServerConnection extends INDIServerConnection {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(MyServerConnection.class);

    public MyServerConnection(INDIConnection connection) {
        super(connection);
    }

    @Override
    public void processProtokolMessage(INDIProtocol<?> xml) {
        if (xml instanceof DefNumberVector) {
            DefNumberVector defNumberVector = (DefNumberVector)xml;
            for (DefElement<?> element : defNumberVector.getElements()) {
                if (element instanceof DefNumber) {
                    DefNumber defNumber = (DefNumber)element;
                    if (defNumber.getFormat().endsWith("x")) {
                        if (log.isTraceEnabled()) {
                            log.trace("Device: {}, name: {}, format: {}, value: {}", defNumber.getDevice(),
                                    defNumber.getName(), defNumber.getFormat(), defNumber.getTextContent());
                        }
                        defNumber.setFormat("%.0f");
                    }
                }
            }
        } else if (xml instanceof SetLightVector) {
            SetLightVector setLightVector = (SetLightVector)xml;
            if (setLightVector.getTimeout() == null) {
                setLightVector.setTimeout("1000");
            }
        }
        super.processProtokolMessage(xml);
    }
}

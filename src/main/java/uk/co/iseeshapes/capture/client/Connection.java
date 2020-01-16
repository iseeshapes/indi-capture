package uk.co.iseeshapes.capture.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.helpers.DefaultHandler;
import uk.co.iseeshapes.capture.configuration.ApplicationConfiguration;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Connection {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(Connection.class);

    private boolean printXML = false;
    private Socket socket;

    public boolean isPrintXML() {
        return printXML;
    }

    public void setPrintXML(boolean printXML) {
        this.printXML = printXML;
    }

    /*
    public void test () throws IOException {
        ResponseParser responseParser = new ResponseParser();
        send ("<getProperties version=\"1.7\" device=\"CCD Simulator\" name=\"CONNECTION\" />", responseParser);
        responseParser.printValues();
    }

    public void test2 () throws IOException {
        ResponseParser responseParser = new ResponseParser();
        send ("<getProperties device=\"CCD Simulator\" />", responseParser);
        responseParser.printValues();
    }

    public void test3 () throws IOException {
        ResponseParser responseParser = new ResponseParser();
        getProperty("CCD Simulator", "CCD_EXPOSURE", responseParser);
        responseParser.printValues();
    }

    public void test4 () throws IOException {
        String device = "CCD Simulator";
        ResponseParser responseParser = new ResponseParser();

        String connectionValueName = "CONNECT";
        SwitchVector switchVector = new SwitchVector(device, "CONNECTION");
        switchVector.setState(State.Ok);
        SwitchValue connectionValue = new SwitchValue(connectionValueName);
        connectionValue.setValue(true);
        switchVector.addValue(connectionValue);
        send(switchVector.newPropertyXML(connectionValueName), responseParser);
        responseParser.printValues();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String exposureValueName = "CCD_EXPOSURE_VALUE";
        NumberVector numberVector = new NumberVector(device, "CCD_EXPOSURE");
        numberVector.setState(State.Busy);
        NumberValue numberValue = new NumberValue(exposureValueName);
        numberValue.setValue(3.0);
        numberVector.addValue(numberValue);
        send(numberVector.newPropertyXML(exposureValueName), responseParser);
        responseParser.printValues();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        getProperty("CCD Simulator", "CCD_EXPOSURE", responseParser);
        responseParser.printValues();
    }
    */

    public void connect (ApplicationConfiguration applicationConfiguration) throws IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(applicationConfiguration.getUrl(), applicationConfiguration.getPort()), 0);
    }

    private void getProperty(String device, String property, DefaultHandler defaultHandler) throws IOException {
        send("<getProperties version=\"1.7\" device=\"" + device + "\" name=\"" + property + "\" />", defaultHandler);
    }

    public void send(String xml, DefaultHandler defaultHandler) throws IOException {
        if (printXML) {
            System.out.print("Sent XML: ");
            System.out.println(xml);
        }
        socket.getOutputStream().write(xml.getBytes());
        try {
            Thread.sleep(100);
            StringBuilder response = new StringBuilder();
            response.append("<content-wrapper>");
            while (socket.isClosed()) {
                Thread.sleep(100);
            }
            int no = 1;
            if (printXML) {
                System.out.print(no);
                System.out.print(": ");
            }
            boolean gotResponse = false;
            int raw;
            InputStream is = socket.getInputStream();
            while (!gotResponse) {
                while (is.available() > 0) {
                    raw = is.read();
                    gotResponse = true;
                    char rawChar = (char) raw;
                    if (printXML) {
                        System.out.print(rawChar);
                        if (rawChar == '\n') {
                            System.out.print(++no);
                            System.out.print(": ");
                        }
                    }
                    response.append(rawChar);
                }
            }
            if (printXML)
                System.out.print('\n');
            response.append("</content-wrapper>");
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = saxParserFactory.newSAXParser();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(response.toString().getBytes());
            saxParser.parse(byteArrayInputStream, defaultHandler);
        } catch (Exception e) {
            log.error("Failed to request: {}", xml, e);
        }
    }

    public void disconnect () throws IOException {
        socket.close();
    }
}

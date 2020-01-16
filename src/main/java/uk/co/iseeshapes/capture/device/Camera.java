package uk.co.iseeshapes.capture.device;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.iseeshapes.capture.AbortException;

import java.io.File;

public class Camera extends DeviceConfiguration {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(Camera.class);

    private static final String exposurePropertyName = "CCD_EXPOSURE";
    private static final String exposureValueName = "CCD_EXPOSURE_VALUE";

    private static final String temperaturePropertyValue = "CCD_TEMPERATURE";
    private static final String temperatureValueName = "CCD_TEMPERATURE_VALUE";

    private static final String abortPropertyName = "CCD_ABORT_EXPOSURE";
    private static final String abortValueName = "ABORT";

    private static final String uploadPropertyName = "UPLOAD_SETTINGS";
    private static final String uploadDirectoryValueName = "UPLOAD_DIR";
    private static final String uploadFilenamePrefixValueName = "UPLOAD_PREFIX";

    private static final String uploadModePropertyName = "UPLOAD_MODE";
    private static final String uploadModeClientValueName = "UPLOAD_CLIENT";
    private static final String uploadModeLocalValueName = "UPLOAD_LOCAL";
    private static final String uploadModeBothValueName = "UPLOAD_BOTH";

    @Override
    protected String getDeviceType() {
        return "Camera";
    }

    @JsonIgnore
    public double getExposure() throws AbortException {
        return client.getValue(deviceName, exposurePropertyName, exposureValueName, true);
    }

    @JsonIgnore
    public void setExposure(double exposure, boolean takeImage) throws AbortException {
        client.setValue(deviceName, exposurePropertyName, exposureValueName, exposure, takeImage);
    }

    @JsonIgnore
    public double getTemperature () throws AbortException {
        return client.getValue(deviceName, temperaturePropertyValue, temperatureValueName, true);
    }

    @JsonIgnore
    public void setTemperature (double temperature, boolean updateServer) throws AbortException {
        client.setValue(deviceName, temperaturePropertyValue, temperatureValueName, temperature, updateServer);
    }

    @JsonIgnore
    public void abortExposure () throws AbortException {
        client.setValue(deviceName, abortPropertyName, abortValueName, true, true);
    }

    @JsonIgnore
    public File getDirectory () throws AbortException {
        String directoryName = client.getValue(deviceName, uploadPropertyName, uploadDirectoryValueName, true);
        return new File(directoryName);
    }

    @JsonIgnore
    public void setDirectory (File directory) throws AbortException {
        client.setValue(deviceName, uploadPropertyName, uploadDirectoryValueName, directory.getAbsolutePath(), false);
    }

    @JsonIgnore
    public File getFile () throws AbortException {
        File directory = getDirectory();
        String prefix = client.getValue(deviceName, uploadPropertyName, uploadFilenamePrefixValueName, true);
        return new File (directory, prefix + ".fits");
    }

    @JsonIgnore
    public void setFile (File file) throws AbortException {
        setDirectory(file.getParentFile());
        String prefix = file.getName();
        int index = prefix.lastIndexOf('.');
        if (index > 1) {
            prefix = prefix.substring(0, index - 1);
        }
        client.setValue(deviceName, uploadPropertyName, uploadFilenamePrefixValueName, prefix, false);
    }

    @JsonIgnore
    public UploadMode getUploadMode () throws AbortException {
        if (client.getValue(deviceName, uploadModePropertyName, uploadModeClientValueName, true)) {
            return UploadMode.client;
        } else if (client.getValue(deviceName, uploadModePropertyName, uploadModeLocalValueName)) {
            return UploadMode.local;
        } else if (client.getValue(deviceName, uploadModePropertyName, uploadModeBothValueName)) {
            return UploadMode.both;
        }
        throw new AbortException("Unknown upload mode");
    }

    @JsonIgnore
    public void setUploadMode (UploadMode uploadMode) throws AbortException {
        if (uploadMode == UploadMode.client) {
            client.setValue(deviceName, uploadModePropertyName, uploadModeClientValueName, true, false);
        } else if (uploadMode == UploadMode.local) {
            client.setValue(deviceName, uploadModePropertyName, uploadModeLocalValueName, true, false);
        } else if (uploadMode == UploadMode.both) {
            client.setValue(deviceName, uploadModePropertyName, uploadModeBothValueName, true, false);
        }
    }

    @JsonIgnore
    public byte[] getImageData () throws AbortException {
        client.setPrintXML(true);
        client.getValue(deviceName, "CCD1", "CCD1", true);
        client.getValue(deviceName, "CCD2", "CCD2", true);
        client.setPrintXML(false);
        return null;
    }
}

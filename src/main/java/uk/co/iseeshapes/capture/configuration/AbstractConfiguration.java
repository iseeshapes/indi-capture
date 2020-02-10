package uk.co.iseeshapes.capture.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.iseeshapes.capture.AbortException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractConfiguration {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(AbstractConfiguration.class);

    private static final Pattern quitPattern = Pattern.compile("^[qQ]$");

    @JsonIgnore
    private PrintStream out;

    @JsonIgnore
    private BufferedReader reader;

    public void setStreams(PrintStream out, BufferedReader reader) {
        this.out = out;
        this.reader = reader;
    }

    protected <T> T readValue (String field, T defaultValue, Function<String, T> function) throws IOException, AbortException {
        while (true) {
            out.print(field);
            if (defaultValue != null) {
                if (defaultValue instanceof Boolean) {
                    out.printf(" (%s)", (boolean)defaultValue ? "y" : "n");
                } else {
                    out.print(" (" + defaultValue + ")");
                }
            }
            out.print(": ");

            String rawValue = reader.readLine();

            if (defaultValue != null && rawValue.length() == 0) {
                return defaultValue;
            }

            Matcher matcher = quitPattern.matcher(rawValue);
            if (matcher.matches()) {
                throw new AbortException();
            }

            T value = function.apply(rawValue);
            if (value != null) {
                return value;
            }
            out.print("\r");
            out.print("Invalid (");
            out.print(rawValue);
            out.print(") ");
        }
    }

    public abstract void fillInBlanks () throws IOException, AbortException;

    public abstract void ask () throws IOException, AbortException;
}

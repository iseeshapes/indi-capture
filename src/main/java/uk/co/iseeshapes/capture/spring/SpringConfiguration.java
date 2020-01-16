package uk.co.iseeshapes.capture.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.co.iseeshapes.capture.configuration.ConfigurationManager;
import uk.co.iseeshapes.capture.script.CaptureScript;

@Configuration
public class SpringConfiguration {

    @Bean
    CaptureScript capture () {
        return new CaptureScript(new ConfigurationManager(new ObjectMapper()));
    }
}

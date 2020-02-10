package uk.co.iseeshapes.capture.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.co.iseeshapes.capture.configuration.ConfigurationManager;
import uk.co.iseeshapes.capture.script.Capture;
import uk.co.iseeshapes.capture.script.ContinuousCapture;

@Configuration
public class SpringConfiguration {
    @Bean
    Capture capture () {
        return new Capture(new ConfigurationManager(new ObjectMapper()));
    }

    @Bean
    ContinuousCapture continuousCapture () {
        return new ContinuousCapture(new ConfigurationManager(new ObjectMapper()));
    }
}

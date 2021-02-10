package uk.co.iseeshapes.capture.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.co.iseeshapes.capture.audio.CaptureSounds;
import uk.co.iseeshapes.capture.audio.impl.UrlCaptureSounds;
import uk.co.iseeshapes.capture.configuration.ConfigurationManager;
import uk.co.iseeshapes.capture.script.Capture;
import uk.co.iseeshapes.capture.script.ContinuousCapture;

@Configuration
public class SpringConfiguration {
    @Bean
    CaptureSounds captureSounds () {
        return new UrlCaptureSounds();
    }

    @Bean
    Capture capture (CaptureSounds captureSounds) {
        return new Capture(new ConfigurationManager(new ObjectMapper()), captureSounds);
    }

    @Bean
    ContinuousCapture continuousCapture (CaptureSounds captureSounds) {
        return new ContinuousCapture(new ConfigurationManager(new ObjectMapper()), captureSounds);
    }
}

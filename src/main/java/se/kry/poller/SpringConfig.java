package se.kry.poller;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableScheduling // The key annotation here to enable periodic polling of services
public class SpringConfig {

    @Bean // Used in the PollingClient to make requests
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

}

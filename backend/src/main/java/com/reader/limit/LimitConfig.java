package com.reader.limit;

import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LimitProperties.class)
public class LimitConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}

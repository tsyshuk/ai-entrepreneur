package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.example.config.JwtProperties;

@EnableConfigurationProperties({ JwtProperties.class })
@SpringBootApplication
public class AiEntrepreneurApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiEntrepreneurApplication.class, args);
    }
}

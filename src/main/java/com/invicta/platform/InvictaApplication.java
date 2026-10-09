package com.invicta.platform;

import com.invicta.platform.config.InvictaProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(InvictaProperties.class)
public class InvictaApplication {
    public static void main(String[] args) {
        SpringApplication.run(InvictaApplication.class, args);
    }
}

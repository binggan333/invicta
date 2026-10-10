package com.invicta;

import com.invicta.config.InvictaProperties;
import com.invicta.config.MailProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({InvictaProperties.class, MailProperties.class})
public class InvictaApplication {
    public static void main(String[] args) {
        SpringApplication.run(InvictaApplication.class, args);
    }
}

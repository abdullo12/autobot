package org.example.autobot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AutobotApplication {
    public static void main(String[] args) {
        SpringApplication.run(AutobotApplication.class, args);
    }

}

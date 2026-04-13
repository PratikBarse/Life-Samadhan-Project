package com.lifesamadhan.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.scheduling.annotation.EnableScheduling
public class LifeSamadhanApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(LifeSamadhanApiApplication.class, args);
    }
}
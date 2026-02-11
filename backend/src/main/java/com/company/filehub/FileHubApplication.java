package com.company.filehub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FileHubApplication {
    public static void main(String[] args) {
        SpringApplication.run(FileHubApplication.class, args);
    }
}

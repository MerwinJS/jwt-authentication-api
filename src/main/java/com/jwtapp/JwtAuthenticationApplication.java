package com.jwtapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

//@EnableJpaRepositories(basePackages = "com.jwtapp.repository")
//@EntityScan(basePackages = "com.jwtapp.model")
@SpringBootApplication
@ConfigurationPropertiesScan
public class JwtAuthenticationApplication {

    public static void main(String[] args) {
        SpringApplication.run(JwtAuthenticationApplication.class, args);
    }

}

package org.phoenix.flightrouteapi;

import org.phoenix.flightrouteapi.security.config.properties.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class FlightRouteApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlightRouteApiApplication.class, args);
    }

}

package com.example.sinkinputapp;

import lombok.Value;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.util.UUID;
import java.util.function.Consumer;

@SpringBootApplication
public class SinkInputAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(SinkInputAppApplication.class, args);
    }
}

enum Status {
    VALUED,
    PREMIUM,
}

@Value
class FlyingPassenger {
    private final UUID id;
    private final String name;
    private final Status status;
}

@Log4j2
@Configuration
class FlyAttendant {

    @Bean
    Consumer<Flux<FlyingPassenger>> attendee() {
        return flux -> flux.subscribe(passenger -> log.info("received: {}", passenger));
    }
}

package com.example.sourceoutputapp;

import lombok.Value;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

@SpringBootApplication
public class SourceOutputAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(SourceOutputAppApplication.class, args);
    }
}

@Value
class Passenger {
    private final UUID id;
    private final String name;
}

@Log4j2
@Configuration
class DeskAgent {

    @Bean
    List<String> names() {
        return Arrays.asList("Max", "Bob", "Alice");
    }

    @Bean
    Supplier<Passenger> randomPassenger(List<String> names) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Supplier<Integer> index = () -> random.nextInt(names.size());
        return () -> {
            Integer i = index.get();
            String name = names.get(i);
            return new Passenger(UUID.randomUUID(), name);
        };
    }

    @Bean
    Supplier<Flux<Passenger>> checkIn() {
        Supplier<Passenger> randomPassenger = randomPassenger(names());
        return () -> Flux.interval(Duration.ofMillis(345))
                         .onBackpressureDrop()
                         .map(aLong -> randomPassenger.get())
                         .doOnNext(passenger -> log.info("generated: {}", passenger));
    }
}

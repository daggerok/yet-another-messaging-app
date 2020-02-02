package com.example.processortransformingapp;

import lombok.Value;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Supplier;

@SpringBootApplication
public class ProcessorTransformingAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProcessorTransformingAppApplication.class, args);
    }
}

@Value
class Passenger {
    private final UUID id;
    private final String name;
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
class GateAgent {

    @Bean
    Function<Flux<Passenger>, Flux<FlyingPassenger>> transfer() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Supplier<Status> randomStatus = () -> random.nextInt(5) == 0 ? Status.PREMIUM : Status.VALUED;
        return flux -> flux.map(passenger -> new FlyingPassenger(passenger.getId(),
                                                                 passenger.getName(),
                                                                 randomStatus.get()))
                           .doOnNext(passenger -> log.info("transformed: {}", passenger));
    }
}


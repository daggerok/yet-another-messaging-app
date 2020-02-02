# yet-another-messaging-app [![Build Status](https://travis-ci.org/daggerok/yet-another-messaging-app.svg?branch=master)](https://travis-ci.org/daggerok/yet-another-messaging-app)
Build reactive pipelines with spring cloud stream

## how too

### source
#### create java bean
```java
@Log4j2
@Configuration
class DeskAgent {

    @Bean
    Supplier<Flux<Passenger>> checkIn() {
        Supplier<Passenger> randomPassenger = randomPassenger(names());
        return () -> Flux.interval(Duration.ofMillis(345))
                         .onBackpressureDrop()
                         .map(aLong -> randomPassenger.get())
                         .doOnNext(passenger -> log.info("generated: {}", passenger));
    }
}
```
### configure bindings
```properties
spring.cloud.stream.bindings.checkIn-out-0.destination=processorTransformingApp
#spring.cloud.function.definition=checkIn
```

### processor
#### create java bean
```java
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
```
### configure bindings
```properties
spring.cloud.stream.bindings.transfer-in-0.destination=processorTransformingApp
spring.cloud.stream.bindings.transfer-in-0.group=processorTransformingApp
spring.cloud.stream.bindings.transfer-out-0.destination=sinkInputApp
#spring.cloud.function.definition=transfer
```

### sink
#### create java bean
```java
@Log4j2
@Configuration
class FlyAttendant {

    @Bean
    Consumer<Flux<FlyingPassenger>> attendee() {
        return flux -> flux.subscribe(passenger -> log.info("received: {}", passenger));
    }
}
```
### configure bindings
```properties
spring.cloud.stream.bindings.attendee-in-0.destination=sinkInputApp
spring.cloud.stream.bindings.attendee-in-0.group=sinkInputApp
#spring.cloud.function.definition=attendee
```

## build, run and test

```bash
./mvnw -f rabbitmq docker:build docker:start

./mvnw
java -jar ./source-output-app/target/*.jar &
java -jar ./processor-transforming-app/target/*.jar &
java -jar ./sink-input-app/target/*.jar &

sleep 15s
killall -9 java

./mvnw -f rabbitmq docker:stop docker:remove
```

## resources
* [spring-cloud-stream functional binding](https://cloud.spring.io/spring-cloud-static/spring-cloud-stream/current/reference/html/spring-cloud-stream.html#_functional_binding_names)
* [spring-cloud-stream reactive functions support](https://github.com/spring-cloud/spring-cloud-stream/blob/master/docs/src/main/asciidoc/spring-cloud-stream.adoc#reactive-functions-support)
* [Old example daggerok/spring-cloud-function-stream-integration](https://github.com/daggerok/spring-cloud-function-stream-integration)
<!--
* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.2.4.RELEASE/maven-plugin/)
* [Spring Configuration Processor](https://docs.spring.io/spring-boot/docs/2.2.4.RELEASE/reference/htmlsingle/#configuration-metadata-annotation-processor)
* [Spring for RabbitMQ](https://docs.spring.io/spring-boot/docs/2.2.4.RELEASE/reference/htmlsingle/#boot-features-amqp)
* [Messaging with RabbitMQ](https://spring.io/guides/gs/messaging-rabbitmq/)
-->

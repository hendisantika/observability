package id.my.hendisantika.fiboclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.logging.LogLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class FiboClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(FiboClientApplication.class, args);
    }

}

@Configuration
class WebClientConfig {
    @Bean
    public WebClient webClientDefault() {
        var factory = new DefaultUriBuilderFactory();
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        // 16 MB
        final int byteCount = 16 * 1024 * 1024;
        final ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(byteCount))
                .build();
        ObjectMapper mapper = new ObjectMapper();
        final HttpClient client = HttpClient.create()
                .wiretap(this.getClass().getCanonicalName(), LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL);

        return WebClient
                .builder()
                .exchangeStrategies(strategies)
                .clientConnector(new ReactorClientHttpConnector(client))
                .codecs(clientCodecConfigurer ->
                        clientCodecConfigurer.defaultCodecs()
                                .jackson2JsonDecoder(new Jackson2JsonDecoder(mapper)))
                .uriBuilderFactory(factory)
                .build();
    }
}

// INITIALIZE
@Log4j2
@Component
@AllArgsConstructor
class SampleInitializer {
    private final WebClient webClientConfig;

    @SneakyThrows
    @EventListener(ApplicationReadyEvent.class)
    public Mono<Void> ready() {
        log.info("Fibonacci Client Working ...");

        var requestSpec = webClientConfig
                .method(HttpMethod.POST)
                .uri("http://localhost:8090/api/calculate-fibos")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        return Flux.interval(Duration.ofMillis(1000))
                .flatMap(tick -> {
                    var body = List.of((int) (Math.random() * 50), (int) (Math.random() * 10), (int) (Math.random() * 10));
                    return requestSpec
                            .bodyValue(Map.of("numbers", body))
                            .retrieve()
                            .bodyToMono(String.class)
                            .doOnNext(log::info);
                })
                .onErrorContinue((error, obj) -> log.error(error.getMessage()))
                .collectList()
                .then();
    }
}

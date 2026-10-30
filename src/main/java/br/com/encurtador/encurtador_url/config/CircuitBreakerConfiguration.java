package br.com.encurtador.encurtador_url.config;

import br.com.encurtador.encurtador_url.cache.CircuitBreakerCacheManager;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

@Configuration
@Slf4j
public class CircuitBreakerConfiguration {

    @Bean
    public CircuitBreakerRegistry redisCircuitBreakerRegistry() {
        var config = CircuitBreakerConfig
                .custom()
                .slidingWindowSize(5)
                .minimumNumberOfCalls(3)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(15))
                .permittedNumberOfCallsInHalfOpenState(2)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();

        CircuitBreakerRegistry registry =
                CircuitBreakerRegistry.of(config);

        registry.circuitBreaker("redisCache")
                .getEventPublisher()
                .onStateTransition(event -> {
                            var transicao = event.getStateTransition();
                            switch (transicao.getToState()) {
                                case OPEN -> log.warn(
                                        "Circuit Breaker Redis: estado alterado de {} para {}. "
                                                + "O cache Redis está indisponível.",
                                        transicao.getFromState(),
                                        transicao.getToState()
                                );
                                case HALF_OPEN -> log.warn(
                                        "Circuit Breaker Redis: estado alterado de {} para {}. "
                                                + "O cache Redis está em teste.",
                                        transicao.getFromState(),
                                        transicao.getToState()
                                );
                                case CLOSED -> log.info(
                                        "Circuit Breaker Redis: estado alterado de {} para {}. "
                                                + "Chamadas ao cache liberadas",
                                        transicao.getFromState(),
                                        transicao.getToState()
                                );
                            }
                        }
                );

        return registry;
    }

    @Bean
    @Primary
    public CacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            RedisCacheConfiguration redisCacheConfiguration,
            CircuitBreakerRegistry registry
    ) {
        RedisCacheManager redisCacheManager =
                RedisCacheManager.builder(connectionFactory)
                        .cacheDefaults(redisCacheConfiguration)
                        .build();

        return new CircuitBreakerCacheManager(
                redisCacheManager,
                registry
        );
    }
}

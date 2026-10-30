package br.com.encurtador.encurtador_url;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom;
import static org.junit.jupiter.api.Assertions.*;

class CircuitBreakerTest {

    @Test
    void deveAbrirCircuitBreakerAposTaxaDeFalhas(){

        var configCircuit = custom()
                .slidingWindowSize(5)
                .minimumNumberOfCalls(3)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(15))
                .build();

        var circuitBreaker = CircuitBreaker.of("testeRedis", configCircuit);

        assertThrows(RuntimeException.class, () ->
                circuitBreaker.executeRunnable(() -> {
                    throw new RuntimeException("Falha simulada");
                }));

        assertThrows(RuntimeException.class, () ->
                circuitBreaker.executeRunnable(() -> {
                    throw new RuntimeException("Falha simulada");
                }));

        assertEquals(
                CircuitBreaker.State.CLOSED,
                circuitBreaker.getState()
        );

        assertThrows(RuntimeException.class, () ->
                circuitBreaker.executeRunnable(() -> {
                    throw new RuntimeException("Falha simulada");
                }));

        assertEquals(
                CircuitBreaker.State.OPEN,
                circuitBreaker.getState()
        );

    }


    @Test
    void deveRecuperarCircuitBreakerAposFalhas() {
        var configuracao = CircuitBreakerConfig.custom()
                .slidingWindowSize(5)
                .minimumNumberOfCalls(3)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofMillis(100))
                .permittedNumberOfCallsInHalfOpenState(2)
                .build();

        var circuitBreaker =
                CircuitBreaker.of("testeRecuperacao", configuracao);

        for (int i = 0; i < 3; i++) {
            assertThrows(RuntimeException.class, () ->
                    circuitBreaker.executeRunnable(() -> {
                        throw new RuntimeException("Falha simulada");
                    }));
        }

        assertEquals(
                CircuitBreaker.State.OPEN,
                circuitBreaker.getState()
        );

        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Teste interrompido durante a espera");
        }

        circuitBreaker.executeRunnable(() -> {});

        assertEquals(
                CircuitBreaker.State.HALF_OPEN,
                circuitBreaker.getState()
        );

        circuitBreaker.executeRunnable(() -> {});

        assertEquals(
                CircuitBreaker.State.CLOSED,
                circuitBreaker.getState()
        );
    }


}

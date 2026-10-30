package br.com.encurtador.encurtador_url.cache;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CircuitBreakerCacheTest {

    private Cache cacheReal;
    private CircuitBreaker circuito;
    private CircuitBreakerCache cacheProtegido;

    @BeforeEach
    void configurar() {
        cacheReal = mock(Cache.class);

        var configuracao = CircuitBreakerConfig.custom()
                .slidingWindowSize(5)
                .minimumNumberOfCalls(3)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(15))
                .build();

        circuito = CircuitBreaker.of("testeCache", configuracao);
        cacheProtegido = new CircuitBreakerCache(cacheReal, circuito);
    }

    @Test
    void deveDelegarConsultaAoCacheQuandoCircuitoFechado() {
        var valorEsperado = new SimpleValueWrapper("https://exemplo.com");

        when(cacheReal.get("abc123")).thenReturn(valorEsperado);

        Cache.ValueWrapper resultado = cacheProtegido.get("abc123");

        assertSame(valorEsperado, resultado);
        verify(cacheReal).get("abc123");
        assertEquals(CircuitBreaker.State.CLOSED, circuito.getState());
    }

    @Test
    void deveAbrirCircuitoAposFalhasDeConsulta() {
        when(cacheReal.get(any())).thenThrow(
                new IllegalStateException("Falha simulada no cache")
        );

        for (int i = 0; i < 3; i++) {
            assertThrows(IllegalStateException.class,
                    () -> cacheProtegido.get("abc123"));
        }

        assertEquals(CircuitBreaker.State.OPEN, circuito.getState());

        // Com o circuito aberto, a consulta não deve chegar ao cache real.
        assertThrows(
                io.github.resilience4j.circuitbreaker.CallNotPermittedException.class,
                () -> cacheProtegido.get("abc123")
        );

        verify(cacheReal, times(3)).get("abc123");
    }

    @Test
    void deveDelegarGravacaoAoCacheQuandoCircuitoFechado() {
        cacheProtegido.put("abc123", "https://exemplo.com");

        verify(cacheReal).put("abc123", "https://exemplo.com");
        assertEquals(CircuitBreaker.State.CLOSED, circuito.getState());
    }



    @Test
    void deveVoltarAConsultarCacheAposRecuperacaoDoCircuitBreaker() {
        var configuracao = CircuitBreakerConfig.custom()
                .slidingWindowSize(5)
                .minimumNumberOfCalls(3)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofMillis(100))
                .permittedNumberOfCallsInHalfOpenState(2)
                .build();

        CircuitBreaker circuito =
                CircuitBreaker.of("testeRecuperacaoCache", configuracao);

        Cache cacheSimulado = mock(Cache.class);
        CircuitBreakerCache cacheProtegido =
                new CircuitBreakerCache(cacheSimulado, circuito);

        AtomicInteger chamadas = new AtomicInteger();

        when(cacheSimulado.get("abc123"))
                .thenAnswer(invocacao -> {
                    if (chamadas.incrementAndGet() <= 3) {
                        throw new IllegalStateException("Redis indisponível");
                    }

                    return new SimpleValueWrapper("https://exemplo.com");
                });

        // As três primeiras consultas falham.
        for (int i = 0; i < 3; i++) {
            assertThrows(
                    IllegalStateException.class,
                    () -> cacheProtegido.get("abc123")
            );
        }

        assertEquals(CircuitBreaker.State.OPEN, circuito.getState());

        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Teste interrompido durante a espera");
        }

        // Duas chamadas bem-sucedidas em HALF_OPEN.
        assertNotNull(cacheProtegido.get("abc123"));
        assertNotNull(cacheProtegido.get("abc123"));

        assertEquals(CircuitBreaker.State.CLOSED, circuito.getState());
    }



}
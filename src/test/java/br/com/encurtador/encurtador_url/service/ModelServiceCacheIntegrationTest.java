package br.com.encurtador.encurtador_url.service;

import br.com.encurtador.encurtador_url.cache.CircuitBreakerCacheManager;
import br.com.encurtador.encurtador_url.config.CacheConfig;
import br.com.encurtador.encurtador_url.model.Url;
import br.com.encurtador.encurtador_url.repository.ModelRepository;
import br.com.encurtador.encurtador_url.utils.CodigoAleatorio;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(ModelServiceCacheIntegrationTest.ConfiguracaoTeste.class)
public class ModelServiceCacheIntegrationTest {

    @Autowired
    private ModelService modelService;

    @Autowired
    private ModelRepository repository;

    @Autowired
    private CircuitBreakerRegistry registro;

    @Configuration
    @EnableCaching
    @Import(CacheConfig.class)
    static class ConfiguracaoTeste {

        @Bean
        CircuitBreakerRegistry circuitoRegistro() {
            var configuracao = CircuitBreakerConfig.custom()
                    .slidingWindowSize(5)
                    .minimumNumberOfCalls(3)
                    .failureRateThreshold(50)
                    .waitDurationInOpenState(Duration.ofSeconds(15))
                    .build();

            return CircuitBreakerRegistry.of(configuracao);
        }

        @Bean
        ModelRepository repository() {
            return mock(ModelRepository.class);
        }

        @Bean
        CodigoAleatorio codigoAleatorio() {
            return mock(CodigoAleatorio.class);
        }

        @Bean
        ModelService modelService(
                ModelRepository repository,
                CodigoAleatorio codigoAleatorio
        ) {
            return new ModelService(repository, codigoAleatorio);
        }

        @Bean
        CacheManager cacheManager(CircuitBreakerRegistry registro) {
            Cache cache = mock(Cache.class);

            when(cache.get(any()))
                    .thenThrow(new IllegalStateException(
                            "Falha simulada na leitura do cache"
                    ));

            doThrow(new IllegalStateException(
                    "Falha simulada na gravação do cache"
            )).when(cache).put(any(), any());

            CacheManager delegado = mock(CacheManager.class);
            when(delegado.getCache("urls")).thenReturn(cache);

            return new CircuitBreakerCacheManager(delegado, registro);
        }
    }

    @Test
    void deveConsultarRepositorioQuandoLeituraEGravacaoDoCacheFalham() {
        String codigo = "abc123";
        String urlOriginal = "https://exemplo.com";

        Url url = Url.builder()
                .codigoEncurtado(codigo)
                .urlOriginal(urlOriginal)
                .build();

        when(repository.findByCodigoEncurtado(codigo))
                .thenReturn(Optional.of(url));

        String resultado = modelService.pegarUrls(codigo);

        assertEquals(urlOriginal, resultado);

        verify(repository).findByCodigoEncurtado(codigo);
    }

    @Test
    void deveConsultarRepositorioQuandoCircuitBreakerJaEstaAberto() {
        String codigo = "circuitoAberto123";
        String urlOriginal = "https://exemplo.com";

        Url url = Url.builder()
                .codigoEncurtado(codigo)
                .urlOriginal(urlOriginal)
                .build();

        when(repository.findByCodigoEncurtado(codigo))
                .thenReturn(Optional.of(url));

        var circuito = registro.circuitBreaker("redisCache");

        circuito.transitionToOpenState();

        String resultado = modelService.pegarUrls(codigo);

        assertEquals(urlOriginal, resultado);
        verify(repository).findByCodigoEncurtado(codigo);
    }


    @Test
    void deveLancarUrlNaoEncontradaQuandoCircuitoEstaAbertoEUrlNaoExiste() {
        String codigo = "codigoInexistente";

        when(repository.findByCodigoEncurtado(codigo))
                .thenReturn(Optional.empty());

        var circuito = registro.circuitBreaker("redisCache");
        circuito.transitionToOpenState();

        assertThrows(
                br.com.encurtador.encurtador_url.exception.UrlNaoEncontradaException.class,
                () -> modelService.pegarUrls(codigo)
        );

        verify(repository).findByCodigoEncurtado(codigo);
    }



}

package br.com.encurtador.encurtador_url.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class CacheConfigTest {

    private CacheErrorHandler handler;
    private Cache cache;
    private RuntimeException erro;

    @BeforeEach
    void configurar() {
        CacheConfig configuracao = new CacheConfig();

        handler = configuracao.errorHandler();
        cache = mock(Cache.class);
        erro = new IllegalStateException("Falha simulada no Redis");
    }

    @Test
    void deveAbsorverFalhaDeLeitura() {
        assertDoesNotThrow(() ->
                handler.handleCacheGetError(erro, cache, "codigo123")
        );
    }

    @Test
    void deveAbsorverFalhaDeGravacao() {
        assertDoesNotThrow(() ->
                handler.handleCachePutError(
                        erro, cache, "codigo123", "https://exemplo.com"
                )
        );
    }

    @Test
    void deveAbsorverFalhaDeRemocao() {
        assertDoesNotThrow(() ->
                handler.handleCacheEvictError(erro, cache, "codigo123")
        );
    }

    @Test
    void deveAbsorverFalhaDeLimpeza() {
        assertDoesNotThrow(() ->
                handler.handleCacheClearError(erro, cache)
        );
    }

}
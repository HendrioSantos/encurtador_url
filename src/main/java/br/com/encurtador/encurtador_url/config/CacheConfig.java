package br.com.encurtador.encurtador_url.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class CacheConfig implements CachingConfigurer, CacheErrorHandler {

    @Override
    public CacheErrorHandler errorHandler(){
        return this;
    }

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        log.warn(
                "Falha ao consultar o cache '{}'. Chave: {}. Erro: {} "
                        + "A aplicação tentará continuar sem o cache.",
                cache.getName(), key, exception.getMessage()
        );
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        log.warn(
                "Falha ao atualizar o cache '{}', Chave: {}. Erro: {} "
                        + "A aplicação tentará continuar sem o cache.",
                cache.getName(), key, exception.getMessage()
        );
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        log.warn(
                "Erro ao inválidar uma entrada do cache '{}', Chave: {}. Erro: {} "
                        + "A aplicação tentará continuar sem o cache.",
                cache.getName(), key, exception.getMessage()
        );
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        log.warn(
                "Erro ao limpar o cache '{}', Erro: {}. "
                        + "A aplicação tentará continuar sem o cache.",
                cache.getName(), exception.getMessage()
        );
    }
}

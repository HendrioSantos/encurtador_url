package br.com.encurtador.encurtador_url.cache;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CircuitBreakerCacheManager implements CacheManager {

    private final CacheManager delegate;
    private final CircuitBreakerRegistry registry;

    private final ConcurrentMap<String, Cache> caches =
            new ConcurrentHashMap<>();

    public CircuitBreakerCacheManager(
            CacheManager delegate,
            CircuitBreakerRegistry registry
    ) {
        this.delegate = delegate;
        this.registry = registry;
    }

    @Override
    public Cache getCache(String name) {
        return caches.computeIfAbsent(name, cacheName -> {
            Cache cacheOriginal = delegate.getCache(cacheName);

            if (cacheOriginal == null) {
                return null;
            }

            return new CircuitBreakerCache(
                    cacheOriginal,
                    registry.circuitBreaker("redisCache")
            );
        });
    }

    @Override
    public Collection<String> getCacheNames() {
        return delegate.getCacheNames();
    }
}

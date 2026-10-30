package br.com.encurtador.encurtador_url.cache;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.cache.Cache;

import java.util.concurrent.Callable;

public class CircuitBreakerCache implements Cache {

    private final Cache cache;
    private final CircuitBreaker circuitBreaker;

    public CircuitBreakerCache(Cache cache, CircuitBreaker circuitBreaker) {
        this.cache = cache;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public String getName() {
        return cache.getName();
    }

    @Override
    public Object getNativeCache() {
        return cache.getNativeCache();
    }

    @Override
    public ValueWrapper get(Object key) {
        return circuitBreaker.executeSupplier(() -> cache.get(key));
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        return circuitBreaker.executeSupplier(() -> cache.get(key, type));
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        return circuitBreaker.executeSupplier(
                () -> cache.get(key, valueLoader)
        );
    }

    @Override
    public void put(Object key, Object value) {
        circuitBreaker.executeRunnable(() -> cache.put(key, value));
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        return circuitBreaker.executeSupplier(
                () -> cache.putIfAbsent(key, value)
        );
    }

    @Override
    public void evict(Object key) {
        circuitBreaker.executeRunnable(() -> cache.evict(key));
    }

    @Override
    public boolean evictIfPresent(Object key) {
        return circuitBreaker.executeSupplier(
                () -> cache.evictIfPresent(key)
        );
    }

    @Override
    public void clear() {
        circuitBreaker.executeRunnable(cache::clear);
    }

    @Override
    public boolean invalidate() {
        return circuitBreaker.executeSupplier(cache::invalidate);
    }
}

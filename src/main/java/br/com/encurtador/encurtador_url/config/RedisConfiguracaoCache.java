package br.com.encurtador.encurtador_url.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfiguracaoCache {

    @Value("${spring.cache.redis.time-to-live:10m}")
    private Duration ttl;

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {

        //Duration.ofMinutes(10)
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer()));
    }

}

package br.com.encurtador.encurtador_url.monitor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnectionCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisMonitoramento {

    private final StringRedisTemplate redisTemplate;

    private Boolean redisDisponivel = null;

    @Scheduled(fixedDelayString = "${app.redis.monitor-interval:5000}")
    public void verificarDisponibilidadeRedis() {
        boolean disponivel;

        try {
            redisTemplate.execute(
                    (RedisCallback<Object>) RedisConnectionCommands::ping
            );
            disponivel = true;

        } catch (Exception ex) {
            disponivel = false;
        }

        if (redisDisponivel == null || redisDisponivel != disponivel) {

            if (disponivel) {
                log.info("Redis disponível agora, conexão estabelecida");
            } else {
                log.warn("Redis indisponível a aplicação continuará sem ele.");
            }

            redisDisponivel = disponivel;
        }
    }

}

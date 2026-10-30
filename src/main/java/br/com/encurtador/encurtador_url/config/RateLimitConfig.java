package br.com.encurtador.encurtador_url.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class RateLimitConfig implements Filter {

    private final Map<String, Bucket> cacheDeBaldes = new ConcurrentHashMap<>();

    private Bucket criarNovoBalde() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(3)
                        .refillIntervally(3, Duration.ofSeconds(10))
                        .build()).build();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        var httpRequest = (HttpServletRequest) request;
        var httpResponse = (HttpServletResponse) response;
        var path = httpRequest.getRequestURI();

        String URL = "/api/v1/model/criar-urls";
        if (path.startsWith(URL)) {
            var ipDoUsuario = httpRequest.getRemoteAddr();

            var balde = cacheDeBaldes.computeIfAbsent(ipDoUsuario, k -> criarNovoBalde());

            if (!balde.tryConsume(1)) {
                log.warn("IP {} bloqueado por excesso de requisições no endpoint de criação.", ipDoUsuario);
                httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                httpResponse.setContentType("text/plain;charset=UTF-8");
                httpResponse.getWriter().write("Fazendo requisições rápido demais, aguarde 10 segundos.");
                return;
            }
        }
            chain.doFilter(request, response);
    }
}

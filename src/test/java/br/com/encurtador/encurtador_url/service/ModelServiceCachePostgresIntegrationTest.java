package br.com.encurtador.encurtador_url.service;

import br.com.encurtador.encurtador_url.model.Url;
import br.com.encurtador.encurtador_url.repository.ModelRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@SpringBootTest(properties = "spring.cache.redis.time-to-live=10s")
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@Transactional
@Testcontainers
class ModelServiceCachePostgresIntegrationTest {

    @Autowired
    private ModelService service;

    @MockitoSpyBean
    private ModelRepository repository;

    @Autowired
    private CacheManager cacheManager;

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("encurtador_teste")
                    .withUsername("teste")
                    .withPassword("teste");

    @Container
    static final GenericContainer<?> REDIS =
            new GenericContainer<>("redis:7-alpine")
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void configurarContainers(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);

        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add(
                "spring.data.redis.port",
                () -> REDIS.getMappedPort(6379)
        );
    }

    private static final String CODIGO_MISS = "cachemiss123";
    private static final String CODIGO_HIT = "cachehit123";
    private static final String CODIGO_TTL = "ttltest12345";

    private Cache obterCache() {
        Cache cache = cacheManager.getCache("urls");

        if (cache == null) {
            throw new IllegalStateException(
                    "Cache 'urls' não encontrado"
            );
        }

        return cache;
    }

    private void prepararUrl(String codigo, String urlOriginal) {
        repository.saveAndFlush(
                Url.builder()
                        .codigoEncurtado(codigo)
                        .urlOriginal(urlOriginal)
                        .dataCriacao(LocalDateTime.now())
                        .build()
        );

        obterCache().evict(codigo);
        clearInvocations(repository);
    }

    @AfterEach
    void limparCacheDosTestes() {
        obterCache().evict(CODIGO_MISS);
        obterCache().evict(CODIGO_HIT);
        obterCache().evict(CODIGO_TTL);
    }

    @Test
    void deveConsultarPostgresQuandoOcorrerCacheMiss() {
        String codigo = CODIGO_MISS;
        String urlOriginal = "https://exemplo.com/cache-miss";

        prepararUrl(codigo, urlOriginal);

        String resultado = service.pegarUrls(codigo);

        assertEquals(urlOriginal, resultado);

        verify(repository, times(1))
                .findByCodigoEncurtado(codigo);
    }

    @Test
    void deveEvitarNovaConsultaAoPostgresQuandoOcorrerCacheHit() {
        String codigo = CODIGO_HIT;
        String urlOriginal = "https://exemplo.com/cache-hit";

        prepararUrl(codigo, urlOriginal);

        String primeiraResposta = service.pegarUrls(codigo);

        assertEquals(urlOriginal, primeiraResposta);

        verify(repository, times(1))
                .findByCodigoEncurtado(codigo);

        clearInvocations(repository);

        String segundaResposta = service.pegarUrls(codigo);

        assertEquals(urlOriginal, segundaResposta);

        verify(repository, never())
                .findByCodigoEncurtado(codigo);
    }


    @Test
    void deveConsultarPostgresNovamenteDepoisDaExpiracaoDoCache()
            throws InterruptedException {

        String codigo = CODIGO_TTL;
        String urlOriginal = "https://exemplo.com/cache-ttl";

        prepararUrl(codigo, urlOriginal);

        String primeiraResposta = service.pegarUrls(codigo);

        assertEquals(urlOriginal, primeiraResposta);
        assertNotNull(obterCache().get(codigo));

        verify(repository, times(1))
                .findByCodigoEncurtado(codigo);

        clearInvocations(repository);

        long limite = System.nanoTime()
                + Duration.ofSeconds(15).toNanos();

        while (obterCache().get(codigo) != null
                && System.nanoTime() < limite) {
            Thread.sleep(200);
        }

        assertNull(
                obterCache().get(codigo),
                "A chave deveria ter expirado após o TTL de 10 segundos"
        );

        String segundaResposta = service.pegarUrls(codigo);

        assertEquals(urlOriginal, segundaResposta);

        verify(repository, times(1))
                .findByCodigoEncurtado(codigo);
    }




}

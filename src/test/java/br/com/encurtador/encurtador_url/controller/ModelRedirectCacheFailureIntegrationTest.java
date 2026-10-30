
package br.com.encurtador.encurtador_url.controller;

import br.com.encurtador.encurtador_url.model.Url;
import br.com.encurtador.encurtador_url.repository.ModelRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@Transactional
class ModelRedirectCacheFailureIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ModelRepository repository;

    @MockitoSpyBean
    private ModelRepository repositorySpy;

    @MockitoSpyBean
    private CacheManager cacheManager;

    @Test
    void deveRedirecionarMesmoQuandoCacheFalhar()
            throws Exception {

        String codigo = "falhacache12";
        String urlOriginal = "https://exemplo.com/cache-indisponivel";

        Cache cacheReal = cacheManager.getCache("urls");

        if (cacheReal == null) {
            throw new IllegalStateException(
                    "Cache 'urls' não encontrado"
            );
        }

        cacheReal.evict(codigo);

        Url url = Url.builder()
                .codigoEncurtado(codigo)
                .urlOriginal(urlOriginal)
                .dataCriacao(LocalDateTime.now())
                .build();

        repository.saveAndFlush(url);

        clearInvocations(repositorySpy);

        Cache cacheComFalha = spy(cacheReal);

        doThrow(new RedisConnectionFailureException(
                "Falha simulada ao ler do Redis"
        )).when(cacheComFalha).get(codigo);

        doThrow(new RedisConnectionFailureException(
                "Falha simulada ao gravar no Redis"
        )).when(cacheComFalha).put(eq(codigo), any());

        doReturn(cacheComFalha)
                .when(cacheManager).getCache("urls");

        mockMvc.perform(get("/" + codigo)
                        .with(request -> {
                            request.setRemoteAddr("192.0.2.164");
                            return request;
                        }))
                .andExpect(status().isFound())
                .andExpect(header().string(
                        HttpHeaders.LOCATION,
                        urlOriginal
                ))
                .andExpect(content().string(""));

        verify(repositorySpy, times(1))
                .findByCodigoEncurtado(codigo);
    }
}

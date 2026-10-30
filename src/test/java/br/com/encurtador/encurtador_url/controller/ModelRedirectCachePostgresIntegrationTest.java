
package br.com.encurtador.encurtador_url.controller;

import br.com.encurtador.encurtador_url.model.Url;
import br.com.encurtador.encurtador_url.repository.ModelRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.mockito.Mockito.clearInvocations;
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
class ModelRedirectCachePostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ModelRepository repository;

    @Autowired
    private CacheManager cacheManager;

    @MockitoSpyBean
    private ModelRepository repositorySpy;

    private static final String CODIGO = "fluxocache12";
    private static final String URL_ORIGINAL =
            "https://exemplo.com/fluxo-cache";

    private Cache obterCache() {
        Cache cache = cacheManager.getCache("urls");

        if (cache == null) {
            throw new IllegalStateException(
                    "Cache 'urls' não encontrado"
            );
        }

        return cache;
    }

    @AfterEach
    void limparCache() {
        obterCache().evict(CODIGO);
    }

    @Test
    void deveConsultarPostgresUmaVezEUsarCacheNoSegundoAcesso()
            throws Exception {

        obterCache().evict(CODIGO);

        Url url = Url.builder()
                .codigoEncurtado(CODIGO)
                .urlOriginal(URL_ORIGINAL)
                .dataCriacao(LocalDateTime.now())
                .build();

        repository.saveAndFlush(url);

        clearInvocations(repositorySpy);

        mockMvc.perform(get("/" + CODIGO)
                        .with(request -> {
                            request.setRemoteAddr("192.0.2.152");
                            return request;
                        }))
                .andExpect(status().isFound())
                .andExpect(header().string(
                        HttpHeaders.LOCATION, URL_ORIGINAL
                ))
                .andExpect(content().string(""));

        verify(repositorySpy, times(1))
                .findByCodigoEncurtado(CODIGO);

        clearInvocations(repositorySpy);

        mockMvc.perform(get("/" + CODIGO)
                        .with(request -> {
                            request.setRemoteAddr("192.0.2.153");
                            return request;
                        }))
                .andExpect(status().isFound())
                .andExpect(header().string(
                        HttpHeaders.LOCATION, URL_ORIGINAL
                ))
                .andExpect(content().string(""));

        verify(repositorySpy, times(0))
                .findByCodigoEncurtado(CODIGO);
    }
}

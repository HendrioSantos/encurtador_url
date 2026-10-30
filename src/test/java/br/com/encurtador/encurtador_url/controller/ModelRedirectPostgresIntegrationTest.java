
package br.com.encurtador.encurtador_url.controller;

import br.com.encurtador.encurtador_url.model.Url;
import br.com.encurtador.encurtador_url.repository.ModelRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@Transactional
class ModelRedirectPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ModelRepository repository;

    @Test
    void deveRedirecionarParaUrlSalvaNoPostgres() throws Exception {
        String codigo = "redireciona1";
        String urlOriginal = "https://exemplo.com/destino";

        Url url = Url.builder()
                .codigoEncurtado(codigo)
                .urlOriginal(urlOriginal)
                .dataCriacao(LocalDateTime.now())
                .build();

        repository.saveAndFlush(url);

        mockMvc.perform(get("/" + codigo)
                        .with(request -> {
                            request.setRemoteAddr("192.0.2.151");
                            return request;
                        }))
                .andExpect(status().isFound())
                .andExpect(header().string(
                        HttpHeaders.LOCATION,
                        urlOriginal
                ))
                .andExpect(content().string(""));
    }
}

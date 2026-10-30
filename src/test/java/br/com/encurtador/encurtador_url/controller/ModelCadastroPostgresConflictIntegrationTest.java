
package br.com.encurtador.encurtador_url.controller;

import br.com.encurtador.encurtador_url.model.Url;
import br.com.encurtador.encurtador_url.repository.ModelRepository;
import br.com.encurtador.encurtador_url.utils.CodigoAleatorio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@Transactional
class ModelCadastroPostgresConflictIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ModelRepository repository;

    @MockitoBean
    private CodigoAleatorio codigoAleatorio;

    @Test
    void deveRetornar409QuandoCodigoEncurtadoJaExistir()
            throws Exception {

        String codigoDuplicado = "duplicado12";

        Url urlExistente = Url.builder()
                .codigoEncurtado(codigoDuplicado)
                .urlOriginal("https://exemplo.com/existente")
                .dataCriacao(LocalDateTime.now())
                .build();

        repository.saveAndFlush(urlExistente);

        when(codigoAleatorio.gerarCodigo())
                .thenReturn(codigoDuplicado);

        mockMvc.perform(post("/api/v1/model/criar-urls")
                        .with(request -> {
                            request.setRemoteAddr("192.0.2.163");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "urlOriginal": "https://exemplo.com/nova"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(content().string(
                        "Os dados fornecidos violam a integridade do banco de dados."
                ));
    }
}

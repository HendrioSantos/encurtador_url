
package br.com.encurtador.encurtador_url.controller;

import br.com.encurtador.encurtador_url.repository.ModelRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@Transactional
class ModelCadastroPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ModelRepository repository;

    @Test
    void deveCriarUrlViaHttpEPersistirNoPostgres() throws Exception {
        String urlOriginal = "https://exemplo.com/integracao";

        var resposta = mockMvc.perform(
                        post("/api/v1/model/criar-urls")
                                .with(request -> {
                                    request.setRemoteAddr("192.0.2.150");
                                    return request;
                                })
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "urlOriginal": "https://exemplo.com/integracao"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.url").isNotEmpty())
                .andExpect(jsonPath("$.urlOriginal")
                        .value(urlOriginal))
                .andExpect(jsonPath("$.horario").isNotEmpty())
                .andReturn();

        String corpo = resposta.getResponse().getContentAsString();
        String codigo = JsonPath.read(corpo, "$.url");

        var urlSalva = repository.findByCodigoEncurtado(codigo);

        assertTrue(urlSalva.isPresent());
        assertEquals(
                urlOriginal,
                urlSalva.get().getUrlOriginal()
        );
        assertEquals(
                codigo,
                urlSalva.get().getCodigoEncurtado()
        );
    }
}

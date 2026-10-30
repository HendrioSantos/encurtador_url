package br.com.encurtador.encurtador_url.controller;

import br.com.encurtador.encurtador_url.repository.ModelRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class ModelCadastroTestContainersIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("encurtador_teste")
                    .withUsername("teste")
                    .withPassword("teste");

    @DynamicPropertySource
    static void configurarPostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ModelRepository repository;

    @Test
    void deveCriarUrlViaHttpEPersistirNoPostgresDoContainer()
            throws Exception {

        String urlOriginal = "https://exemplo.com/integracao-container";

        var resposta = mockMvc.perform(
                        post("/api/v1/model/criar-urls")
                                .with(request -> {
                                    request.setRemoteAddr("192.0.2.150");
                                    return request;
                                })
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "urlOriginal":
                                            "https://exemplo.com/integracao-container"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andReturn();

        String corpo = resposta.getResponse().getContentAsString();
        String codigo = JsonPath.read(corpo, "$.url");

        var urlSalva = repository.findByCodigoEncurtado(codigo);

        assertTrue(urlSalva.isPresent());
        assertEquals(urlOriginal, urlSalva.get().getUrlOriginal());
        assertEquals(codigo, urlSalva.get().getCodigoEncurtado());
    }
}

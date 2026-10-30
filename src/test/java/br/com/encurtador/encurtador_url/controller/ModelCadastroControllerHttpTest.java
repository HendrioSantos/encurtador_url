package br.com.encurtador.encurtador_url.controller;

import br.com.encurtador.encurtador_url.dto.ModelCriar;
import br.com.encurtador.encurtador_url.dto.ModelListagem;
import br.com.encurtador.encurtador_url.service.ModelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.CannotCreateTransactionException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ModelCadastroController.class)
public class ModelCadastroControllerHttpTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ModelService service;

    @Test
    void deveRetornar201QuandoUrlForValida() throws Exception {
        when(service.criarUrl(any(ModelCriar.class)))
                .thenReturn(mock(ModelListagem.class));

        mockMvc.perform(post("/api/v1/model/criar-urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "urlOriginal": "https://exemplo.com"
                                }
                                """))
                .andExpect(status().isCreated());

        verify(service).criarUrl(any(ModelCriar.class));
    }


    @Test
    void deveRetornar400QuandoUrlEstiverVazia() throws Exception {
        mockMvc.perform(post("/api/v1/model/criar-urls")
                        .with(request -> {
                            request.setRemoteAddr("192.0.2.101");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "urlOriginal": ""
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void deveRetornar400QuandoProtocoloNaoForPermitido()
            throws Exception {
        mockMvc.perform(post("/api/v1/model/criar-urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "urlOriginal": "ftp://exemplo.com"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void deveRetornar400QuandoUrlUltrapassar2048Caracteres()
            throws Exception {

        String urlLonga = "https://exemplo.com/" + "a".repeat(2040);

        mockMvc.perform(post("/api/v1/model/criar-urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"urlOriginal": "%s"}
                                """.formatted(urlLonga)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }


    @Test
    void deveRetornar500QuandoOcorrerFalhaInesperadaAoSalvarUrl()
            throws Exception {

        when(service.criarUrl(
                any(ModelCriar.class)
        )).thenThrow(new RuntimeException("Falha simulada no banco"));

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .post("/api/v1/model/criar-urls")
                                .with(request -> {
                                    request.setRemoteAddr("192.0.2.160");
                                    return request;
                                })
                                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "urlOriginal": "https://exemplo.com"
                                        }
                                        """)
                )
                .andExpect(
                        MockMvcResultMatchers
                                .status().isInternalServerError()
                )
                .andExpect(
                        MockMvcResultMatchers
                                .content().string(
                                        "Ocorreu um erro interno no servidor. Tente novamente mais tarde."
                                )
                );
    }


    @Test
    void deveRetornar503QuandoNaoForPossivelIniciarTransacao()
            throws Exception {

        when(service.criarUrl(
                any(ModelCriar.class)
        )).thenThrow(new CannotCreateTransactionException(
                "Falha simulada ao conectar ao banco"
        ));

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .post("/api/v1/model/criar-urls")
                                .with(request -> {
                                    request.setRemoteAddr("192.0.2.161");
                                    return request;
                                })
                                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "urlOriginal": "https://exemplo.com"
                                        }
                                        """)
                )
                .andExpect(
                        MockMvcResultMatchers
                                .status().isServiceUnavailable()
                )
                .andExpect(
                        MockMvcResultMatchers
                                .content().string(
                                        "Serviço temporariamente indisponível. Tente novamente mais tarde."
                                )
                );
    }


    @Test
    void deveRetornar409QuandoOcorrerViolacaoDeIntegridade()
            throws Exception {

        when(service.criarUrl(
                any(ModelCriar.class)
        )).thenThrow(new DataIntegrityViolationException(
                "Violação simulada de integridade"
        ));

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .post("/api/v1/model/criar-urls")
                                .with(request -> {
                                    request.setRemoteAddr("192.0.2.162");
                                    return request;
                                })
                                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "urlOriginal": "https://exemplo.com"
                                        }
                                        """)
                )
                .andExpect(
                        MockMvcResultMatchers
                                .status().isConflict()
                )
                .andExpect(
                        MockMvcResultMatchers
                                .content().string(
                                        "Os dados fornecidos violam a integridade do banco de dados."
                                )
                );
    }

}

package br.com.encurtador.encurtador_url.controller;

import br.com.encurtador.encurtador_url.exception.UrlNaoEncontradaException;
import br.com.encurtador.encurtador_url.service.ModelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ModelRedirectController.class)
public class ModelRedirectControllerHttpTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ModelService service;

    @Test
    void deveRedirecionarQuandoCodigoExistir() throws Exception {
        String codigo = "abc123";
        String urlOriginal = "https://exemplo.com";

        when(service.pegarUrls(codigo)).thenReturn(urlOriginal);

        mockMvc.perform(get("/" + codigo))
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, urlOriginal))
                .andExpect(content().string(""));

        verify(service).pegarUrls(codigo);
    }

    @Test
    void deveRetornar404QuandoCodigoNaoExistir() throws Exception {
        String codigo = "inexistente";

        when(service.pegarUrls(codigo))
                .thenThrow(new UrlNaoEncontradaException(
                        "Url não encontrada ou expirada"));

        mockMvc.perform(get("/" + codigo))
                .andExpect(status().isNotFound());

        verify(service).pegarUrls(codigo);
    }


    @Test
    void deveRetornar405QuandoMetodoHttpNaoForSuportado()
            throws Exception {

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .post("/abc123")
                )
                .andExpect(status().isMethodNotAllowed());

        verifyNoInteractions(service);
    }


    @Test
    void deveRetornar503QuandoNaoForPossivelCriarTransacao()
            throws Exception {

        when(service.pegarUrls("abc123"))
                .thenThrow(new org.springframework.transaction
                        .CannotCreateTransactionException(
                        "Banco de dados indisponível"));

        mockMvc.perform(get("/abc123"))
                .andExpect(status().isServiceUnavailable());

        verify(service).pegarUrls("abc123");
    }


    @Test
    void deveRetornar500QuandoOcorrerErroInesperado()
            throws Exception {

        when(service.pegarUrls("abc123"))
                .thenThrow(new IllegalStateException(
                        "Erro interno simulado"));

        mockMvc.perform(get("/abc123"))
                .andExpect(status().isInternalServerError());

        verify(service).pegarUrls("abc123");
    }


}

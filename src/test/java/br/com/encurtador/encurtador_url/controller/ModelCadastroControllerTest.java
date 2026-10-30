package br.com.encurtador.encurtador_url.controller;

import br.com.encurtador.encurtador_url.dto.ModelCriar;
import br.com.encurtador.encurtador_url.dto.ModelListagem;
import br.com.encurtador.encurtador_url.service.ModelService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModelCadastroControllerTest {

    @Mock
    private ModelService service;

    @InjectMocks
    private ModelCadastroController controller;

    @Test
    void deveRetornarCreatedAoCriarUrl() {
        ModelCriar dados = new ModelCriar("https://exemplo.com");

        ModelListagem respostaEsperada = org.mockito.Mockito.mock(ModelListagem.class);

        when(service.criarUrl(dados)).thenReturn(respostaEsperada);

        ResponseEntity<ModelListagem> resposta = controller.fazerUrl(dados);

        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
        assertSame(respostaEsperada, resposta.getBody());
        verify(service).criarUrl(dados);
    }

}
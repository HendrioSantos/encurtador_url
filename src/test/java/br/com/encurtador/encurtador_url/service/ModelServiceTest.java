package br.com.encurtador.encurtador_url.service;

import br.com.encurtador.encurtador_url.dto.ModelCriar;
import br.com.encurtador.encurtador_url.dto.ModelListagem;
import br.com.encurtador.encurtador_url.exception.UrlNaoEncontradaException;
import br.com.encurtador.encurtador_url.model.Url;
import br.com.encurtador.encurtador_url.repository.ModelRepository;
import br.com.encurtador.encurtador_url.utils.CodigoAleatorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModelServiceTest {

    @Mock
    private ModelRepository repository;

    @Mock
    private CodigoAleatorio codigoAleatorio;

    @InjectMocks
    private ModelService modelService;

    @Test
    void deveRetornarUrlOriginalQuandoCodigoExiste() {
        String codigo = "abc123";
        String urlOriginal = "https://exemplo.com";

        Url url = Url.builder()
                .codigoEncurtado(codigo)
                .urlOriginal(urlOriginal)
                .build();

        when(repository.findByCodigoEncurtado(codigo))
                .thenReturn(Optional.of(url));

        String resultado = modelService.pegarUrls(codigo);

        assertEquals(urlOriginal, resultado);
        verify(repository).findByCodigoEncurtado(codigo);
    }

    @Test
    void deveLancarExcecaoQuandoCodigoNaoExiste() {
        String codigo = "inexistente";

        when(repository.findByCodigoEncurtado(codigo))
                .thenReturn(Optional.empty());

        assertThrows(
                UrlNaoEncontradaException.class,
                () -> modelService.pegarUrls(codigo)
        );

        verify(repository).findByCodigoEncurtado(codigo);
    }


    @Test
    void deveCriarUrlESalvarComCodigoGerado() {
        String urlOriginal = "https://exemplo.com";
        String codigoGerado = "abc123XYZ789";

        ModelCriar dados = new ModelCriar(urlOriginal);

        when(codigoAleatorio.gerarCodigo())
                .thenReturn(codigoGerado);

        when(repository.save(any(Url.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        ModelListagem resultado = modelService.criarUrl(dados);

        assertNotNull(resultado);

        verify(codigoAleatorio).gerarCodigo();

        verify(repository).save(argThat(url ->
                url.getUrlOriginal().equals(urlOriginal)
                        && url.getCodigoEncurtado().equals(codigoGerado)
                        && url.getDataCriacao() != null
        ));
    }


    @Test
    void deveRetornarDtoComDadosDaUrlCriada() {
        String urlOriginal = "https://exemplo.com";
        String codigoGerado = "abc123XYZ789";

        ModelCriar dados = new ModelCriar(urlOriginal);

        when(codigoAleatorio.gerarCodigo())
                .thenReturn(codigoGerado);

        when(repository.save(any(Url.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        ModelListagem resultado = modelService.criarUrl(dados);

        assertEquals(codigoGerado, resultado.url());
        assertEquals(urlOriginal, resultado.urlOriginal());
        assertNotNull(resultado.horario());

        verify(repository).save(any(Url.class));
    }



}
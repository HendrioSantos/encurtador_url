package br.com.encurtador.encurtador_url.service;

import br.com.encurtador.encurtador_url.dto.ModelCriar;
import br.com.encurtador.encurtador_url.dto.ModelListagem;
import br.com.encurtador.encurtador_url.exception.UrlNaoEncontradaException;
import br.com.encurtador.encurtador_url.model.Url;
import br.com.encurtador.encurtador_url.repository.ModelRepository;
import br.com.encurtador.encurtador_url.utils.CodigoAleatorio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModelService {

    private final ModelRepository repository;
    private final CodigoAleatorio codigoAleatorio;

    @Transactional(readOnly = true)
    @Cacheable(value = "urls", key = "#codigo")
    public String pegarUrls(String codigo){
        log.info("Buscando URL real para o código encurtado: {}", codigo);
        return repository.findByCodigoEncurtado(codigo).map(Url::getUrlOriginal)
                .orElseThrow(() -> {
                    log.error("Tentativa falha de redirecionamento. Código inválido: {}", codigo);
                    return new UrlNaoEncontradaException("Url não encontrada ou expirada");
                });
    }

    @Transactional
    public ModelListagem criarUrl(ModelCriar dados){
        var urlBasica = Url.builder()
                .urlOriginal(dados.urlOriginal())
                .codigoEncurtado(codigoAleatorio.gerarCodigo())
                .dataCriacao(LocalDateTime.now())
                .build();
        var urlSalva = repository.save(urlBasica);
        return new ModelListagem(urlSalva);
    }

}

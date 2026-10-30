package br.com.encurtador.encurtador_url.repository;

import br.com.encurtador.encurtador_url.model.Url;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ModelRepositoryTest {

    @Autowired
    private ModelRepository repository;

    @Test
    void deveSalvarEConsultarUrlPeloCodigoEncurtado() {
        Url url = Url.builder()
                .urlOriginal("https://exemplo.com")
                .codigoEncurtado("teste123")
                .dataCriacao(LocalDateTime.now())
                .build();

        repository.save(url);

        var resultado = repository.findByCodigoEncurtado("teste123");

        assertTrue(resultado.isPresent());
        assertEquals("https://exemplo.com",
                resultado.get().getUrlOriginal());
        assertEquals("teste123",
                resultado.get().getCodigoEncurtado());
    }

    @Test
    void deveRetornarVazioQuandoCodigoNaoExistir() {
        var resultado =
                repository.findByCodigoEncurtado("naoexiste");

        assertTrue(resultado.isEmpty());
    }

}
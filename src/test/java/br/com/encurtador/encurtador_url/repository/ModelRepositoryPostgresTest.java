package br.com.encurtador.encurtador_url.repository;

import br.com.encurtador.encurtador_url.model.Url;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
public class ModelRepositoryPostgresTest {

    @Autowired
    private ModelRepository repository;

    @Test
    void deveSalvarEConsultarUrlPeloCodigoEncurtado() {
        Url url = Url.builder()
                .urlOriginal("https://exemplo.com")
                .codigoEncurtado("pgteste123")
                .dataCriacao(LocalDateTime.now())
                .build();

        repository.saveAndFlush(url);

        var resultado =
                repository.findByCodigoEncurtado("pgteste123");

        assertTrue(resultado.isPresent());
        assertEquals(
                "https://exemplo.com",
                resultado.get().getUrlOriginal()
        );
        assertEquals(
                "pgteste123",
                resultado.get().getCodigoEncurtado()
        );
    }

    @Test
    void deveRetornarVazioQuandoCodigoNaoExistir() {
        var resultado =
                repository.findByCodigoEncurtado("naoexiste");

        assertTrue(resultado.isEmpty());
    }


    @Test
    void deveImpedirDoisRegistrosComOMesmoCodigoEncurtadoRunTimeException() {
        Url primeiraUrl = Url.builder()
                .urlOriginal("https://exemplo.com/primeira")
                .codigoEncurtado("duplicado12")
                .dataCriacao(LocalDateTime.now())
                .build();

        Url segundaUrl = Url.builder()
                .urlOriginal("https://exemplo.com/segunda")
                .codigoEncurtado("duplicado12")
                .dataCriacao(LocalDateTime.now())
                .build();

        repository.saveAndFlush(primeiraUrl);

        assertThrows(
                RuntimeException.class,
                () -> {
                    repository.save(segundaUrl);
                    repository.flush();
                }
        );
    }

    @Test
    void deveImpedirDoisRegistrosComOMesmoCodigoEncurtadoDataIntegrityViolationException() {
        Url primeiraUrl = Url.builder()
                .urlOriginal("https://exemplo.com/primeira")
                .codigoEncurtado("duplicado12")
                .dataCriacao(LocalDateTime.now())
                .build();

        Url segundaUrl = Url.builder()
                .urlOriginal("https://exemplo.com/segunda")
                .codigoEncurtado("duplicado12")
                .dataCriacao(LocalDateTime.now())
                .build();

        repository.saveAndFlush(primeiraUrl);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> {
                    repository.save(segundaUrl);
                    repository.flush();
                }
        );
    }



    @Test
    void deveRejeitarCodigoEncurtadoDuplicadoNoPostgres() {
        String codigo = "duplicado12";

        Url primeiraUrl = Url.builder()
                .codigoEncurtado(codigo)
                .urlOriginal("https://exemplo.com/primeira")
                .dataCriacao(LocalDateTime.now())
                .build();

        Url segundaUrl = Url.builder()
                .codigoEncurtado(codigo)
                .urlOriginal("https://exemplo.com/segunda")
                .dataCriacao(LocalDateTime.now())
                .build();

        repository.saveAndFlush(primeiraUrl);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(segundaUrl)
        );
    }

}

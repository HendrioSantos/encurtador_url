package br.com.encurtador.encurtador_url.repository;

import br.com.encurtador.encurtador_url.model.Url;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class ModelRepositoryConcorrenciaPostgresTest {

    @Autowired
    private ModelRepository repository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void devePermitirSomenteUmaUrlQuandoDuasThreadsUsamMesmoCodigo()
            throws Exception {

        String codigo = java.util.UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12);

        CountDownLatch prontas = new CountDownLatch(2);
        CountDownLatch iniciar = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate transacao =
                new TransactionTemplate(transactionManager);

        try {
            Future<Boolean> primeira = executor.submit(() ->
                    tentarSalvar(codigo, "https://exemplo.com/um",
                            prontas, iniciar, transacao));

            Future<Boolean> segunda = executor.submit(() ->
                    tentarSalvar(codigo, "https://exemplo.com/dois",
                            prontas, iniciar, transacao));

            prontas.await();
            iniciar.countDown();

            boolean resultadoPrimeiro = primeira.get();
            boolean resultadoSegundo = segunda.get();

            assertNotEquals(
                    resultadoPrimeiro,
                    resultadoSegundo,
                    "Uma gravação deve passar e a outra deve ser rejeitada"
            );

            assertTrue(
                    repository.findByCodigoEncurtado(codigo).isPresent(),
                    "Deve existir somente uma URL com esse código"
            );
        } finally {
            iniciar.countDown();
            executor.shutdownNow();
        }
    }

    private boolean tentarSalvar(
            String codigo,
            String urlOriginal,
            CountDownLatch prontas,
            CountDownLatch iniciar,
            TransactionTemplate transacao) throws InterruptedException {

        prontas.countDown();
        iniciar.await();

        try {
            transacao.execute(status -> {
                Url url = Url.builder()
                        .codigoEncurtado(codigo)
                        .urlOriginal(urlOriginal)
                        .dataCriacao(LocalDateTime.now())
                        .build();

                repository.saveAndFlush(url);
                return null;
            });

            return true;
        } catch (DataIntegrityViolationException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}

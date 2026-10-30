package br.com.encurtador.encurtador_url.repository;

import br.com.encurtador.encurtador_url.model.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModelRepository extends JpaRepository<Url, Long> {
    Optional<Url> findByCodigoEncurtado(String codigoEncurtado);
}

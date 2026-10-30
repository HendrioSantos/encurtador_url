package br.com.encurtador.encurtador_url.exception.dto;

import br.com.encurtador.encurtador_url.utils.MinhaException;
import org.springframework.validation.FieldError;

public record ErroASimples(
        FieldError fieldError,
        MinhaException e
) {
}

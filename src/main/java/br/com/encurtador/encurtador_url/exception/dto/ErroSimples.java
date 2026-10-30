package br.com.encurtador.encurtador_url.exception.dto;

import br.com.encurtador.encurtador_url.utils.MinhaException;

import java.time.LocalDateTime;

public record ErroSimples(
        MinhaException e,
        LocalDateTime localDateTime
) {
}

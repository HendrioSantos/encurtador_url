package br.com.encurtador.encurtador_url.dto;

import br.com.encurtador.encurtador_url.model.Url;

import java.time.LocalDateTime;

public record ModelListagem(
        String url,
        String urlOriginal,
        LocalDateTime horario
) {
    public ModelListagem(Url model) {
        this(
                model.getCodigoEncurtado(),
                model.getUrlOriginal(),
                model.getDataCriacao()
        );
    }
}

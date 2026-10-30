package br.com.encurtador.encurtador_url.dto;

import br.com.encurtador.encurtador_url.model.Url;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record ModelCriar(
        @NotBlank(message = "A Url não pode estar vazia")
        @Size(max = 2048, message = "A Url não pode ter mais de 2048 caracteres")
        @URL(regexp = "^(http|https)://.*$",
                message = "Somente Url válida usando http|https")
        String urlOriginal
) {
    public ModelCriar(Url model) {
        this(
                model.getUrlOriginal()
        );
    }
}

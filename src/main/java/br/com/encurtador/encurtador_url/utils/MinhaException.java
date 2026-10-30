package br.com.encurtador.encurtador_url.utils;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class MinhaException extends RuntimeException {

    private final HttpStatus status;

    public MinhaException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public MinhaException(HttpStatus status) {
        this.status = status;
    }

    public MinhaException(String message) {
        super(message);
        this.status = HttpStatus.NOT_FOUND;
    }
}

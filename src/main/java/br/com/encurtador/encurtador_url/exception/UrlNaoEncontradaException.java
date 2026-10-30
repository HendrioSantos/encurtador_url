package br.com.encurtador.encurtador_url.exception;

public class UrlNaoEncontradaException extends RuntimeException {

    public UrlNaoEncontradaException() {
    }

    public UrlNaoEncontradaException(String s) {
        super(s);
    }

    public UrlNaoEncontradaException(String message, Throwable cause) {
        super(message, cause);
    }

    public UrlNaoEncontradaException(Throwable cause) {
        super(cause);
    }
}

package br.com.encurtador.encurtador_url.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalHandlerException {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> tratarErroCodigoInvalido(IllegalArgumentException ex) {
        log.warn("Requisição recusada por argumento inválido: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(UrlNaoEncontradaException.class)
    public ResponseEntity<String> tratarErroUrlNaoEncontrada(UrlNaoEncontradaException ex) {
        log.warn("Falha ao buscar recurso: {}, erro: {}", ex.getMessage(), HttpStatus.NOT_FOUND);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> tratarErrosDeValidacao(
            MethodArgumentNotValidException ex) {

        Map<String, String> erros = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                erros.put(error.getField(), error.getDefaultMessage())
        );

        log.warn("Falha na validação dos dados de entrada: {}", erros);

        Map<String, Object> resposta = new HashMap<>();
        resposta.put("mensagem", "Os dados enviados são inválidos.");
        resposta.put("erros", erros);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, String>> tratarMetodoNaoSuportado(HttpRequestMethodNotSupportedException ex) {
        Map<String, String> erro = new HashMap<>();
        erro.put("Erro", "Método Http não suportado para esta requisição. Verifique na documentação da API");
        erro.put("Método inválido", ex.getMethod());
        erro.put("Métodos suportados", ex.getSupportedHttpMethods() == null
                ? "Consulte a documentação da API" : ex.getSupportedHttpMethods().toString());
        log.warn("Tentativa de requisição com método HTTP não suportado: {}, " +
                "onde os métodos suportados são: {}", ex.getMethod(), ex.getSupportedHttpMethods());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(erro);
    }

    @ExceptionHandler(CannotCreateTransactionException.class)
    public ResponseEntity<String> handlerFalhaTransacao(
            CannotCreateTransactionException exception) {
        log.error("Não foi possível iniciar a transação com o banco de dados.", exception);
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Serviço temporariamente indisponível. Tente novamente mais tarde.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> tratarViolacaoIntegridadeDados(DataIntegrityViolationException ex) {
        log.error("Violação de integridade dos dados: ", ex);
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body("Os dados fornecidos violam a integridade do banco de dados.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> tratarErroInesperado(Exception ex) {
        log.error("Erro interno inesperado no servidor: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Ocorreu um erro interno no servidor. Tente novamente mais tarde.");
    }
}

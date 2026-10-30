package br.com.encurtador.encurtador_url.controller;

import br.com.encurtador.encurtador_url.dto.ModelCriar;
import br.com.encurtador.encurtador_url.dto.ModelListagem;
import br.com.encurtador.encurtador_url.service.ModelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/model")
@RequiredArgsConstructor
public class ModelCadastroController {

    private final ModelService service;

    @PostMapping("/criar-urls")
    public ResponseEntity<ModelListagem> fazerUrl(@RequestBody @Valid ModelCriar dados){
        var url = service.criarUrl(dados);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(url);
    }

}


package br.com.encurtador.encurtador_url.utils;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class CodigoAleatorio {

    private final SecureRandom random;

    private static final String CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public CodigoAleatorio(SecureRandom random) {
        this.random = random;
    }

    public String gerarCodigo(){
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            int indice = random.nextInt(CHARACTERS.length());
            char caracteres = CHARACTERS.charAt(indice);
            stringBuilder.append(caracteres);
        }
        return stringBuilder.toString();
    }

}

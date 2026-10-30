package br.com.encurtador.encurtador_url.utils;

public class Base62 {

    private static final String CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = CHARACTERS.length();

    public static String encode(Long id){
        if (id == null || id <= 0){
            throw new IllegalArgumentException("O id deve ser válido");
        }

        var construtorString = new StringBuilder();
        while(id > 0){
            int resto = (int) (id % BASE);
            construtorString.append(CHARACTERS.charAt(resto));
            id = id / BASE;
        }

        return construtorString.reverse().toString();
    }

    public static Long decode(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("O código não pode estar vazio.");
        }
        long id = 0;

        for (int i = 0; i < codigo.length(); i++) {
            char c = codigo.charAt(i);
            int valorDoCaractere = CHARACTERS.indexOf(c);
            if (valorDoCaractere == -1) {
                throw new IllegalArgumentException("O código contém caracteres inválidos.");
            }
            id = id * BASE + valorDoCaractere;
        }

        return id;
    }

}

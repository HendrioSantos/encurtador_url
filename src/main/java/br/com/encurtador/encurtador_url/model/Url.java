package br.com.encurtador.encurtador_url.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "url")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "codigoEncurtado")
@Builder
public class Url {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url_original", nullable = false)
    private String urlOriginal;

    @Column(name = "codigo_encurtado", unique = true, length = 12)
    private String codigoEncurtado;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

}

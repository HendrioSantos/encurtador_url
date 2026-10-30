package br.com.encurtador.encurtador_url;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class EncurtadorUrlApplication {

	public static void main(String[] args) {
		SpringApplication.run(EncurtadorUrlApplication.class, args);
	}

}

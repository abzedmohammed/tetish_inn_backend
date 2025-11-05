package tetish_inn_backend.tetish_inn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@SpringBootApplication
public class TetishInnApplication {
    public static void main(String[] args) {
		SpringApplication.run(TetishInnApplication.class, args);
	}

}

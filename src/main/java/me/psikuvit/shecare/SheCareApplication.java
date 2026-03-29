package me.psikuvit.shecare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SheCareApplication {

    public static void main(String[] args) {
        SpringApplication.run(SheCareApplication.class, args);
    }

}

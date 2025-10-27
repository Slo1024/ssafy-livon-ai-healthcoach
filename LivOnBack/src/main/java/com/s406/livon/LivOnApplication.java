package com.s406.livon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class LivOnApplication {

    public static void main(String[] args) {
        SpringApplication.run(LivOnApplication.class, args);
    }

}

package org.ayomide;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("org.ayomide.repository")
@EntityScan("org.ayomide.model")
public class Main {
    public static void main(String[] args) {

        SpringApplication.run(Main.class, args);

    }
}
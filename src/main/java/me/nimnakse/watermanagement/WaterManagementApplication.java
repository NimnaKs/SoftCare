package me.nimnakse.watermanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class WaterManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(WaterManagementApplication.class, args);
    }
}

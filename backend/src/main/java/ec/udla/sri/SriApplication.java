package ec.udla.sri;

import ec.udla.sri.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class SriApplication {
    public static void main(String[] args) {
        SpringApplication.run(SriApplication.class, args);
    }
}

package ec.udla.sri.config;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        List<String> corsOrigins,
        Cache cache,
        Upstream upstream,
        Politicas politicas) {

    public record Cache(String provider) {}

    public record Upstream(String sriBaseUrl, String antBaseUrl, Duration connectTimeout, Duration readTimeout) {}

    /** fresca: se sirve de caché sin consultar el origen. retencion: vida máxima del último dato bueno. */
    public record Politica(Duration fresca, Duration retencion) {}

    public record Politicas(Politica contribuyente, Politica vehiculo, Politica licencia) {}
}

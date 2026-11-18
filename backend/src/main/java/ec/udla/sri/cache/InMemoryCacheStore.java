package ec.udla.sri.cache;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Solo para desarrollo local y pruebas (CACHE_PROVIDER=memory). */
@Component
@ConditionalOnProperty(name = "app.cache.provider", havingValue = "memory")
public class InMemoryCacheStore implements CacheStore {

    private record Entrada(String valor, Instant expira) {}

    private final ConcurrentHashMap<String, Entrada> datos = new ConcurrentHashMap<>();

    @Override
    public Optional<String> get(String key) {
        Entrada e = datos.get(key);
        if (e == null) return Optional.empty();
        if (Instant.now().isAfter(e.expira())) {
            datos.remove(key);
            return Optional.empty();
        }
        return Optional.of(e.valor());
    }

    @Override
    public void put(String key, String value, Duration ttl) {
        datos.put(key, new Entrada(value, Instant.now().plus(ttl)));
    }
}

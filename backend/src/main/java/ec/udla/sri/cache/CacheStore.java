package ec.udla.sri.cache;

import java.time.Duration;
import java.util.Optional;

/** Almacén clave/valor con expiración. Un fallo del almacén nunca debe romper la consulta. */
public interface CacheStore {

    Optional<String> get(String key);

    void put(String key, String value, Duration ttl);
}

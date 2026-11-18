package ec.udla.sri.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ec.udla.sri.config.AppProperties.Politica;
import ec.udla.sri.error.ApiException;
import ec.udla.sri.error.OrigenNoDisponibleException;
import ec.udla.sri.model.Meta;
import ec.udla.sri.model.Resultado;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Patrón "último dato bueno" (stale-on-error sobre cache-aside):
 * 1. Dato en caché más joven que {@code fresca}  -> se responde desde caché sin llamar al externo.
 * 2. Si no, se consulta el externo; si responde bien, se guarda con TTL = {@code retencion}.
 * 3. Si el externo cae y hay dato guardado, se devuelve ese dato marcado como obsoleto, con aviso.
 * 4. Si el externo cae y no hay dato guardado, se responde 503.
 * Las excepciones de negocio (no encontrado, regla) se propagan y nunca se cachean.
 */
@Component
public class ResilientCache {

    private static final Logger log = LoggerFactory.getLogger(ResilientCache.class);

    record Sobre<T>(Instant guardadoEn, T valor) {}

    private final CacheStore store;
    private final ObjectMapper mapper;

    public ResilientCache(CacheStore store, ObjectMapper mapper) {
        this.store = store;
        this.mapper = mapper;
    }

    public <T> Resultado<T> obtener(String clave, Class<T> tipo, Politica politica, String fuente, Supplier<T> origen) {
        Optional<Sobre<T>> guardado = leer(clave, tipo);

        if (guardado.isPresent()) {
            Duration edad = Duration.between(guardado.get().guardadoEn(), Instant.now());
            if (edad.compareTo(politica.fresca()) < 0) {
                return new Resultado<>(guardado.get().valor(),
                        new Meta(Meta.CACHE_FRESCA, guardado.get().guardadoEn(), edad.toSeconds(), false, null));
            }
        }

        try {
            T valor = origen.get();
            Instant ahora = Instant.now();
            escribir(clave, new Sobre<>(ahora, valor), politica.retencion());
            return new Resultado<>(valor, new Meta(Meta.ORIGEN, ahora, 0, false, null));
        } catch (OrigenNoDisponibleException e) {
            log.warn("Origen {} no disponible para '{}': {}", fuente, clave, e.getMessage());
            if (guardado.isEmpty()) {
                throw ApiException.servicioNoDisponible(fuente);
            }
            Sobre<T> s = guardado.get();
            Duration edad = Duration.between(s.guardadoEn(), Instant.now());
            return new Resultado<>(s.valor(),
                    new Meta(Meta.CACHE_OBSOLETA, s.guardadoEn(), edad.toSeconds(), true,
                            "El servicio de " + fuente + " no está disponible. Se muestra el último dato correcto, "
                                    + "obtenido " + describir(edad) + "; puede no estar actualizado."));
        }
    }

    private <T> Optional<Sobre<T>> leer(String clave, Class<T> tipo) {
        return store.get(clave).flatMap(json -> {
            try {
                var tipoSobre = mapper.getTypeFactory().constructParametricType(Sobre.class, tipo);
                return Optional.of(mapper.<Sobre<T>>readValue(json, tipoSobre));
            } catch (JsonProcessingException | RuntimeException e) {
                log.warn("Entrada de caché ilegible para '{}', se ignora", clave);
                return Optional.empty();
            }
        });
    }

    private <T> void escribir(String clave, Sobre<T> sobre, Duration ttl) {
        try {
            store.put(clave, mapper.writeValueAsString(sobre), ttl);
        } catch (JsonProcessingException e) {
            log.warn("No se pudo serializar '{}' para la caché", clave);
        }
    }

    static String describir(Duration d) {
        long min = d.toMinutes();
        if (min < 1) return "hace menos de un minuto";
        if (min < 60) return "hace " + min + (min == 1 ? " minuto" : " minutos");
        long h = d.toHours();
        if (h < 48) return "hace " + h + (h == 1 ? " hora" : " horas");
        return "hace " + d.toDays() + " días";
    }
}

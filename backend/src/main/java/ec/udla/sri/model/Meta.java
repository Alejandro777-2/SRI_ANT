package ec.udla.sri.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

/**
 * Procedencia del dato.
 * origen: ORIGEN (consulta en vivo) | CACHE_FRESCA | CACHE_OBSOLETA (el externo falló y se devuelve el último dato bueno).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Meta(String origen, Instant obtenidoEn, long edadSegundos, boolean obsoleto, String aviso) {

    public static final String ORIGEN = "ORIGEN";
    public static final String CACHE_FRESCA = "CACHE_FRESCA";
    public static final String CACHE_OBSOLETA = "CACHE_OBSOLETA";
}

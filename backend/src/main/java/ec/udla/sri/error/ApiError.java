package ec.udla.sri.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiError(
        int estado,
        String codigo,
        String mensaje,
        List<ApiException.Detalle> detalles,
        String ruta,
        Instant timestamp) {}

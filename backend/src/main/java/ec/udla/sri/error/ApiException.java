package ec.udla.sri.error;

import java.util.List;
import org.springframework.http.HttpStatus;

/** Error de negocio/API con código estable y mensaje legible. */
public class ApiException extends RuntimeException {

    public record Detalle(String campo, String codigo, String mensaje) {}

    private final HttpStatus estado;
    private final String codigo;
    private final List<Detalle> detalles;

    public ApiException(HttpStatus estado, String codigo, String mensaje, List<Detalle> detalles) {
        super(mensaje);
        this.estado = estado;
        this.codigo = codigo;
        this.detalles = detalles == null ? List.of() : detalles;
    }

    public ApiException(HttpStatus estado, String codigo, String mensaje) {
        this(estado, codigo, mensaje, List.of());
    }

    public HttpStatus estado() { return estado; }
    public String codigo() { return codigo; }
    public List<Detalle> detalles() { return detalles; }

    public static ApiException validacion(List<Detalle> detalles) {
        return new ApiException(HttpStatus.BAD_REQUEST, "VALIDACION_FALLIDA",
                "Los datos enviados no son válidos.", detalles);
    }

    public static ApiException noEncontrado(String codigo, String mensaje) {
        return new ApiException(HttpStatus.NOT_FOUND, codigo, mensaje);
    }

    public static ApiException reglaNegocio(String codigo, String mensaje) {
        return new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, codigo, mensaje);
    }

    public static ApiException servicioNoDisponible(String fuente) {
        return new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "SERVICIO_EXTERNO_NO_DISPONIBLE",
                "El servicio de " + fuente + " no está disponible y aún no hay un dato guardado para esta consulta. "
                        + "Intente nuevamente en unos minutos.");
    }
}

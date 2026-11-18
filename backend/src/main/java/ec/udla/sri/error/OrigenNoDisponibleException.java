package ec.udla.sri.error;

/**
 * El servicio externo falló (timeout, 5xx, respuesta ilegible). Dispara el uso del último dato bueno.
 * Las respuestas de negocio (p. ej. "no es contribuyente") NO usan esta excepción.
 */
public class OrigenNoDisponibleException extends RuntimeException {

    private final String fuente;

    public OrigenNoDisponibleException(String fuente, String mensaje, Throwable causa) {
        super(fuente + ": " + mensaje, causa);
        this.fuente = fuente;
    }

    public OrigenNoDisponibleException(String fuente, String mensaje) {
        this(fuente, mensaje, null);
    }

    public String fuente() { return fuente; }
}

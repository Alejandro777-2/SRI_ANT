package ec.udla.sri.model;

import java.util.function.Function;

/** Sobre estándar de respuesta: datos + procedencia. */
public record Resultado<T>(T datos, Meta meta) {

    public <R> Resultado<R> mapear(Function<T, R> f) {
        return new Resultado<>(f.apply(datos), meta);
    }
}

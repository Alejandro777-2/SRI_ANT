package ec.udla.sri.model;

import java.util.Map;

/** Campos normalizados más el detalle completo devuelto por el SRI. */
public record VehiculoInfo(
        String placa,
        String marca,
        String modelo,
        String anio,
        String clase,
        String color,
        String servicio,
        Map<String, Object> detalle) {}

package ec.udla.sri.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ec.udla.sri.cache.ResilientCache;
import ec.udla.sri.client.SriClient;
import ec.udla.sri.config.AppProperties;
import ec.udla.sri.error.ApiException;
import ec.udla.sri.model.Resultado;
import ec.udla.sri.model.VehiculoInfo;
import ec.udla.sri.validation.Validadores;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class VehiculoService {

    private final SriClient sri;
    private final ResilientCache cache;
    private final AppProperties props;
    private final ObjectMapper mapper;

    public VehiculoService(SriClient sri, ResilientCache cache, AppProperties props, ObjectMapper mapper) {
        this.sri = sri;
        this.cache = cache;
        this.props = props;
        this.mapper = mapper;
    }

    public Resultado<VehiculoInfo> consultar(String placaCrudo) {
        String placa = Validadores.normalizarPlaca(placaCrudo);
        var error = Validadores.placa(placa);
        if (error != null) throw ApiException.validacion(List.of(error));

        return cache.obtener("sri:vehiculo:" + placa, VehiculoInfo.class,
                props.politicas().vehiculo(), "SRI", () -> obtener(placa));
    }

    private VehiculoInfo obtener(String placa) {
        JsonNode n = sri.obtenerVehiculo(placa).orElseThrow(() -> ApiException.noEncontrado(
                "VEHICULO_NO_ENCONTRADO", "No se encontró un vehículo con la matrícula " + placa + "."));
        Map<String, Object> detalle = mapper.convertValue(n, new TypeReference<>() {});
        return new VehiculoInfo(
                placa,
                primero(n, "marca", "descripcionMarca", "marcaVehiculo"),
                primero(n, "modelo", "descripcionModelo", "modeloVehiculo"),
                primero(n, "anioAuto", "anio", "anioModelo"),
                primero(n, "clase", "claseVehiculo", "descripcionClase"),
                primero(n, "color1", "color", "descripcionColor"),
                primero(n, "tipoServicio", "servicio", "descripcionServicio"),
                detalle);
    }

    private static String primero(JsonNode n, String... campos) {
        for (String c : campos) {
            JsonNode v = n.path(c);
            if (!v.isMissingNode() && !v.isNull() && !v.asText().isBlank()) return v.asText();
        }
        return null;
    }
}

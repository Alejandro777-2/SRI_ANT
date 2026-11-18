package ec.udla.sri.service;

import ec.udla.sri.cache.ResilientCache;
import ec.udla.sri.client.AntClient;
import ec.udla.sri.config.AppProperties;
import ec.udla.sri.error.ApiException;
import ec.udla.sri.model.LicenciaInfo;
import ec.udla.sri.model.Resultado;
import ec.udla.sri.validation.Validadores;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class LicenciaService {

    private final AntClient ant;
    private final ResilientCache cache;
    private final AppProperties props;

    public LicenciaService(AntClient ant, ResilientCache cache, AppProperties props) {
        this.ant = ant;
        this.cache = cache;
        this.props = props;
    }

    public Resultado<LicenciaInfo> puntos(String cedula) {
        var error = Validadores.cedula(cedula);
        if (error != null) throw ApiException.validacion(List.of(error));

        return cache.obtener("ant:licencia:" + cedula, LicenciaInfo.class,
                props.politicas().licencia(), "ANT", () -> consultar(cedula));
    }

    private LicenciaInfo consultar(String cedula) {
        int puntos = ant.obtenerPuntos(cedula).orElseThrow(() -> ApiException.noEncontrado(
                "LICENCIA_NO_ENCONTRADA", "La ANT no tiene una licencia registrada para la cédula " + cedula + "."));
        return new LicenciaInfo(cedula, puntos, LicenciaInfo.PUNTOS_MAXIMO);
    }
}

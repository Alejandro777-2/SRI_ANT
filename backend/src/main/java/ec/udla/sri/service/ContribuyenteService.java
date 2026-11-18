package ec.udla.sri.service;

import com.fasterxml.jackson.databind.JsonNode;
import ec.udla.sri.cache.ResilientCache;
import ec.udla.sri.client.SriClient;
import ec.udla.sri.config.AppProperties;
import ec.udla.sri.error.ApiException;
import ec.udla.sri.error.ApiException.Detalle;
import ec.udla.sri.error.OrigenNoDisponibleException;
import ec.udla.sri.model.ContribuyenteInfo;
import ec.udla.sri.model.Resultado;
import ec.udla.sri.model.VerificacionDatos;
import ec.udla.sri.validation.Validadores;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ContribuyenteService {

    private final SriClient sri;
    private final ResilientCache cache;
    private final AppProperties props;

    public ContribuyenteService(SriClient sri, ResilientCache cache, AppProperties props) {
        this.sri = sri;
        this.cache = cache;
        this.props = props;
    }

    public Resultado<VerificacionDatos> verificar(String correo, String ruc) {
        List<Detalle> errores = new ArrayList<>();
        Detalle dc = Validadores.correo(correo);
        Detalle dr = Validadores.ruc(ruc);
        if (dc != null) errores.add(dc);
        if (dr != null) errores.add(dr);
        if (!errores.isEmpty()) throw ApiException.validacion(errores);

        String correoLimpio = correo.trim();
        return cache
                .obtener("sri:contribuyente:" + ruc, ContribuyenteInfo.class,
                        props.politicas().contribuyente(), "SRI", () -> consultar(ruc))
                .mapear(c -> new VerificacionDatos(correoLimpio, c));
    }

    private ContribuyenteInfo consultar(String ruc) {
        if (!sri.existeRuc(ruc)) {
            throw ApiException.noEncontrado("NO_ES_CONTRIBUYENTE",
                    "El RUC " + ruc + " no consta como contribuyente en el SRI.");
        }
        JsonNode n = sri.obtenerContribuyente(ruc).orElseThrow(
                () -> new OrigenNoDisponibleException("SRI", "el RUC existe pero no devolvió datos"));

        if (!esPersonaNatural(n)) {
            throw ApiException.reglaNegocio("NO_ES_PERSONA_NATURAL",
                    "El RUC " + ruc + " corresponde a una sociedad; solo se admiten personas naturales.");
        }
        return new ContribuyenteInfo(
                ruc,
                texto(n, "razonSocial"),
                texto(n, "estadoContribuyenteRuc"),
                texto(n, "tipoContribuyente"),
                texto(n, "actividadEconomicaPrincipal"),
                texto(n, "regimen"),
                texto(n, "obligadoLlevarContabilidad"),
                texto(n.path("informacionFechaContribuyente"), "fechaInicioActividades"));
    }

    static boolean esPersonaNatural(JsonNode n) {
        String persona = texto(n, "personaSociedad");
        if (persona != null) return "PNL".equalsIgnoreCase(persona);
        String tipo = texto(n, "tipoContribuyente");
        return tipo != null && tipo.toUpperCase().contains("NATURAL");
    }

    private static String texto(JsonNode n, String campo) {
        JsonNode v = n.path(campo);
        return v.isMissingNode() || v.isNull() ? null : v.asText();
    }
}

package ec.udla.sri.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ec.udla.sri.config.AppProperties;
import ec.udla.sri.error.OrigenNoDisponibleException;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Cliente REST del SRI (catastro de contribuyentes y matriculación vehicular). */
@Component
public class SriClient {

    private static final String FUENTE = "SRI";
    private static final String CATASTRO = "/sri-catastro-sujeto-servicio-internet/rest/ConsolidadoContribuyente";
    private static final String VEHICULO = "/sri-matriculacion-vehicular-recaudacion-servicio-internet"
            + "/rest/BaseVehiculo/obtenerPorNumeroPlacaOPorNumeroCampvOPorNumeroCpn";

    private final RestClient http;
    private final ObjectMapper mapper;

    public SriClient(AppProperties props, ObjectMapper mapper) {
        this.http = Http.crear(props.upstream().sriBaseUrl(), props);
        this.mapper = mapper;
    }

    public boolean existeRuc(String ruc) {
        String cuerpo = get(CATASTRO + "/existePorNumeroRuc?numeroRuc={ruc}", ruc).trim();
        if ("true".equalsIgnoreCase(cuerpo)) return true;
        if ("false".equalsIgnoreCase(cuerpo)) return false;
        throw new OrigenNoDisponibleException(FUENTE, "respuesta inesperada en existePorNumeroRuc");
    }

    /** Devuelve el primer registro del contribuyente, o vacío si el SRI no devuelve datos. */
    public Optional<JsonNode> obtenerContribuyente(String ruc) {
        return primerRegistro(get(CATASTRO + "/obtenerPorNumerosRuc?&ruc={ruc}", ruc));
    }

    public Optional<JsonNode> obtenerVehiculo(String placaCampvCpn) {
        return primerRegistro(get(VEHICULO + "?numeroPlacaCampvCpn={p}", placaCampvCpn));
    }

    private Optional<JsonNode> primerRegistro(String cuerpo) {
        try {
            if (cuerpo.isBlank()) return Optional.empty();
            JsonNode raiz = mapper.readTree(cuerpo);
            JsonNode nodo = raiz.isArray() ? (raiz.isEmpty() ? null : raiz.get(0)) : raiz;
            if (nodo == null || nodo.isNull() || (nodo.isObject() && nodo.isEmpty())) return Optional.empty();
            return Optional.of(nodo);
        } catch (JsonProcessingException e) {
            throw new OrigenNoDisponibleException(FUENTE, "respuesta no es JSON válido", e);
        }
    }

    private String get(String uri, Object... vars) {
        try {
            String cuerpo = http.get().uri(uri, vars)
                    .header(HttpHeaders.USER_AGENT, Http.USER_AGENT)
                    .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN)
                    .retrieve()
                    .body(String.class);
            if (cuerpo == null) throw new OrigenNoDisponibleException(FUENTE, "respuesta vacía");
            return cuerpo;
        } catch (RestClientException e) {
            throw new OrigenNoDisponibleException(FUENTE, e.getMessage(), e);
        }
    }
}

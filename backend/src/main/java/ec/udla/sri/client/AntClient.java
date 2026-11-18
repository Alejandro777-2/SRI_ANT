package ec.udla.sri.client;

import ec.udla.sri.config.AppProperties;
import ec.udla.sri.error.OrigenNoDisponibleException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Cliente de la web de consulta de citaciones de la ANT (baja disponibilidad). */
@Component
public class AntClient {

    private static final String RUTA = "/PortalWEB/paginas/clientes/clp_grid_citaciones.jsp"
            + "?ps_tipo_identificacion=CED&ps_identificacion={cedula}&ps_placa=";

    private final RestClient http;

    public AntClient(AppProperties props) {
        this.http = Http.crear(props.upstream().antBaseUrl(), props);
    }

    /** Puntos vigentes de la licencia, o vacío si la ANT no tiene registro para la cédula. */
    public Optional<Integer> obtenerPuntos(String cedula) {
        try {
            byte[] cuerpo = http.get().uri(RUTA, cedula)
                    .header(HttpHeaders.USER_AGENT, Http.USER_AGENT)
                    .accept(MediaType.TEXT_HTML)
                    .retrieve()
                    .body(byte[].class);
            String html = cuerpo == null ? null : new String(cuerpo, StandardCharsets.ISO_8859_1);
            return AntHtmlParser.extraerPuntos(html);
        } catch (RestClientException e) {
            throw new OrigenNoDisponibleException("ANT", e.getMessage(), e);
        }
    }
}

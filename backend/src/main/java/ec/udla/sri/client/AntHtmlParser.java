package ec.udla.sri.client;

import ec.udla.sri.error.OrigenNoDisponibleException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;

/**
 * Extrae los puntos de la licencia del HTML de la ANT.
 * Es el único punto que depende del formato de la página: si la ANT cambia el HTML, se ajusta solo aquí.
 */
public final class AntHtmlParser {

    private static final Pattern PUNTOS =
            Pattern.compile("(?i)puntos(?:\\s+(?:en|de)\\s+(?:su\\s+|la\\s+)?licencia)?\\s*[:=\\-]?\\s*(\\d{1,2})\\b");
    private static final Pattern SIN_REGISTRO =
            Pattern.compile("(?i)no\\s+(?:se\\s+)?(?:encontr|existe|registra)");

    private AntHtmlParser() {}

    /**
     * @return puntos si la página los informa; vacío si la ANT indica que no hay registro;
     *         lanza OrigenNoDisponibleException si el HTML no es reconocible (se trata como caída del servicio).
     */
    public static Optional<Integer> extraerPuntos(String html) {
        if (html == null || html.isBlank()) {
            throw new OrigenNoDisponibleException("ANT", "respuesta vacía");
        }
        String texto = Jsoup.parse(html).text();
        Matcher m = PUNTOS.matcher(texto);
        if (m.find()) {
            int puntos = Integer.parseInt(m.group(1));
            if (puntos > 30) {
                throw new OrigenNoDisponibleException("ANT", "valor de puntos fuera de rango: " + puntos);
            }
            return Optional.of(puntos);
        }
        if (SIN_REGISTRO.matcher(texto).find()) {
            return Optional.empty();
        }
        throw new OrigenNoDisponibleException("ANT", "formato de página no reconocido");
    }
}

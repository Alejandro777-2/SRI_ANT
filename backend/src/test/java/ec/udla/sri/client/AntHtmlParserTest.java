package ec.udla.sri.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.udla.sri.error.OrigenNoDisponibleException;
import org.junit.jupiter.api.Test;

/** Las muestras HTML son SIMULADAS: ajustar contra el HTML real de la ANT. */
class AntHtmlParserTest {

    @Test
    void extraePuntosDeTabla() {
        String html = "<html><body><table><tr><td>Puntos en su licencia:</td><td> 27 </td></tr></table></body></html>";
        assertEquals(27, AntHtmlParser.extraerPuntos(html).orElseThrow());
    }

    @Test
    void extraePuntosEnTextoCorrido() {
        assertEquals(30, AntHtmlParser.extraerPuntos("<div>PUNTOS: 30</div>").orElseThrow());
    }

    @Test
    void sinRegistroDevuelveVacio() {
        assertTrue(AntHtmlParser.extraerPuntos("<p>No se encontraron resultados</p>").isEmpty());
    }

    @Test
    void htmlNoReconocibleSeTrataComoCaida() {
        assertThrows(OrigenNoDisponibleException.class,
                () -> AntHtmlParser.extraerPuntos("<h1>503 Service Unavailable</h1>"));
    }

    @Test
    void respuestaVaciaSeTrataComoCaida() {
        assertThrows(OrigenNoDisponibleException.class, () -> AntHtmlParser.extraerPuntos(""));
    }

    @Test
    void puntosFueraDeRangoSeRechazan() {
        assertThrows(OrigenNoDisponibleException.class, () -> AntHtmlParser.extraerPuntos("Puntos: 99"));
    }
}

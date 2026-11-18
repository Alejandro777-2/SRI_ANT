package ec.udla.sri.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.udla.sri.config.AppProperties.Politica;
import ec.udla.sri.error.ApiException;
import ec.udla.sri.error.OrigenNoDisponibleException;
import ec.udla.sri.model.LicenciaInfo;
import ec.udla.sri.model.Meta;
import ec.udla.sri.model.Resultado;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

class ResilientCacheTest {

    private static final Politica SIEMPRE_CONSULTAR = new Politica(Duration.ZERO, Duration.ofDays(7));
    private static final Politica FRESCA_1H = new Politica(Duration.ofHours(1), Duration.ofDays(7));

    private ResilientCache cache;

    @BeforeEach
    void init() {
        cache = new ResilientCache(new InMemoryCacheStore(), Jackson2ObjectMapperBuilder.json().build());
    }

    private static LicenciaInfo lic(int p) {
        return new LicenciaInfo("1710034065", p, 30);
    }

    @Test
    void primeraConsultaVaAlOrigenYGuarda() {
        Resultado<LicenciaInfo> r = cache.obtener("k", LicenciaInfo.class, SIEMPRE_CONSULTAR, "ANT", () -> lic(25));
        assertEquals(Meta.ORIGEN, r.meta().origen());
        assertFalse(r.meta().obsoleto());
        assertEquals(25, r.datos().puntos());
    }

    @Test
    void siElOrigenCaeDevuelveElUltimoDatoBuenoConAviso() {
        cache.obtener("k", LicenciaInfo.class, SIEMPRE_CONSULTAR, "ANT", () -> lic(25));

        Resultado<LicenciaInfo> r = cache.obtener("k", LicenciaInfo.class, SIEMPRE_CONSULTAR, "ANT", () -> {
            throw new OrigenNoDisponibleException("ANT", "timeout");
        });

        assertEquals(Meta.CACHE_OBSOLETA, r.meta().origen());
        assertTrue(r.meta().obsoleto());
        assertNotNull(r.meta().aviso());
        assertEquals(25, r.datos().puntos());
    }

    @Test
    void sinDatoGuardadoYOrigenCaidoResponde503() {
        ApiException e = assertThrows(ApiException.class, () ->
                cache.obtener("k", LicenciaInfo.class, SIEMPRE_CONSULTAR, "ANT", () -> {
                    throw new OrigenNoDisponibleException("ANT", "timeout");
                }));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, e.estado());
        assertEquals("SERVICIO_EXTERNO_NO_DISPONIBLE", e.codigo());
    }

    @Test
    void datoFrescoSeSirveSinLlamarAlOrigen() {
        cache.obtener("k", LicenciaInfo.class, FRESCA_1H, "ANT", () -> lic(25));
        AtomicInteger llamadas = new AtomicInteger();

        Resultado<LicenciaInfo> r = cache.obtener("k", LicenciaInfo.class, FRESCA_1H, "ANT", () -> {
            llamadas.incrementAndGet();
            return lic(10);
        });

        assertEquals(0, llamadas.get());
        assertEquals(Meta.CACHE_FRESCA, r.meta().origen());
        assertEquals(25, r.datos().puntos());
    }

    @Test
    void erroresDeNegocioSePropaganYNoSeCachean() {
        assertThrows(ApiException.class, () ->
                cache.obtener("k", LicenciaInfo.class, SIEMPRE_CONSULTAR, "ANT", () -> {
                    throw ApiException.noEncontrado("LICENCIA_NO_ENCONTRADA", "no existe");
                }));

        // Si hubiera quedado algo en caché, esta llamada con origen caído lo devolvería en vez de 503.
        assertThrows(ApiException.class, () ->
                cache.obtener("k", LicenciaInfo.class, SIEMPRE_CONSULTAR, "ANT", () -> {
                    throw new OrigenNoDisponibleException("ANT", "timeout");
                }));
    }

    @Test
    void siElOrigenRecuperaSeActualizaElDato() {
        cache.obtener("k", LicenciaInfo.class, SIEMPRE_CONSULTAR, "ANT", () -> lic(25));
        Resultado<LicenciaInfo> r = cache.obtener("k", LicenciaInfo.class, SIEMPRE_CONSULTAR, "ANT", () -> lic(22));
        assertEquals(22, r.datos().puntos());
        assertEquals(Meta.ORIGEN, r.meta().origen());
    }
}

package ec.udla.sri.web;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.udla.sri.client.AntClient;
import ec.udla.sri.client.SriClient;
import ec.udla.sri.error.OrigenNoDisponibleException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {"app.cache.provider=memory"})
@AutoConfigureMockMvc
class ApiIntegrationTest {

    // Cédulas con dígito verificador válido. Un RUC distinto por prueba: la caché en memoria se comparte en la clase.
    private static final String RUC = "1710034065001";
    private static final String RUC_NO_CONTRIBUYENTE = "1700000019001";
    private static final String RUC_SOCIEDAD = "0100000009001";
    private static final String CEDULA = "1710034065";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @MockitoBean SriClient sri;
    @MockitoBean AntClient ant;

    private String cuerpo(String correo, String ruc) throws Exception {
        return json.writeValueAsString(java.util.Map.of("correo", correo, "ruc", ruc));
    }

    @Test
    void rucInvalidoYCorreoInvalidoDevuelven400ConDetalles() throws Exception {
        mvc.perform(post("/api/v1/contribuyentes/verificaciones")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo("no-es-correo", "123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACION_FALLIDA"))
                .andExpect(jsonPath("$.detalles.length()").value(2))
                .andExpect(jsonPath("$.detalles[0].campo").value("correo"))
                .andExpect(jsonPath("$.detalles[1].campo").value("ruc"));
    }

    @Test
    void cuerpoAusenteDevuelve400() throws Exception {
        mvc.perform(post("/api/v1/contribuyentes/verificaciones").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("CUERPO_INVALIDO"));
    }

    @Test
    void noContribuyenteDevuelve404() throws Exception {
        when(sri.existeRuc(RUC_NO_CONTRIBUYENTE)).thenReturn(false);
        mvc.perform(post("/api/v1/contribuyentes/verificaciones")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo("a@b.com", RUC_NO_CONTRIBUYENTE)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("NO_ES_CONTRIBUYENTE"));
    }

    @Test
    void sociedadDevuelve422() throws Exception {
        when(sri.existeRuc(RUC_SOCIEDAD)).thenReturn(true);
        when(sri.obtenerContribuyente(RUC_SOCIEDAD)).thenReturn(Optional.of(
                json.readTree("{\"razonSocial\":\"ACME S.A.\",\"personaSociedad\":\"SCD\"}")));
        mvc.perform(post("/api/v1/contribuyentes/verificaciones")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo("a@b.com", RUC_SOCIEDAD)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("NO_ES_PERSONA_NATURAL"));
    }

    @Test
    void personaNaturalDevuelve200ConDatosYMeta() throws Exception {
        when(sri.existeRuc(RUC)).thenReturn(true);
        when(sri.obtenerContribuyente(RUC)).thenReturn(Optional.of(json.readTree(
                "{\"razonSocial\":\"PEREZ JUAN\",\"personaSociedad\":\"PNL\",\"estadoContribuyenteRuc\":\"ACTIVO\","
                        + "\"informacionFechaContribuyente\":{\"fechaInicioActividades\":\"2015-03-01\"}}")));
        mvc.perform(post("/api/v1/contribuyentes/verificaciones")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo("a@b.com", RUC)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.correo").value("a@b.com"))
                .andExpect(jsonPath("$.datos.contribuyente.razonSocial").value("PEREZ JUAN"))
                .andExpect(jsonPath("$.datos.contribuyente.fechaInicioActividades").value("2015-03-01"))
                .andExpect(jsonPath("$.meta.origen").value("ORIGEN"))
                .andExpect(jsonPath("$.meta.obsoleto").value(false));
    }

    @Test
    void vehiculoInvalidoDevuelve400YNoExistenteDevuelve404() throws Exception {
        mvc.perform(get("/api/v1/vehiculos/A1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles[0].codigo").value("PLACA_INVALIDA"));

        when(sri.obtenerVehiculo("ZZZ9999")).thenReturn(Optional.empty());
        mvc.perform(get("/api/v1/vehiculos/zzz-9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("VEHICULO_NO_ENCONTRADO"));
    }

    @Test
    void vehiculoEncontradoDevuelveCamposNormalizados() throws Exception {
        when(sri.obtenerVehiculo("PBX1234")).thenReturn(Optional.of(json.readTree(
                "{\"marca\":\"CHEVROLET\",\"modelo\":\"SAIL\",\"anioAuto\":2019,\"color1\":\"BLANCO\"}")));
        mvc.perform(get("/api/v1/vehiculos/PBX-1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.placa").value("PBX1234"))
                .andExpect(jsonPath("$.datos.marca").value("CHEVROLET"))
                .andExpect(jsonPath("$.datos.anio").value("2019"))
                .andExpect(jsonPath("$.datos.detalle.modelo").value("SAIL"));
    }

    @Test
    void cedulaInvalidaEnLicenciaDevuelve400() throws Exception {
        mvc.perform(get("/api/v1/licencias/1710034066/puntos"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles[0].codigo").value("CEDULA_INVALIDA"));
    }

    @Test
    void antCaidaSinCacheDevuelve503() throws Exception {
        when(ant.obtenerPuntos(anyString())).thenThrow(new OrigenNoDisponibleException("ANT", "timeout"));
        mvc.perform(get("/api/v1/licencias/" + CEDULA + "/puntos"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.codigo").value("SERVICIO_EXTERNO_NO_DISPONIBLE"));
    }

    @Test
    void antCaidaConCacheDevuelveUltimoDatoBuenoConAviso() throws Exception {
        String otraCedula = "0102030400"; // distinta para no mezclar caché con otras pruebas
        when(ant.obtenerPuntos(otraCedula)).thenReturn(Optional.of(24));
        mvc.perform(get("/api/v1/licencias/" + otraCedula + "/puntos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.puntos").value(24))
                .andExpect(jsonPath("$.meta.origen").value("ORIGEN"));

        // Segunda llamada: la política "fresca" (15 min) la sirve desde caché sin tocar la ANT.
        mvc.perform(get("/api/v1/licencias/" + otraCedula + "/puntos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.origen").value("CACHE_FRESCA"))
                .andExpect(jsonPath("$.datos.puntos").value(24));
    }

    @Test
    void rutaInexistenteDevuelveErrorEstructurado() throws Exception {
        mvc.perform(get("/api/v1/nada"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RUTA_NO_ENCONTRADA"));
    }
}

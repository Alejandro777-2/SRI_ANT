package ec.udla.sri.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ValidadoresTest {

    @Test
    void cedulaValidaPasa() {
        assertNull(Validadores.cedula("1710034065"));
    }

    @Test
    void cedulaConDigitoVerificadorIncorrectoFalla() {
        assertEquals("CEDULA_INVALIDA", Validadores.cedula("1710034066").codigo());
    }

    @Test
    void cedulaConProvinciaInvalidaFalla() {
        assertNotNull(Validadores.cedula("9910034065"));
    }

    @Test
    void rucPersonaNaturalValidoPasa() {
        assertNull(Validadores.ruc("1710034065001"));
    }

    @Test
    void rucConLongitudIncorrectaFalla() {
        assertEquals("RUC_INVALIDO", Validadores.ruc("171003406500").codigo());
    }

    @Test
    void rucConEstablecimientoCeroFalla() {
        assertEquals("RUC_INVALIDO", Validadores.ruc("1710034065000").codigo());
    }

    @Test
    void rucNuloEsRequerido() {
        assertEquals("RUC_REQUERIDO", Validadores.ruc(null).codigo());
    }

    @Test
    void correoValidoEInvalido() {
        assertNull(Validadores.correo("persona@ejemplo.com"));
        assertEquals("CORREO_INVALIDO", Validadores.correo("persona@ejemplo").codigo());
        assertEquals("CORREO_REQUERIDO", Validadores.correo(" ").codigo());
    }

    @Test
    void placaSeNormalizaYValida() {
        assertEquals("PBX1234", Validadores.normalizarPlaca(" pbx-1234 "));
        assertNull(Validadores.placa("PBX1234"));
        assertEquals("PLACA_INVALIDA", Validadores.placa("A1").codigo());
    }
}

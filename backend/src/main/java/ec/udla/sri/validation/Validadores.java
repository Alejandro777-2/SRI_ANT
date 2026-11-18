package ec.udla.sri.validation;

import ec.udla.sri.error.ApiException.Detalle;
import java.util.regex.Pattern;

/** Validaciones de entrada. Cada método devuelve null si es válido o el Detalle del error. */
public final class Validadores {

    private static final Pattern CORREO =
            Pattern.compile("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PLACA_CAMV_CPN = Pattern.compile("^[A-Z0-9]{5,17}$");

    private Validadores() {}

    public static Detalle correo(String valor) {
        if (valor == null || valor.isBlank()) {
            return new Detalle("correo", "CORREO_REQUERIDO", "El correo electrónico es obligatorio.");
        }
        if (valor.length() > 254 || !CORREO.matcher(valor.trim()).matches()) {
            return new Detalle("correo", "CORREO_INVALIDO", "El correo electrónico no tiene un formato válido.");
        }
        return null;
    }

    public static Detalle ruc(String valor) {
        if (valor == null || valor.isBlank()) {
            return new Detalle("ruc", "RUC_REQUERIDO", "El RUC es obligatorio.");
        }
        if (!valor.matches("\\d{13}")) {
            return new Detalle("ruc", "RUC_INVALIDO", "El RUC debe tener 13 dígitos numéricos.");
        }
        if (valor.endsWith("000")) {
            return new Detalle("ruc", "RUC_INVALIDO", "El código de establecimiento del RUC no es válido.");
        }
        Detalle cedula = cedula(valor.substring(0, 10), "ruc");
        if (cedula != null) {
            return new Detalle("ruc", "RUC_INVALIDO",
                    "El RUC de persona natural debe iniciar con una cédula válida (10 dígitos).");
        }
        return null;
    }

    public static Detalle cedula(String valor) {
        return cedula(valor, "cedula");
    }

    private static Detalle cedula(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            return new Detalle(campo, "CEDULA_REQUERIDA", "La cédula es obligatoria.");
        }
        if (!valor.matches("\\d{10}")) {
            return new Detalle(campo, "CEDULA_INVALIDA", "La cédula debe tener 10 dígitos numéricos.");
        }
        int provincia = Integer.parseInt(valor.substring(0, 2));
        boolean provinciaValida = (provincia >= 1 && provincia <= 24) || provincia == 30;
        int tercero = valor.charAt(2) - '0';
        if (!provinciaValida || tercero > 5 || !digitoVerificadorOk(valor)) {
            return new Detalle(campo, "CEDULA_INVALIDA", "La cédula no es válida (provincia o dígito verificador).");
        }
        return null;
    }

    public static String normalizarPlaca(String valor) {
        return valor == null ? null : valor.trim().toUpperCase().replace("-", "").replace(" ", "");
    }

    public static Detalle placa(String normalizada) {
        if (normalizada == null || normalizada.isBlank()) {
            return new Detalle("placa", "PLACA_REQUERIDA", "La matrícula (placa, CAMV o CPN) es obligatoria.");
        }
        if (!PLACA_CAMV_CPN.matcher(normalizada).matches()) {
            return new Detalle("placa", "PLACA_INVALIDA",
                    "La matrícula debe tener entre 5 y 17 caracteres alfanuméricos (ej. PBX1234).");
        }
        return null;
    }

    /** Módulo 10 con coeficientes 2,1,2,1,2,1,2,1,2. */
    private static boolean digitoVerificadorOk(String cedula) {
        int suma = 0;
        for (int i = 0; i < 9; i++) {
            int d = cedula.charAt(i) - '0';
            int v = (i % 2 == 0) ? d * 2 : d;
            suma += v > 9 ? v - 9 : v;
        }
        int verificador = (10 - (suma % 10)) % 10;
        return verificador == cedula.charAt(9) - '0';
    }
}

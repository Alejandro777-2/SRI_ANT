package ec.udla.sri.model;

public record LicenciaInfo(String cedula, int puntos, int puntosMaximo) {

    public static final int PUNTOS_MAXIMO = 30;
}

package ec.udla.sri.model;

public record ContribuyenteInfo(
        String ruc,
        String razonSocial,
        String estado,
        String tipoContribuyente,
        String actividadEconomica,
        String regimen,
        String obligadoContabilidad,
        String fechaInicioActividades) {}

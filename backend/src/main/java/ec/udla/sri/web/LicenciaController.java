package ec.udla.sri.web;

import ec.udla.sri.model.LicenciaInfo;
import ec.udla.sri.model.Resultado;
import ec.udla.sri.service.LicenciaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/licencias")
public class LicenciaController {

    private final LicenciaService service;

    public LicenciaController(LicenciaService service) {
        this.service = service;
    }

    @GetMapping("/{cedula}/puntos")
    public Resultado<LicenciaInfo> puntos(@PathVariable String cedula) {
        return service.puntos(cedula);
    }
}

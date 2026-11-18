package ec.udla.sri.web;

import ec.udla.sri.model.Resultado;
import ec.udla.sri.model.VerificacionDatos;
import ec.udla.sri.service.ContribuyenteService;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/contribuyentes")
public class ContribuyenteController {

    private final ContribuyenteService service;

    public ContribuyenteController(ContribuyenteService service) {
        this.service = service;
    }

    /** POST porque el correo viaja en el cuerpo (no en la URL/logs). No crea un recurso, por eso responde 200. */
    @PostMapping("/verificaciones")
    public Resultado<VerificacionDatos> verificar(@RequestBody Map<String, String> cuerpo) {
        return service.verificar(cuerpo.get("correo"), cuerpo.get("ruc"));
    }
}

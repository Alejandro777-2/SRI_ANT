package ec.udla.sri.web;

import ec.udla.sri.model.Resultado;
import ec.udla.sri.model.VehiculoInfo;
import ec.udla.sri.service.VehiculoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vehiculos")
public class VehiculoController {

    private final VehiculoService service;

    public VehiculoController(VehiculoService service) {
        this.service = service;
    }

    @GetMapping("/{placa}")
    public Resultado<VehiculoInfo> porMatricula(@PathVariable String placa) {
        return service.consultar(placa);
    }
}

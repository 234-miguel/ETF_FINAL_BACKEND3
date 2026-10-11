
package com.bancoxyz.bff.bff.mobile;

import com.bancoxyz.bff.model.Cuenta;
import com.bancoxyz.bff.service.CuentaService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mobile")
public class MobileController {

    private final CuentaService cuentaService;

    public MobileController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @GetMapping("/cuentas")
    public List<Map<String, Object>> obtenerCuentas() {

        return cuentaService.obtenerCuentas()
                .stream()
                .map(cuenta -> Map.<String, Object>of(
                        "cuentaId", cuenta.getCuentaId(),
                        "saldo", cuenta.getSaldo()
                ))
                .toList();
    }

    @GetMapping("/saldo/{id}")
    public ResponseEntity<Map<String, Object>> consultarSaldo(
            @PathVariable Long id) {

        try {
            Cuenta cuenta = cuentaService.obtenerCuentaPorId(id);

            return ResponseEntity.ok(Map.of(
                    "cuentaId", cuenta.getCuentaId(),
                    "saldoDisponible", cuenta.getSaldo()
            ));

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}

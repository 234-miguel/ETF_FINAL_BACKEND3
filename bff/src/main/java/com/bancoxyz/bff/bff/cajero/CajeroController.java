package com.bancoxyz.bff.bff.cajero;

import com.bancoxyz.bff.model.Cuenta;
import com.bancoxyz.bff.service.CuentaService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cajero")
public class CajeroController {

    private final CuentaService cuentaService;

    public CajeroController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @GetMapping("/saldo/{id}")
    public ResponseEntity<Map<String, Object>> consultarSaldo(
            @PathVariable Long id) {

        try {
            Cuenta cuenta = cuentaService.obtenerCuentaPorId(id);

            Map<String, Object> respuesta = new LinkedHashMap<>();

            respuesta.put("cuentaId", cuenta.getCuentaId());
            respuesta.put("saldoDisponible", cuenta.getSaldo());

            return ResponseEntity.ok(respuesta);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/retiro/{id}/{monto}")
    public ResponseEntity<Map<String, Object>> retirar(
            @PathVariable Long id,
            @PathVariable Double monto) {

        try {
            Cuenta cuenta = cuentaService.obtenerCuentaPorId(id);

            double saldoAnterior = cuenta.getSaldo();

            double saldoNuevo = cuentaService.retirar(id, monto);

            Map<String, Object> respuesta = new LinkedHashMap<>();

            respuesta.put("mensaje", "Retiro realizado correctamente");
            respuesta.put("cuentaId", id);
            respuesta.put("montoRetirado", monto);
            respuesta.put("saldoAnterior", saldoAnterior);
            respuesta.put("saldoNuevo", saldoNuevo);

            return ResponseEntity.ok(respuesta);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
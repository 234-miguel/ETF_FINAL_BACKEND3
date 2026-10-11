package com.bancoxyz.bff.bff.web;

import com.bancoxyz.bff.model.Cuenta;
import com.bancoxyz.bff.service.CuentaService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/web")
public class WebController {

    private final CuentaService cuentaService;

    public WebController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @GetMapping("/cuentas")
    public ResponseEntity<List<Cuenta>> obtenerCuentas() {
        return ResponseEntity.ok(cuentaService.obtenerCuentas());
    }

    @GetMapping("/cuentas/{id}")
    public ResponseEntity<?> obtenerCuentaPorId(@PathVariable Long id) {

        try {
            Cuenta cuenta = cuentaService.obtenerCuentaPorId(id);
            return ResponseEntity.ok(cuenta);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
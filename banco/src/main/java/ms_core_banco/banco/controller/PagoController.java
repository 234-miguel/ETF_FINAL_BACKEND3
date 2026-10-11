
package ms_core_banco.banco.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import ms_core_banco.banco.event.TransaccionEventPublisher;
import ms_core_banco.banco.event.TransaccionRealizadaEvent;
import ms_core_banco.banco.model.CuentaInteres;
import ms_core_banco.banco.model.MovimientoAnual;
import ms_core_banco.banco.repository.CuentaInteresRepository;
import ms_core_banco.banco.repository.MovimientoAnualRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/core/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final CuentaInteresRepository cuentaRepository;
    private final MovimientoAnualRepository movimientoRepository;
    private final TransaccionEventPublisher eventPublisher;

    // DEPOSITAR DINERO
    @PostMapping("/deposito/{id}")
    @Transactional
    public ResponseEntity<?> depositar(
            @PathVariable Long id,
            @RequestBody Map<String, BigDecimal> datos) {

        BigDecimal monto = datos.get("monto");

        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Monto invalido"));
        }

        var cuentaOptional = cuentaRepository.findById(id);

        if (cuentaOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        CuentaInteres cuenta = cuentaOptional.get();

        cuenta.setSaldo(cuenta.getSaldo().add(monto));
        cuentaRepository.save(cuenta);

        guardarMovimiento(id, "deposito", monto,
                "Deposito realizado");

        publicarEvento(id, "deposito", monto, cuenta.getSaldo());

        return ResponseEntity.ok(cuenta);
    }

    // REALIZAR PAGO
    @PostMapping("/pago/{id}")
    @Transactional
    public ResponseEntity<?> pagar(
            @PathVariable Long id,
            @RequestBody Map<String, BigDecimal> datos) {

        BigDecimal monto = datos.get("monto");

        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Monto invalido"));
        }

        var cuentaOptional = cuentaRepository.findById(id);

        if (cuentaOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        CuentaInteres cuenta = cuentaOptional.get();

        if (cuenta.getSaldo().compareTo(monto) < 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Saldo insuficiente"));
        }

        cuenta.setSaldo(cuenta.getSaldo().subtract(monto));
        cuentaRepository.save(cuenta);

        guardarMovimiento(id, "pago", monto,
                "Pago realizado");

        publicarEvento(id, "pago", monto, cuenta.getSaldo());

        return ResponseEntity.ok(cuenta);
    }

    // TRANSFERIR ENTRE CUENTAS
    @PostMapping("/transferencia/{origenId}/{destinoId}")
    @Transactional
    public ResponseEntity<?> transferir(
            @PathVariable Long origenId,
            @PathVariable Long destinoId,
            @RequestBody Map<String, BigDecimal> datos) {

        BigDecimal monto = datos.get("monto");

        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Monto invalido"));
        }

        if (origenId.equals(destinoId)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error",
                            "Las cuentas deben ser diferentes"));
        }

        var origenOptional = cuentaRepository.findById(origenId);
        var destinoOptional = cuentaRepository.findById(destinoId);

        if (origenOptional.isEmpty() || destinoOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        CuentaInteres origen = origenOptional.get();
        CuentaInteres destino = destinoOptional.get();

        if (origen.getSaldo().compareTo(monto) < 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Saldo insuficiente"));
        }

        origen.setSaldo(origen.getSaldo().subtract(monto));
        destino.setSaldo(destino.getSaldo().add(monto));

        cuentaRepository.save(origen);
        cuentaRepository.save(destino);

        guardarMovimiento(origenId, "transferencia_salida",
                monto, "Transferencia a cuenta " + destinoId);

        guardarMovimiento(destinoId, "transferencia_entrada",
                monto, "Transferencia desde cuenta " + origenId);

        publicarEvento(origenId, "transferencia_salida",
                monto, origen.getSaldo());

        publicarEvento(destinoId, "transferencia_entrada",
                monto, destino.getSaldo());

        return ResponseEntity.ok(Map.of(
                "mensaje", "Transferencia realizada",
                "cuentaOrigen", origenId,
                "cuentaDestino", destinoId,
                "monto", monto
        ));
    }

    private void guardarMovimiento(
            Long cuentaId,
            String tipo,
            BigDecimal monto,
            String descripcion) {

        MovimientoAnual movimiento = new MovimientoAnual(
                null,
                cuentaId,
                LocalDate.now(),
                tipo,
                monto,
                descripcion
        );

        movimientoRepository.save(movimiento);
    }

    private void publicarEvento(
            Long cuentaId,
            String tipo,
            BigDecimal monto,
            BigDecimal saldo) {

        eventPublisher.publicar(new TransaccionRealizadaEvent(
                cuentaId,
                tipo,
                monto,
                saldo,
                LocalDateTime.now()
        ));
    }
}

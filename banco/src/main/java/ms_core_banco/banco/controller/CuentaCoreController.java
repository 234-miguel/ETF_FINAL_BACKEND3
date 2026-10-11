
package ms_core_banco.banco.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import ms_core_banco.banco.dto.ErrorResponseDTO;
import ms_core_banco.banco.dto.RetiroRequestDTO;
import ms_core_banco.banco.event.TransaccionEventPublisher;
import ms_core_banco.banco.event.TransaccionRealizadaEvent;
import ms_core_banco.banco.model.CuentaInteres;
import ms_core_banco.banco.model.MovimientoAnual;
import ms_core_banco.banco.repository.CuentaInteresRepository;
import ms_core_banco.banco.repository.MovimientoAnualRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/core/cuentas")
@RequiredArgsConstructor
public class CuentaCoreController {

    private final CuentaInteresRepository cuentaInteresRepository;
    private final MovimientoAnualRepository movimientoAnualRepository;
    private final TransaccionEventPublisher eventPublisher;

    // LISTAR CUENTAS
    @GetMapping
    public List<CuentaInteres> listar() {
        return cuentaInteresRepository.findAll();
    }

    // CONSULTAR CUENTA
    @GetMapping("/{id}")
    public ResponseEntity<CuentaInteres> obtener(
            @PathVariable Long id) {

        return cuentaInteresRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // CONSULTAR MOVIMIENTOS
    @GetMapping("/{id}/movimientos")
    public List<MovimientoAnual> movimientos(
            @PathVariable Long id) {

        return movimientoAnualRepository.findByCuentaId(id);
    }

    // ABRIR CUENTA
    @PostMapping
    public ResponseEntity<?> crear(
            @RequestBody CuentaInteres cuenta) {

        if (cuenta.getCuentaId() == null ||
                cuenta.getNombre() == null ||
                cuenta.getNombre().isBlank() ||
                cuenta.getSaldo() == null ||
                cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0 ||
                cuenta.getEdad() == null ||
                cuenta.getEdad() < 18 ||
                cuenta.getTipo() == null ||
                cuenta.getTipo().isBlank()) {

            return ResponseEntity.badRequest()
                    .body(new ErrorResponseDTO(
                            "Datos de cuenta invalidos"));
        }

        if (cuentaInteresRepository.existsById(
                cuenta.getCuentaId())) {

            return ResponseEntity.status(409)
                    .body(new ErrorResponseDTO(
                            "La cuenta ya existe"));
        }

        return ResponseEntity.status(201)
                .body(cuentaInteresRepository.save(cuenta));
    }

    // ACTUALIZAR DATOS DE CUENTA
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @RequestBody CuentaInteres datos) {

        return cuentaInteresRepository.findById(id)
                .map(cuenta -> {

                    if (datos.getNombre() == null ||
                            datos.getNombre().isBlank() ||
                            datos.getEdad() == null ||
                            datos.getEdad() < 18 ||
                            datos.getTipo() == null ||
                            datos.getTipo().isBlank()) {

                        return ResponseEntity.badRequest()
                                .body(new ErrorResponseDTO(
                                        "Datos de cuenta invalidos"));
                    }

                    cuenta.setNombre(datos.getNombre());
                    cuenta.setEdad(datos.getEdad());
                    cuenta.setTipo(datos.getTipo());

                    // El saldo no se modifica aqui.
                    // Solo cambia mediante operaciones bancarias.

                    return ResponseEntity.ok(
                            cuentaInteresRepository.save(cuenta));

                })
                .orElseGet(() ->
                        ResponseEntity.notFound().build());
    }

    // CERRAR CUENTA
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
            @PathVariable Long id) {

        var cuenta = cuentaInteresRepository.findById(id);

        if (cuenta.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (cuenta.get().getSaldo()
                .compareTo(BigDecimal.ZERO) != 0) {

            return ResponseEntity.badRequest()
                    .body(new ErrorResponseDTO(
                            "No se puede cerrar una cuenta con saldo"));
        }

        cuentaInteresRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    // RETIRO BANCARIO CON KAFKA
    @PostMapping("/{id}/retiro")
    public ResponseEntity<?> retirar(
            @PathVariable Long id,
            @RequestBody RetiroRequestDTO request) {

        if (request == null ||
                request.monto() == null ||
                request.monto().compareTo(BigDecimal.ZERO) <= 0) {

            return ResponseEntity.badRequest()
                    .body(new ErrorResponseDTO(
                            "El monto debe ser mayor que cero"));
        }

        return cuentaInteresRepository.findById(id)
                .map(cuenta -> {

                    if (cuenta.getSaldo()
                            .compareTo(request.monto()) < 0) {

                        return ResponseEntity.badRequest()
                                .body(new ErrorResponseDTO(
                                        "Saldo insuficiente"));
                    }

                    cuenta.setSaldo(
                            cuenta.getSaldo().subtract(request.monto()));

                    cuentaInteresRepository.save(cuenta);

                    MovimientoAnual movimiento =
                            new MovimientoAnual(
                                    null,
                                    id,
                                    LocalDate.now(),
                                    "retiro",
                                    request.monto(),
                                    "Retiro en cajero automatico"
                            );

                    movimientoAnualRepository.save(movimiento);

                    eventPublisher.publicar(
                            new TransaccionRealizadaEvent(
                                    id,
                                    "retiro",
                                    request.monto(),
                                    cuenta.getSaldo(),
                                    LocalDateTime.now()
                            )
                    );

                    return ResponseEntity.ok(cuenta);

                })
                .orElseGet(() ->
                        ResponseEntity.notFound().build());
    }
}

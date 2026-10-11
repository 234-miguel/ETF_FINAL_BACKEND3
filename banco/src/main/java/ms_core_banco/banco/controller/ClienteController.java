
package ms_core_banco.banco.controller;

import lombok.RequiredArgsConstructor;
import ms_core_banco.banco.model.Cliente;
import ms_core_banco.banco.repository.ClienteRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/core/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteRepository clienteRepository;

    // REGISTRAR CLIENTE
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Cliente cliente) {

        if (cliente.getNombre() == null ||
                cliente.getNombre().isBlank() ||
                cliente.getRut() == null ||
                cliente.getRut().isBlank()) {

            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Nombre y RUT son obligatorios"));
        }

        cliente.setId(null);

        return ResponseEntity.status(201)
                .body(clienteRepository.save(cliente));
    }

    // LISTAR CLIENTES
    @GetMapping
    public List<Cliente> listar() {
        return clienteRepository.findAll();
    }

    // BUSCAR CLIENTE
    @GetMapping("/{id}")
    public ResponseEntity<Cliente> buscar(@PathVariable Long id) {
        return clienteRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ACTUALIZAR CLIENTE
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @RequestBody Cliente datos) {

        return clienteRepository.findById(id)
                .map(cliente -> {

                    if (datos.getNombre() == null ||
                            datos.getNombre().isBlank()) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("error", "Nombre obligatorio"));
                    }

                    cliente.setNombre(datos.getNombre());
                    cliente.setCorreo(datos.getCorreo());
                    cliente.setTelefono(datos.getTelefono());
                    cliente.setDireccion(datos.getDireccion());

                    return ResponseEntity.ok(
                            clienteRepository.save(cliente));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ELIMINAR CLIENTE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {

        if (!clienteRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        clienteRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}

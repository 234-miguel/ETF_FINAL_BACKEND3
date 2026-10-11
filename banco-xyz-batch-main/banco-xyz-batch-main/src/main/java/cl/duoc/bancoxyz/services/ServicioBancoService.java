package cl.duoc.bancoxyz.services;

import cl.duoc.bancoxyz.exceptions.ServicioBancoNoDisponibleException;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ServicioBancoService {

    private final ConcurrentHashMap<Long, AtomicInteger> intentos =
            new ConcurrentHashMap<>();

    public void verificarDisponibilidad(Long id) {

        if (id == null) {
            return;
        }

        AtomicInteger contador =
                intentos.computeIfAbsent(id, key -> new AtomicInteger(0));

        int intentoActual = contador.incrementAndGet();

        // Simulación de error temporal para demostrar RETRY.
        if (id % 7 == 0 && intentoActual == 1) {
            throw new ServicioBancoNoDisponibleException(
                    "Servicio temporalmente no disponible para registro: " + id
            );
        }
    }
}
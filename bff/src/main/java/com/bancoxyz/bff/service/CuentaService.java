
package com.bancoxyz.bff.service;

import com.bancoxyz.bff.model.Cuenta;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class CuentaService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String coreBaseUrl;

    public CuentaService(
            @Value("${core.base-url}") String coreBaseUrl) {
        this.coreBaseUrl = coreBaseUrl;
    }

    public List<Cuenta> obtenerCuentas() {
        try {
            ResponseEntity<List<Cuenta>> respuesta =
                    restTemplate.exchange(
                            coreBaseUrl + "/core/cuentas",
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<List<Cuenta>>() {}
                    );

            return respuesta.getBody() == null
                    ? List.of()
                    : respuesta.getBody();

        } catch (RestClientException e) {
            throw new IllegalStateException(
                    "No se pudo consultar el Core bancario", e);
        }
    }

    public Cuenta obtenerCuentaPorId(Long id) {
        try {
            Cuenta cuenta = restTemplate.getForObject(
                    coreBaseUrl + "/core/cuentas/{id}",
                    Cuenta.class,
                    id
            );

            if (cuenta == null) {
                throw new IllegalArgumentException(
                        "La cuenta " + id + " no existe");
            }

            return cuenta;

        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException(
                    "La cuenta " + id + " no existe", e);

        } catch (RestClientException e) {
            throw new IllegalStateException(
                    "No se pudo consultar el Core bancario", e);
        }
    }

    public double retirar(Long id, Double monto) {

        if (monto == null || !Double.isFinite(monto) || monto <= 0) {
            throw new IllegalArgumentException(
                    "El monto debe ser mayor que cero");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Double>> solicitud =
                new HttpEntity<>(
                        Map.of("monto", monto),
                        headers
                );

        try {
            ResponseEntity<Cuenta> respuesta =
                    restTemplate.exchange(
                            coreBaseUrl + "/core/cuentas/{id}/retiro",
                            HttpMethod.POST,
                            solicitud,
                            Cuenta.class,
                            id
                    );

            Cuenta cuentaActualizada = respuesta.getBody();

            if (cuentaActualizada == null) {
                throw new IllegalStateException(
                        "El Core no devolvio la cuenta actualizada");
            }

            return cuentaActualizada.getSaldo();

        } catch (org.springframework.web.client.HttpClientErrorException.BadRequest e) {
            throw new IllegalArgumentException(
                    "El Core rechazo el retiro: " + e.getResponseBodyAsString(), e);

        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException(
                    "La cuenta " + id + " no existe", e);

        } catch (RestClientException e) {
            throw new IllegalStateException(
                    "No se pudo realizar el retiro en el Core bancario", e);
        }
    }
}

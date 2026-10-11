package cl.duoc.bancoxyz.processors;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.batch.item.ItemProcessor;

import cl.duoc.bancoxyz.dtos.InteresDTO;
import cl.duoc.bancoxyz.entities.InteresEntity;
import cl.duoc.bancoxyz.exceptions.BancoValidationException;

public class InteresProcessor
        implements ItemProcessor<InteresDTO, InteresEntity> {

    private static final BigDecimal TASA_AHORRO =
            new BigDecimal("0.02");

    private static final BigDecimal TASA_PRESTAMO =
            new BigDecimal("0.05");

    @Override
    public InteresEntity process(InteresDTO dto) {

        if (dto.getCuentaId() == null) {
            throw new BancoValidationException(
                    "Cuenta sin ID"
            );
        }

        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new BancoValidationException(
                    "Nombre vacío para la cuenta " + dto.getCuentaId()
            );
        }

        if (dto.getSaldo() == null || dto.getSaldo().isBlank()) {
            throw new BancoValidationException(
                    "Saldo vacío para la cuenta " + dto.getCuentaId()
            );
        }

        BigDecimal saldoInicial;

        try {
            saldoInicial = new BigDecimal(dto.getSaldo());
        } catch (NumberFormatException e) {
            throw new BancoValidationException(
                    "Saldo inválido para la cuenta " + dto.getCuentaId()
            );
        }

        if (saldoInicial.compareTo(BigDecimal.ZERO) < 0) {
            throw new BancoValidationException(
                    "Saldo negativo para la cuenta " + dto.getCuentaId()
            );
        }

        if (dto.getEdad() == null || dto.getEdad() < 18 || dto.getEdad() > 100) {
            throw new BancoValidationException(
                    "Edad inválida para la cuenta " + dto.getCuentaId()
            );
        }

        if (dto.getTipo() == null || dto.getTipo().isBlank()) {
            throw new BancoValidationException(
                    "Tipo de cuenta vacío para " + dto.getCuentaId()
            );
        }

        String tipo = dto.getTipo().toUpperCase();

        BigDecimal tasa;

        if ("AHORRO".equals(tipo)) {
            tasa = TASA_AHORRO;
        } else if ("PRESTAMO".equals(tipo)) {
            tasa = TASA_PRESTAMO;
        } else {
            throw new BancoValidationException(
                    "Tipo de cuenta inválido para " + dto.getCuentaId()
            );
        }

        BigDecimal interesCalculado =
                saldoInicial.multiply(tasa)
                        .setScale(2, RoundingMode.HALF_UP);

        BigDecimal saldoFinal = saldoInicial.add(interesCalculado);

        return InteresEntity.builder()
                .cuentaId(dto.getCuentaId())
                .nombre(dto.getNombre())
                .saldoInicial(saldoInicial)
                .edad(dto.getEdad())
                .tipo(tipo)
                .interesCalculado(interesCalculado)
                .saldoFinal(saldoFinal)
                .estado("PROCESADO")
                .build();
    }
}
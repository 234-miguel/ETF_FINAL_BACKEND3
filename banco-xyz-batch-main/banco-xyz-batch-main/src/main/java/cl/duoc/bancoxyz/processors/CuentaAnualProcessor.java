package cl.duoc.bancoxyz.processors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.batch.item.ItemProcessor;

import cl.duoc.bancoxyz.dtos.CuentaAnualDTO;
import cl.duoc.bancoxyz.entities.CuentaAnualEntity;
import cl.duoc.bancoxyz.exceptions.BancoValidationException;

public class CuentaAnualProcessor
        implements ItemProcessor<CuentaAnualDTO, CuentaAnualEntity> {

    private static final DateTimeFormatter FORMATO_NORMAL =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter FORMATO_ALTERNATIVO =
            DateTimeFormatter.ofPattern("yyyy/MM/dd");

    @Override
    public CuentaAnualEntity process(CuentaAnualDTO dto) {

        if (dto.getCuentaId() == null) {
            throw new BancoValidationException(
                    "Registro anual sin ID de cuenta"
            );
        }

        if (dto.getTransaccion() == null || dto.getTransaccion().isBlank()) {
            throw new BancoValidationException(
                    "Tipo de transacción vacío para la cuenta "
                            + dto.getCuentaId()
            );
        }

        if (dto.getMonto() == null || dto.getMonto() <= 0) {
            throw new BancoValidationException(
                    "Monto inválido para la cuenta "
                            + dto.getCuentaId()
            );
        }

        if (dto.getDescripcion() == null || dto.getDescripcion().isBlank()) {
            throw new BancoValidationException(
                    "Descripción vacía para la cuenta "
                            + dto.getCuentaId()
            );
        }

        LocalDate fecha = convertirFecha(dto.getFecha());

        return CuentaAnualEntity.builder()
                .cuentaId(dto.getCuentaId())
                .fecha(fecha)
                .transaccion(dto.getTransaccion().toUpperCase())
                .monto(BigDecimal.valueOf(dto.getMonto()))
                .descripcion(dto.getDescripcion())
                .estado("PROCESADO")
                .build();
    }

    private LocalDate convertirFecha(String fecha) {

        if (fecha == null || fecha.isBlank()) {
            throw new BancoValidationException(
                    "Fecha vacía en registro anual"
            );
        }

        try {
            return LocalDate.parse(fecha, FORMATO_NORMAL);
        } catch (DateTimeParseException e) {

            try {
                return LocalDate.parse(fecha, FORMATO_ALTERNATIVO);
            } catch (DateTimeParseException ex) {
                throw new BancoValidationException(
                        "Formato de fecha inválido: " + fecha
                );
            }
        }
    }
}

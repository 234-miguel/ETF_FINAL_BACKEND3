package cl.duoc.bancoxyz.processors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.batch.item.ItemProcessor;

import cl.duoc.bancoxyz.dtos.TransaccionDTO;
import cl.duoc.bancoxyz.entities.TransaccionEntity;
import cl.duoc.bancoxyz.exceptions.BancoValidationException;

public class TransaccionProcessor
        implements ItemProcessor<TransaccionDTO, TransaccionEntity> {

    private static final DateTimeFormatter FORMATO_NORMAL =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter FORMATO_ALTERNATIVO =
            DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private static final DateTimeFormatter FORMATO_DIA_MES_ANIO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public TransaccionEntity process(TransaccionDTO dto) {

        if (dto.getId() == null) {
            throw new BancoValidationException(
                    "Transacción sin ID"
            );
        }

        if (dto.getMonto() == null || dto.getMonto() <= 0) {
            throw new BancoValidationException(
                    "Monto inválido para la transacción " + dto.getId()
            );
        }

        if (dto.getTipo() == null || dto.getTipo().isBlank()) {
            throw new BancoValidationException(
                    "Tipo vacío para la transacción " + dto.getId()
            );
        }

        LocalDate fecha = convertirFecha(dto.getFecha());

        return TransaccionEntity.builder()
                .id(dto.getId())
                .fecha(fecha)
                .monto(BigDecimal.valueOf(dto.getMonto()))
                .tipo(dto.getTipo().toUpperCase())
                .estado("PROCESADA")
                .build();
    }

    private LocalDate convertirFecha(String fecha) {

        if (fecha == null || fecha.isBlank()) {
            throw new BancoValidationException(
                    "Fecha vacía en la transacción"
            );
        }

        try {
            return LocalDate.parse(fecha, FORMATO_NORMAL);

        } catch (DateTimeParseException e) {

            try {
                return LocalDate.parse(fecha, FORMATO_ALTERNATIVO);

            } catch (DateTimeParseException e2) {

                try {
                    return LocalDate.parse(fecha, FORMATO_DIA_MES_ANIO);

                } catch (DateTimeParseException e3) {

                    throw new BancoValidationException(
                            "Formato de fecha inválido: " + fecha
                    );
                }
            }
        }
    }
}
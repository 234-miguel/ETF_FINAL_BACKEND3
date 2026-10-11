package cl.duoc.bancoxyz.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransaccionEntity {

    private Long id;
    private LocalDate fecha;
    private BigDecimal monto;
    private String tipo;
    private String estado;
}
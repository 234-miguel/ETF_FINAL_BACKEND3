package cl.duoc.bancoxyz.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InteresEntity {

    private Long cuentaId;
    private String nombre;
    private BigDecimal saldoInicial;
    private Integer edad;
    private String tipo;
    private BigDecimal interesCalculado;
    private BigDecimal saldoFinal;
    private String estado;
}
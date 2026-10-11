package cl.duoc.bancoxyz.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CuentaAnualDTO {

    private Long cuentaId;
    private String fecha;
    private String transaccion;
    private Double monto;
    private String descripcion;
}
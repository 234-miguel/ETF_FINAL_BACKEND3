package cl.duoc.bancoxyz.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InteresDTO {

    private Long cuentaId;
    private String nombre;
    private String saldo;
    private Integer edad;
    private String tipo;
}
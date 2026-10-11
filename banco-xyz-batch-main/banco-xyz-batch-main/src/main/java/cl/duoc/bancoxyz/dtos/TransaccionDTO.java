package cl.duoc.bancoxyz.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransaccionDTO {

    private Long id;
    private String fecha;
    private Double monto;
    private String tipo;
}
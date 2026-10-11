package cl.duoc.bancoxyz.services;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;

@Service
public class ReporteTransaccionesService {

    private final JdbcTemplate jdbcTemplate;

    public ReporteTransaccionesService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void generarReporte() throws IOException {

        Integer cantidad = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM TRANSACCIONES_PROCESADAS",
                Integer.class
        );

        BigDecimal total = jdbcTemplate.queryForObject(
                "SELECT NVL(SUM(MONTO), 0) FROM TRANSACCIONES_PROCESADAS",
                BigDecimal.class
        );

        File carpeta = new File("reportes");

        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }

        File archivo = new File(
                carpeta,
                "reporte_transacciones.txt"
        );

        try (FileWriter writer = new FileWriter(archivo)) {

            writer.write("REPORTE DIARIO DE TRANSACCIONES\n");
            writer.write("==============================\n");
            writer.write("Transacciones procesadas: " + cantidad + "\n");
            writer.write("Monto total procesado: " + total + "\n");
        }
    }
}
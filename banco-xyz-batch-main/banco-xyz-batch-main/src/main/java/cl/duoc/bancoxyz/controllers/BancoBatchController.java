package cl.duoc.bancoxyz.controllers;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/batch")
public class BancoBatchController {

    private final JobLauncher jobLauncher;

    private final Job transaccionesJob;
    private final Job interesesJob;
    private final Job cuentasAnualesJob;

    public BancoBatchController(
            JobLauncher jobLauncher,
            @Qualifier("transaccionesJob") Job transaccionesJob,
            @Qualifier("interesesJob") Job interesesJob,
            @Qualifier("cuentasAnualesJob") Job cuentasAnualesJob) {

        this.jobLauncher = jobLauncher;
        this.transaccionesJob = transaccionesJob;
        this.interesesJob = interesesJob;
        this.cuentasAnualesJob = cuentasAnualesJob;
    }

    @PostMapping("/transacciones")
    public ResponseEntity<Map<String, Object>> ejecutarTransacciones(
            @RequestParam(defaultValue = "2024") String periodo)
            throws Exception {

        return ejecutarJob(transaccionesJob, periodo);
    }

    @PostMapping("/intereses")
    public ResponseEntity<Map<String, Object>> ejecutarIntereses(
            @RequestParam(defaultValue = "2024-12") String periodo)
            throws Exception {

        return ejecutarJob(interesesJob, periodo);
    }

    @PostMapping("/cuentas-anuales")
    public ResponseEntity<Map<String, Object>> ejecutarCuentasAnuales(
            @RequestParam(defaultValue = "2024") String periodo)
            throws Exception {

        return ejecutarJob(cuentasAnualesJob, periodo);
    }

    private ResponseEntity<Map<String, Object>> ejecutarJob(
            Job job, String periodo) throws Exception {

        JobParameters parametros = new JobParametersBuilder()
                .addString("periodo", periodo)
                .toJobParameters();

        JobExecution ejecucion = jobLauncher.run(job, parametros);

        Map<String, Object> resultado = new LinkedHashMap<>();

        resultado.put("job", job.getName());
        resultado.put("executionId", ejecucion.getId());
        resultado.put("estado", ejecucion.getStatus().toString());
        resultado.put("exitCode",
                ejecucion.getExitStatus().getExitCode());
        resultado.put("mensaje",
                ejecucion.getExitStatus().getExitDescription());

        return ResponseEntity.ok(resultado);
    }
}
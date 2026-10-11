package cl.duoc.bancoxyz.config;

import cl.duoc.bancoxyz.dtos.CuentaAnualDTO;
import cl.duoc.bancoxyz.dtos.InteresDTO;
import cl.duoc.bancoxyz.dtos.TransaccionDTO;
import cl.duoc.bancoxyz.entities.CuentaAnualEntity;
import cl.duoc.bancoxyz.entities.InteresEntity;
import cl.duoc.bancoxyz.entities.TransaccionEntity;
import cl.duoc.bancoxyz.exceptions.ServicioBancoNoDisponibleException;
import cl.duoc.bancoxyz.policies.BancoSkipPolicy;
import cl.duoc.bancoxyz.processors.CuentaAnualProcessor;
import cl.duoc.bancoxyz.processors.InteresProcessor;
import cl.duoc.bancoxyz.processors.TransaccionProcessor;
import cl.duoc.bancoxyz.services.ServicioBancoService;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;

import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.core.task.TaskExecutor;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;


import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import org.springframework.retry.backoff.ExponentialBackOffPolicy;

import org.springframework.transaction.PlatformTransactionManager;

import cl.duoc.bancoxyz.services.ReporteTransaccionesService;

import javax.sql.DataSource;

@Configuration
public class BatchConfig {

    private static final int CHUNK_SIZE = 10;

    private final ReporteTransaccionesService reporteTransaccionesService;

    public BatchConfig(ReporteTransaccionesService reporteTransaccionesService) {
        this.reporteTransaccionesService = reporteTransaccionesService;
    }
    // =========================================================
    // SKIP POLICY
    // =========================================================

    @Bean
    public BancoSkipPolicy bancoSkipPolicy() {
        return new BancoSkipPolicy();
    }

    // =========================================================
    // RETRY BACKOFF
    // =========================================================

    @Bean
    public ExponentialBackOffPolicy backOffPolicy() {

        ExponentialBackOffPolicy policy =
                new ExponentialBackOffPolicy();

        policy.setInitialInterval(500);
        policy.setMultiplier(2.0);
        policy.setMaxInterval(3000);

        return policy;
    }

    // Cada particion obtiene su propio reader, sin compartir estado entre hilos.
    // start y end son indices de registros (no incluyen la cabecera).
    @Bean
    @StepScope
    public FlatFileItemReader<TransaccionDTO> transaccionReader(
            @Value("#{stepExecutionContext['start']}") Integer start,
            @Value("#{stepExecutionContext['end']}") Integer end) {
        return new FlatFileItemReaderBuilder<TransaccionDTO>()
                .name("transaccionReaderParticionado")
                .resource(new ClassPathResource("data/transacciones.csv"))
                .linesToSkip(start + 1)
                .maxItemCount(end - start)
                .delimited().delimiter(",")
                .names("id", "fecha", "monto", "tipo")
                .targetType(TransaccionDTO.class)
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<InteresDTO> interesReader(
            @Value("#{stepExecutionContext['start']}") Integer start,
            @Value("#{stepExecutionContext['end']}") Integer end) {
        return new FlatFileItemReaderBuilder<InteresDTO>()
                .name("interesReaderParticionado")
                .resource(new ClassPathResource("data/intereses.csv"))
                .linesToSkip(start + 1)
                .maxItemCount(end - start)
                .delimited().delimiter(",")
                .names("cuentaId", "nombre", "saldo", "edad", "tipo")
                .targetType(InteresDTO.class)
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<CuentaAnualDTO> cuentaAnualReader(
            @Value("#{stepExecutionContext['start']}") Integer start,
            @Value("#{stepExecutionContext['end']}") Integer end) {
        return new FlatFileItemReaderBuilder<CuentaAnualDTO>()
                .name("cuentaAnualReaderParticionado")
                .resource(new ClassPathResource("data/cuentas_anuales.csv"))
                .linesToSkip(start + 1)
                .maxItemCount(end - start)
                .delimited().delimiter(",")
                .names("cuentaId", "fecha", "transaccion", "monto", "descripcion")
                .targetType(CuentaAnualDTO.class)
                .build();
    }

    // Calcula particiones sin cargar todo el CSV en memoria.
    private Partitioner csvPartitioner(String resourcePath, int recordsPerPartition) {
        return gridSize -> {
            int total = 0;
            ClassPathResource resource = new ClassPathResource(resourcePath);
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                reader.readLine(); // cabecera
                while (reader.readLine() != null) total++;
            } catch (IOException ex) {
                throw new IllegalStateException("No se pudo leer " + resourcePath, ex);
            }
            Map<String, ExecutionContext> partitions = new LinkedHashMap<>();
            for (int start = 0, n = 0; start < total; start += recordsPerPartition, n++) {
                ExecutionContext ctx = new ExecutionContext();
                ctx.putInt("start", start);
                ctx.putInt("end", Math.min(start + recordsPerPartition, total));
                partitions.put("particion" + n, ctx);
            }
            return partitions;
        };
    }

    @Bean
    public Partitioner transaccionPartitioner() {
        return csvPartitioner("data/transacciones.csv", 100);
    }

    @Bean
    public Partitioner interesPartitioner() {
        return csvPartitioner("data/intereses.csv", 100);
    }

    @Bean
    public Partitioner cuentaAnualPartitioner() {
        return csvPartitioner("data/cuentas_anuales.csv", 100);
    }

    // =========================================================
    // PROCESSORS
    // =========================================================

    @Bean
    public ItemProcessor<TransaccionDTO, TransaccionEntity>
    transaccionProcessor(
            ServicioBancoService servicioBancoService) {

        TransaccionProcessor processor =
                new TransaccionProcessor();

        return item -> {

            servicioBancoService
                    .verificarDisponibilidad(item.getId());

            return processor.process(item);
        };
    }

    @Bean
    public ItemProcessor<InteresDTO, InteresEntity>
    interesProcessor(
            ServicioBancoService servicioBancoService) {

        InteresProcessor processor =
                new InteresProcessor();

        return item -> {

            servicioBancoService
                    .verificarDisponibilidad(item.getCuentaId());

            return processor.process(item);
        };
    }

    @Bean
    public ItemProcessor<CuentaAnualDTO, CuentaAnualEntity>
    cuentaAnualProcessor(
            ServicioBancoService servicioBancoService) {

        CuentaAnualProcessor processor =
                new CuentaAnualProcessor();

        return item -> {

            servicioBancoService
                    .verificarDisponibilidad(item.getCuentaId());

            return processor.process(item);
        };
    }

    // =========================================================
    // WRITERS
    // =========================================================

    @Bean
    public JdbcBatchItemWriter<TransaccionEntity>
    transaccionWriter(DataSource dataSource) {

        return new JdbcBatchItemWriterBuilder<TransaccionEntity>()
                .dataSource(dataSource)
                .sql("""
                        INSERT INTO TRANSACCIONES_PROCESADAS
                        (ID, FECHA, MONTO, TIPO, ESTADO)
                        VALUES
                        (:id, :fecha, :monto, :tipo, :estado)
                        """)
                .beanMapped()
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<InteresEntity>
    interesWriter(DataSource dataSource) {

        return new JdbcBatchItemWriterBuilder<InteresEntity>()
                .dataSource(dataSource)
                .sql("""
                        INSERT INTO INTERESES_PROCESADOS
                        (
                            CUENTA_ID,
                            NOMBRE,
                            SALDO_INICIAL,
                            EDAD,
                            TIPO,
                            INTERES_CALCULADO,
                            SALDO_FINAL,
                            ESTADO
                        )
                        VALUES
                        (
                            :cuentaId,
                            :nombre,
                            :saldoInicial,
                            :edad,
                            :tipo,
                            :interesCalculado,
                            :saldoFinal,
                            :estado
                        )
                        """)
                .beanMapped()
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<CuentaAnualEntity>
    cuentaAnualWriter(DataSource dataSource) {

        return new JdbcBatchItemWriterBuilder<CuentaAnualEntity>()
                .dataSource(dataSource)
                .sql("""
                        INSERT INTO CUENTAS_ANUALES_PROCESADAS
                        (
                            CUENTA_ID,
                            FECHA,
                            TRANSACCION,
                            MONTO,
                            DESCRIPCION,
                            ESTADO
                        )
                        VALUES
                        (
                            :cuentaId,
                            :fecha,
                            :transaccion,
                            :monto,
                            :descripcion,
                            :estado
                        )
                        """)
                .beanMapped()
                .build();
    }

    // =========================================================
    // STEPS
    // =========================================================

    @Bean
    public Step transaccionStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("transaccionReader") FlatFileItemReader<TransaccionDTO>
                    transaccionReader,
            @Qualifier("transaccionProcessor")
            ItemProcessor<TransaccionDTO, TransaccionEntity>
                    transaccionProcessor,
            JdbcBatchItemWriter<TransaccionEntity>
                    transaccionWriter,
            BancoSkipPolicy bancoSkipPolicy,
            ExponentialBackOffPolicy backOffPolicy) {

        return new StepBuilder(
                "transaccionStep",
                jobRepository
        )
                .<TransaccionDTO, TransaccionEntity>chunk(
                        CHUNK_SIZE,
                        transactionManager
                )
                .reader(transaccionReader)
                .processor(transaccionProcessor)
                .writer(transaccionWriter)

                .faultTolerant()

                .skipPolicy(bancoSkipPolicy)

                .retry(
                        ServicioBancoNoDisponibleException.class
                )
                .retryLimit(3)
                .backOffPolicy(backOffPolicy)

                .build();
    }

    @Bean
    public Step reporteTransaccionesStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager) {

        return new StepBuilder("reporteTransaccionesStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {

                    reporteTransaccionesService.generarReporte();

                    return RepeatStatus.FINISHED;

                }, transactionManager)
                .build();
    }

    @Bean
    public Step interesStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("interesReader") FlatFileItemReader<InteresDTO>
                    interesReader,
            @Qualifier("interesProcessor")
            ItemProcessor<InteresDTO, InteresEntity>
                    interesProcessor,
            JdbcBatchItemWriter<InteresEntity>
                    interesWriter,
            BancoSkipPolicy bancoSkipPolicy,
            ExponentialBackOffPolicy backOffPolicy) {

        return new StepBuilder(
                "interesStep",
                jobRepository
        )
                .<InteresDTO, InteresEntity>chunk(
                        CHUNK_SIZE,
                        transactionManager
                )
                .reader(interesReader)
                .processor(interesProcessor)
                .writer(interesWriter)

                .faultTolerant()

                .skipPolicy(bancoSkipPolicy)

                .retry(
                        ServicioBancoNoDisponibleException.class
                )
                .retryLimit(3)
                .backOffPolicy(backOffPolicy)


                .build();
    }

    @Bean
    public Step cuentaAnualStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("cuentaAnualReader") FlatFileItemReader<CuentaAnualDTO>
                    cuentaAnualReader,
            @Qualifier("cuentaAnualProcessor")
            ItemProcessor<CuentaAnualDTO, CuentaAnualEntity>
                    cuentaAnualProcessor,
            JdbcBatchItemWriter<CuentaAnualEntity>
                    cuentaAnualWriter,
            BancoSkipPolicy bancoSkipPolicy,
            ExponentialBackOffPolicy backOffPolicy) {

        return new StepBuilder(
                "cuentaAnualStep",
                jobRepository
        )
                .<CuentaAnualDTO, CuentaAnualEntity>chunk(
                        CHUNK_SIZE,
                        transactionManager
                )
                .reader(cuentaAnualReader)
                .processor(cuentaAnualProcessor)
                .writer(cuentaAnualWriter)

                .faultTolerant()

                .skipPolicy(bancoSkipPolicy)

                .retry(
                        ServicioBancoNoDisponibleException.class
                )
                .retryLimit(3)
                .backOffPolicy(backOffPolicy)


                .build();
    }

    // Los steps maestros distribuyen particiones entre hilos.
    @Bean
    public Step transaccionMasterStep(JobRepository repository,
                                      @Qualifier("transaccionStep") Step worker,
                                      @Qualifier("transaccionPartitioner") Partitioner partitioner,
                                      @Qualifier("batchTaskExecutor") TaskExecutor executor) {
        return new StepBuilder("transaccionMasterStep", repository)
                .partitioner("transaccionStep", partitioner)
                .step(worker)
                .gridSize(6)
                .taskExecutor(executor)
                .build();
    }

    @Bean
    public Step interesMasterStep(JobRepository repository,
                                  @Qualifier("interesStep") Step worker,
                                  @Qualifier("interesPartitioner") Partitioner partitioner,
                                  @Qualifier("batchTaskExecutor") TaskExecutor executor) {
        return new StepBuilder("interesMasterStep", repository)
                .partitioner("interesStep", partitioner)
                .step(worker)
                .gridSize(6)
                .taskExecutor(executor)
                .build();
    }

    @Bean
    public Step cuentaAnualMasterStep(JobRepository repository,
                                      @Qualifier("cuentaAnualStep") Step worker,
                                      @Qualifier("cuentaAnualPartitioner") Partitioner partitioner,
                                      @Qualifier("batchTaskExecutor") TaskExecutor executor) {
        return new StepBuilder("cuentaAnualMasterStep", repository)
                .partitioner("cuentaAnualStep", partitioner)
                .step(worker)
                .gridSize(6)
                .taskExecutor(executor)
                .build();
    }

    @Bean
    public Job transaccionesJob(JobRepository repository,
                                @Qualifier("transaccionMasterStep") Step master,
                                @Qualifier("reporteTransaccionesStep") Step reporte) {
        return new JobBuilder("transaccionesJob", repository)
                .start(master).next(reporte).build();
    }

    @Bean
    public Job interesesJob(JobRepository repository,
                            @Qualifier("interesMasterStep") Step master) {
        return new JobBuilder("interesesJob", repository).start(master).build();
    }

    @Bean
    public Job cuentasAnualesJob(JobRepository repository,
                                 @Qualifier("cuentaAnualMasterStep") Step master) {
        return new JobBuilder("cuentasAnualesJob", repository).start(master).build();
    }
}


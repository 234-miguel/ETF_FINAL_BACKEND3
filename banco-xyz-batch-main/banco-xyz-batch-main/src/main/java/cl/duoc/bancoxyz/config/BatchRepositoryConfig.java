package cl.duoc.bancoxyz.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Isolation;

@Configuration
@EnableBatchProcessing
public class BatchRepositoryConfig extends DefaultBatchConfiguration {

    @Override
    protected Isolation getIsolationLevelForCreate() {
        return Isolation.READ_COMMITTED;
    }
}
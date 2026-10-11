package cl.duoc.bancoxyz.policies;

import cl.duoc.bancoxyz.exceptions.BancoValidationException;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.dao.DataIntegrityViolationException;

public class BancoSkipPolicy implements SkipPolicy {

    private static final int LIMITE_SKIPS = 1000;

    @Override
    public boolean shouldSkip(Throwable throwable, long skipCount)
            throws SkipLimitExceededException {

        if (skipCount >= LIMITE_SKIPS) {
            return false;
        }

        return throwable instanceof FlatFileParseException
                || throwable instanceof BancoValidationException
                || throwable instanceof DataIntegrityViolationException;
    }
}
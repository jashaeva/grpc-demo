package omsu.exception;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;

@GrpcAdvice
public class GrpcExceptionAdvice {

    private static final Logger log = LoggerFactory.getLogger(GrpcExceptionAdvice.class);


    @GrpcExceptionHandler(DataIntegrityViolationException.class)
    public StatusRuntimeException handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.warn("Data integrity violation: {}", e.getMessage());

        if (e.getMessage().contains("inventory_stock_quantity_check")) {
            return Status.INVALID_ARGUMENT
                    .withDescription("count must be greater than or equal to 1")
                    .asRuntimeException();
        }
        return Status.INTERNAL
                .withDescription("Database integrity violation")
                .asRuntimeException();
    }

    @GrpcExceptionHandler(DuplicateKeyException.class)
    public StatusRuntimeException handleDuplicateKeyException(DuplicateKeyException e) {
        log.warn("Duplicate key violation: {}", e.getMessage());
        return Status.ALREADY_EXISTS.withDescription(e.getMessage())
                .asRuntimeException();
    }

    @GrpcExceptionHandler(EntityNotFoundException.class)
    public StatusRuntimeException handleEntityNotFoundException(EntityNotFoundException e) {
        log.info("Entity not found: {}", e.getMessage()); // INFO - нормальная бизнес-ситуация
        return Status.NOT_FOUND.withDescription(e.getMessage())
                .asRuntimeException();
    }
}

package omsu.inventory.exception;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

@GrpcAdvice
@Component
public class GrpcExceptionAdvice {

    @GrpcExceptionHandler(StatusRuntimeException.class)
    public StatusRuntimeException handleStatusRuntimeException(StatusRuntimeException e) {
        System.out.println("=== Handling StatusRuntimeException ===");
        System.out.println("Status: " + e.getStatus());
        System.out.println("Message: " + e.getMessage());
        e.printStackTrace();
        return e;
    }

    @GrpcExceptionHandler(DataIntegrityViolationException.class)
    public StatusRuntimeException handleDataIntegrityViolation(DataIntegrityViolationException e) {
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
        return Status.ALREADY_EXISTS.withDescription(e.getMessage())
                .asRuntimeException();
    }

    @GrpcExceptionHandler(EntityNotFoundException.class)
    public StatusRuntimeException handleEntityNotFoundException(EntityNotFoundException e) {
        return Status.NOT_FOUND.withDescription(e.getMessage())
                .asRuntimeException();
    }
}

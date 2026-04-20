package omsu.inventory.exception;

import io.envoyproxy.pgv.ValidationException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import jakarta.validation.ConstraintViolationException;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

//@GrpcAdvice
@Component
public class GrpcExceptionAdvice {

    @GrpcExceptionHandler(StatusRuntimeException.class)
    public void ignoreStatusRuntimeException(StatusRuntimeException e) {
        // Пустой метод - исключение не обрабатывается
        // Фреймворк сам отправит его клиенту
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

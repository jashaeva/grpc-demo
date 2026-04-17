package omsu.inventory.exception;

import io.envoyproxy.pgv.ValidationException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import jakarta.validation.ConstraintViolationException;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@GrpcAdvice
@Component
public class GrpcExceptionAdvice {

//    @GrpcExceptionHandler(ConstraintViolationException.class)
//    public StatusRuntimeException handleConstraintViolation(ConstraintViolationException e) {
//        String message = e.getConstraintViolations().stream()
//                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
//                .collect(Collectors.joining(", "));
//
//        // Возвращаем INVALID_ARGUMENT, не пытаясь закрыть call вручную
//        return Status.INVALID_ARGUMENT
//                .withDescription("Validation error: " + message)
//                .asRuntimeException();
//    }
//    @GrpcExceptionHandler(RuntimeException.class)
//    public StatusRuntimeException handleInvalidArgument(RuntimeException e) {
//        Metadata metadata = new Metadata();
//        Metadata.Key<String> ERROR_MESSAGE =
//                Metadata.Key.of("Фигня происходит", Metadata.ASCII_STRING_MARSHALLER);
//        metadata.put(ERROR_MESSAGE, e.getMessage());
//        return Status.INTERNAL.asRuntimeException(metadata);
//    }

//    @GrpcExceptionHandler(ValidationException.class)
//    public StatusRuntimeException handleValidation(ValidationException e) {
//        // Возвращаем клиенту статус INVALID_ARGUMENT с понятным сообщением
//        return Status.INVALID_ARGUMENT
//                .withDescription(e.getMessage())
//                .asRuntimeException();
//    }
@GrpcExceptionHandler(StatusRuntimeException.class)
public StatusRuntimeException handleStatusRuntimeException(StatusRuntimeException e) {
    // Ничего не делаем, просто возвращаем исходное исключение
    // Оно уже корректно и не требует повторной обработки
    return e;
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

//    @GrpcExceptionHandler(Exception.class)
//    public StatusRuntimeException handleGenericException(Exception e) {
////        log.error("Internal error", e);
//        return Status.INTERNAL
//                .withDescription("Internal server error")
//                .asRuntimeException();
//    }
}

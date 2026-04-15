package omsu.inventory.exception;

import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusException;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

import java.sql.SQLException;

@GrpcAdvice
public class GrpcExceptionAdvice {


    @GrpcExceptionHandler(RuntimeException.class)
    public Status handleInvalidArgument(Exception e) {
        Metadata metadata = new Metadata();
        Metadata.Key<String> ERROR_MESSAGE =
                Metadata.Key.of("Фигня происходит", Metadata.ASCII_STRING_MARSHALLER);
        metadata.put(ERROR_MESSAGE, e.getMessage());

        return Status.INTERNAL.asException(metadata).getStatus();
    }

    @GrpcExceptionHandler(SQLException.class)
    public StatusException handleResourceNotFoundException(SQLException e) {
        Status status = Status.INTERNAL.withDescription("SQL error").withCause(e);

        // Инициализация метадаты
        Metadata metadata = new Metadata();
        Metadata.Key<String> SQL_STATE =
                Metadata.Key.of("sql-state", Metadata.ASCII_STRING_MARSHALLER);
        Metadata.Key<String> ERROR_CODE =
                Metadata.Key.of("sql-error-code", Metadata.ASCII_STRING_MARSHALLER);
        Metadata.Key<String> ERROR_MESSAGE =
                Metadata.Key.of("sql-error-message", Metadata.ASCII_STRING_MARSHALLER);

        // Заполнение данными об ошибке
        metadata.put(SQL_STATE, e.getSQLState());
        metadata.put(ERROR_CODE, String.valueOf(e.getErrorCode()));
        metadata.put(ERROR_MESSAGE, e.getMessage());

        return status.asException(metadata);
    }
}

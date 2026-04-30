package omsu.exception;

import org.springframework.dao.EmptyResultDataAccessException;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String string, Throwable e) {
    }
}

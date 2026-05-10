package omsu.exception;

public class NotEnoughInventoryException extends RuntimeException {
    public NotEnoughInventoryException(String string) {
        super(string);
    }

    public NotEnoughInventoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotEnoughInventoryException(Throwable cause) { super(cause);}
}

package omsu.dto;

public record LogEvent(
        String method,
        String request,
        String response
) {
}

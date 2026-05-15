package omsu.utils;

import org.springframework.jdbc.support.KeyHolder;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

public final class UuidUtils {
    private UuidUtils(){}

    @Nullable
    public static UUID getUuid(KeyHolder keyHolder) {
        Map<String, Object> keys = keyHolder.getKeys();
        if (keys != null && keys.containsKey("id")) {
            Object id = keys.get("id");
            if (id instanceof UUID) {
                return (UUID) id;
            }
            else if (id instanceof String) {
                return UUID.fromString((String) id);
            } else if (id != null) {
                return UUID.fromString(id.toString());
            }
        }
        return null;
    }
}

package omsu.utils;

import com.google.protobuf.Timestamp;
import java.time.Instant;


/**
 * Proto - com.google.protobuf.Timestamp
 * Timestamp - java.sql.Timestamp
 * Instant == Instant
 */
public class TimestampConverter {

    public static Instant protoToInstant(Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }

    // Обратно
    public static Timestamp toProto(Instant instant) {
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

    public static java.sql.Timestamp protoToTimestamp(Timestamp proto) {
        Instant instant = Instant.ofEpochSecond(proto.getSeconds(), proto.getNanos());
        return java.sql.Timestamp.from(instant);
    }

    public static java.sql.Timestamp instantToTimestamp(Instant instant) {
        return java.sql.Timestamp.from(instant);
    }
}

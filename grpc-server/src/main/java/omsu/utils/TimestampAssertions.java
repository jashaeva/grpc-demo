package omsu.utils;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Assertions;

/**
 * Proto - com.google.protobuf.Timestamp
 * Timestamp - java.sql.Timestamp
 * Instant == Instant
 */
public class TimestampAssertions {

    /**
     * Сравнивает два Timestamp с учетом допустимой погрешности
     * @param expected ожидаемое значение
     * @param actual фактическое значение
     * @param toleranceNanos допустимая погрешность в наносекундах
     * @return true если разница не превышает погрешность
     */
    public static boolean equalsWithTolerance(Timestamp expected, Timestamp actual, long toleranceNanos) {
        if (expected == actual) return true;
        if (expected == null || actual == null) return false;

        if (expected.getSeconds() != actual.getSeconds()) return false;

        long nanosDiff = Math.abs(expected.getNanos() - actual.getNanos());
        return nanosDiff <= toleranceNanos;
    }

    /**
     * Сравнивает два Timestamp с погрешностью 1 миллисекунда (1_000_000 наносекунд)
     */
    public static boolean equalsWithDefaultTolerance(Timestamp expected, Timestamp actual) {
        return equalsWithTolerance(expected, actual, 1_000_000);
    }

    /**
     * Проверка Timestamp с учетом погрешности
     */
    public static void assertEqualsWithTolerance(Timestamp expected, Timestamp actual, long toleranceNanos) {
        if (!equalsWithTolerance(expected, actual, toleranceNanos)) {
            Assertions.fail(String.format(
                    "Timestamps are not equal within tolerance of %d nanos.%n" +
                            "Expected: seconds=%d, nanos=%d%n" +
                            "Actual:   seconds=%d, nanos=%d%n" +
                            "Difference in nanos: %d",
                    toleranceNanos,
                    expected.getSeconds(), expected.getNanos(),
                    actual.getSeconds(), actual.getNanos(),
                    Math.abs(expected.getNanos() - actual.getNanos())
            ));
        }
    }

    /**
     * Проверка Timestamp с погрешностью по умолчанию (1 мс)
     */
    public static void assertEqualsWithDefaultTolerance(Timestamp expected, Timestamp actual) {
        assertEqualsWithTolerance(expected, actual, 1_000_000);
    }
}



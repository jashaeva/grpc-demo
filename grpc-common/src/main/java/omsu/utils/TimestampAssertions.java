package omsu.utils;

import com.google.protobuf.Timestamp;

public class TimestampAssertions {

    public static boolean equalsWithTolerance(Timestamp expected, Timestamp actual, long toleranceNanos) {
        if (expected == actual) return true;
        if (expected == null || actual == null) return false;

        if (expected.getSeconds() != actual.getSeconds()) return false;

        long nanosDiff = Math.abs(expected.getNanos() - actual.getNanos());
        return nanosDiff <= toleranceNanos;
    }

    public static boolean equalsWithDefaultTolerance(Timestamp expected, Timestamp actual) {
        return equalsWithTolerance(expected, actual, 1_000_000);
    }

    /**
     * Проверка Timestamp с учетом погрешности
     * @throws AssertionError если значения не равны в пределах погрешности
     */
    public static void assertEqualsWithTolerance(Timestamp expected, Timestamp actual, long toleranceNanos) {
        if (!equalsWithTolerance(expected, actual, toleranceNanos)) {
            throw new AssertionError(String.format(
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

    public static void assertEqualsWithDefaultTolerance(Timestamp expected, Timestamp actual) {
        assertEqualsWithTolerance(expected, actual, 1_000_000);
    }
}
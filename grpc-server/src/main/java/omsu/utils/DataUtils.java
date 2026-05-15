package omsu.utils;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.Objects;

import net.datafaker.Faker;
import omsu.grpc.OrderStatus;

public class DataUtils {

    private static final Faker faker = new Faker(Locale.of("RU_ru"));


    @Nonnull
    public static String randomUsername() {
        return faker.name().fullName();
    }

    @Nonnull
    public static String randomPassword() {
        return faker.bothify("????####");
    }

    @Nonnull
    public static String randomName() {
        return faker.name().firstName();
    }

    @Nonnull
    public static long randomQuantity() {
        return Math.abs(faker.number().randomDigitNotZero());
    }

    @Nonnull
    public static long randomQuantity(long upper) {
        return faker.number().numberBetween(1, upper);
    }

    @Nonnull
    public static String randomInventory() {
        return faker.commerce().productName();
    }

    @Nonnull
    public static double randomAmount() {
        return faker.number().randomDouble(2, 1, 9999999);
    }

    @Nonnull
    public static String randomCategory() {
        return faker.food().fruit();
    }

    @Nonnull
    public static String randomSentence(int wordsCount) {
        return faker.lorem().sentence(wordsCount);
    }

    @Nonnull
    public static OrderStatus randomStatus() {
        return Objects.requireNonNull(OrderStatus.forNumber(faker.number().numberBetween(0, 4)));
    }

    @Nonnull
    public static OrderStatus randomStatusStart() {
        boolean status = faker.bool().bool();
        return ( status ? OrderStatus.PENDING : OrderStatus.CREATED );
    }
}


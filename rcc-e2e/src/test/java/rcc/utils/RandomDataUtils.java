package rcc.utils;

import com.github.javafaker.Faker;

public class RandomDataUtils {

    private static final Faker faker = new Faker();

    public static String randomUserName() {
        return faker.animal().name() + faker.animal().name();
    }

    public static String randomFirstName() {
        return faker.name().name();
    }

    public static String randomLastName() {
        return faker.name().lastName();
    }

    public static String randomCity() {
        return faker.address().city();
    }
}

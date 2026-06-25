package io.student.rococo.utils;

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

    public static String randomLongText(int minLength) {
        StringBuilder text = new StringBuilder();
        while (text.length() < minLength) {
            text.append(faker.lorem().paragraph()).append(" ");
        }
        return text.toString().trim();
    }
}

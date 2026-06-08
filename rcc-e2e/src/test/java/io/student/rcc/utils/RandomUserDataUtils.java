package io.student.rcc.utils;

import com.github.javafaker.Faker;

public class RandomUserDataUtils {

    private static final Faker faker = new Faker();

    public static String getRandomUserName() {
        return faker.animal().name() + faker.animal().name();
    }

    public static String getRandomFirstName() {
        return faker.name().name();
    }

    public static String getRandomLastName() {
        return faker.name().lastName();
    }
}

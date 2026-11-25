package io.student.rococo.utils;

import net.datafaker.Faker;

import java.util.concurrent.ThreadLocalRandom;

public class RandomDataUtils {

    private static final Faker faker = new Faker();

    private RandomDataUtils() {
    }

    public static String randomUserName() {
        String name = faker.animal().name() + faker.animal().name();
        return name.replaceAll("\\s+", "").toLowerCase();
    }

    public static String randomCity() {
        return faker.address().city();
    }

    public static String randomLongText(int exactLength) {
        String text = faker.lorem().paragraph();
        while (text.length() < exactLength) {
            text += " " + faker.lorem().paragraph();
        }
        return text.substring(0, exactLength);
    }

    public static String randomMaxLengthName(int length) {
        return faker.lorem().characters(length);
    }

    public static int randomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
package com.chheang.mengheak.logisticsapis.common.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Predicate;

import com.chheang.mengheak.logisticsapis.exception.ConflictException;

/**
 * Builds the human-facing reference codes (tracking numbers, trip codes). The date prefix makes
 * them sortable by booking day; the random tail keeps them unguessable, which matters because a
 * tracking number alone is enough to read a shipment's public status.
 */
@Component
public class ReferenceCodeGenerator {

    private static final DateTimeFormatter DATE_PART = DateTimeFormatter.ofPattern("yyMMdd");
    private static final String ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int RANDOM_LENGTH = 7;
    private static final int MAX_ATTEMPTS = 10;

    private final SecureRandom random = new SecureRandom();

    public String generateTrackingNumber(Predicate<String> alreadyExists) {
        return generate("LGX", alreadyExists);
    }

    public String generateTripCode(Predicate<String> alreadyExists) {
        return generate("TRP", alreadyExists);
    }

    private String generate(String prefix, Predicate<String> alreadyExists) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String candidate = "%s%s%s".formatted(prefix, LocalDate.now().format(DATE_PART), randomPart());
            if (!alreadyExists.test(candidate)) {
                return candidate;
            }
        }
        throw new ConflictException("Could not allocate a unique %s code, please retry".formatted(prefix));
    }

    private String randomPart() {
        StringBuilder builder = new StringBuilder(RANDOM_LENGTH);
        for (int i = 0; i < RANDOM_LENGTH; i++) {
            builder.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return builder.toString();
    }
}

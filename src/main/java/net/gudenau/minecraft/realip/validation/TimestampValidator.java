package net.gudenau.minecraft.realip.validation;

/**
 * A simple interface that is used by all timestamp validators.
 */
public sealed interface TimestampValidator permits HttpDateTimestampValidator, NopTimestampValidator, SystemTimestampValidator {
    /**
     * Validates a timestamp. It is valid if it did not occur too early or too late.
     *
     * @param timestamp The timestamp to validate
     * @return True if valid, false if invalid
     */
    boolean validate(int timestamp);
}

package net.gudenau.minecraft.realip;

import net.fabricmc.example.validation.*;
import net.gudenau.minecraft.realip.validation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * A wrapper for the timestamp and signature validators.
 */
public final class ValidationHelper {
    private static final TimestampValidator TIMESTAMP_VALIDATOR = createTimestampValidator();
    private static final SignatureValidator SIGNATURE_VALIDATOR = new SignatureValidator();
    
    private static TimestampValidator createTimestampValidator() {
        return switch(Configuration.timestampValidator){
            case SystemTimestampValidator.NAME -> SystemTimestampValidator.getInstance();
            case HttpDateTimestampValidator.NAME -> HttpDateTimestampValidator.getInstance();
            case NopTimestampValidator.NAME -> NopTimestampValidator.getInstance();
            default -> {
                System.err.printf("Unknown timestamp validator \"%s\", disabling timestamp validation\n", Configuration.timestampValidator);
                yield NopTimestampValidator.getInstance();
            }
        };
    }
    
    /**
     * Validates a timestamp against the timestamp validator instance. A timestamp is valid if it did not occur too
     * early or too late from the current time.
     *
     * @param timestamp The timestamp to validate
     * @return True if valid, false if invalid
     */
    public static boolean validateTimestamp(int timestamp){
        return TIMESTAMP_VALIDATOR.validate(timestamp);
    }
    
    /**
     * Gets the max timestamp offset allowed, in seconds.
     *
     * @return The max allowed timestamp offset
     */
    public static int getMaxTimestampOffset() {
        return Configuration.maxTimestampOffset;
    }
    
    /**
     * Validates a signature of some data with the public key provided by TCP Shield.
     *
     * @param payload The data to validate
     * @param signature The signature to use
     * @return True if valid, false if invalid
     */
    public static boolean validateSignature(String payload, String signature) {
        return SIGNATURE_VALIDATOR.validate(
            payload.getBytes(StandardCharsets.UTF_8),
            Base64.getDecoder().decode(signature)
        );
    }
    
    // Runs <clinit>
    public static void init() {}
}

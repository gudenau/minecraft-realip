package net.gudenau.minecraft.realip.validation;

/**
 * A super dumb timestamp validator that doesn't check anything.
 */
public final class NopTimestampValidator implements TimestampValidator {
    public static final String NAME = "nop";
    
    private static final NopTimestampValidator INSTANCE = new NopTimestampValidator();
    
    public static TimestampValidator getInstance() {
        return INSTANCE;
    }
    
    @Override
    public boolean validate(int timestamp) {
        return true;
    }
}

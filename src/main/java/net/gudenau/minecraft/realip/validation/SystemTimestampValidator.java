package net.gudenau.minecraft.realip.validation;

import net.gudenau.minecraft.realip.ValidationHelper;

/**
 * A basic timestamp validator that only uses the system's wall time for validation.
 */
public final class SystemTimestampValidator implements TimestampValidator {
    public static final String NAME = "system";
    
    private static final TimestampValidator INSTANCE = new SystemTimestampValidator();
    
    public static TimestampValidator getInstance() {
        return INSTANCE;
    }
    
    @Override
    public boolean validate(int timestamp) {
        return Math.abs(System.currentTimeMillis() / 1000 - timestamp) <= ValidationHelper.getMaxTimestampOffset();
    }
}

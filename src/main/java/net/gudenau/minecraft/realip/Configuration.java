package net.gudenau.minecraft.realip;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * The configuration for this mod. Uses a super simple format because nothing complex is required.
 */
public final class Configuration {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("realip.cfg");
    
    static String timestampValidator = "http";
    static int maxTimestampOffset = 3;
    
    public static void load() {
        if(!Files.exists(CONFIG_PATH)){
            save();
            return;
        }
        
        var state = new Object(){
            String timestampValidator = Configuration.timestampValidator;
            int maxTimestampOffset = Configuration.maxTimestampOffset;
        };
        try(var reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)){
            reader.lines()
                .map((string)->{
                    int commentIndex = string.indexOf('#');
                    if(commentIndex == -1){
                        return string;
                    }else{
                        return string.substring(0, commentIndex);
                    }
                })
                .map(String::trim)
                .filter((string)->!string.isEmpty())
                .map((string)->string.toLowerCase(Locale.ROOT))
                .forEach((string)->{
                    var split = string.split("=", 2);
                    if(split.length != 2){
                        return;
                    }
                    switch (split[0]){
                        case "timestamp_validator" -> state.timestampValidator = split[1];
                        case "max_timestamp_offset" -> state.maxTimestampOffset = Integer.parseInt(split[1]);
                    }
                });
        }catch (IOException e){
            save();
            return;
        }
        timestampValidator = state.timestampValidator;
        maxTimestampOffset = state.maxTimestampOffset;
    }
    
    private static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try(var writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)){
                writer.write("""
                    # Allows you to change the timestamp validator used to prevent replay attacks.
                    #
                    # Default value: http
                    #
                    # Valid values:
                    #  http: Uses Alphabet's Google servers to get a better time
                    #  system: Only uses the system's time
                    #  nop: Disables validation
                    timestamp_validator=http
                    
                    # This controls the offset that is allowed when validating timestamps. If a timestamp is outside of
                    # this range the user will not be allowed to join. Keep this reasonable.
                    #
                    # This value is in seconds.
                    #
                    # Default value: 3
                    max_timestamp_offset=3
                    """);
            }
        } catch (IOException ignored) {}
    }
}

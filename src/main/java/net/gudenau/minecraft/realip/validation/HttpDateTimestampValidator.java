package net.gudenau.minecraft.realip.validation;

import net.gudenau.minecraft.realip.ValidationHelper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ForkJoinPool;

/**
 * A basic timestamp verifier that uses Alphabet's Google servers to get an offset to wall time to attempt to keep
 * clocks synchronized.
 */
public final class HttpDateTimestampValidator implements TimestampValidator {
    public static final String NAME = "http";
    
    private final Timer timer;
    private long offset;
    
    public static TimestampValidator getInstance() {
        return new HttpDateTimestampValidator();
    }
    
    private HttpDateTimestampValidator() {
        timer = new Timer("realip offset updater", true);
        // Update the offset once an hour, just in case.
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ForkJoinPool.commonPool().execute(() -> {
                    try {
                        updateOffset();
                    } catch (IOException | InterruptedException e) {
                        // Swallow when it fails to update, not the end of the world.
                        if(offset == 0) {
                            throw new RuntimeException("Could not get time from Alphabet's servers", e);
                        }
                    }
                });
            }
        }, 0, 60 /*h*/ * 60 /*m*/ * 60 /*s*/ * 1000/*ms*/);
    }
    
    /**
     * Updates the time offset based on Alphabet's Google servers.
     *
     * @throws IOException If the website could not be reached successfully
     * @throws InterruptedException If this thread was interrupted
     */
    private void updateOffset() throws IOException, InterruptedException {
        // Create this and let it get GC'ed, don't use it often enough to justify the objects floating around
        var client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL)
            // Alphabet should be super fast after all...
            .connectTimeout(Duration.ofSeconds(5))
            .build();
        
        var request = HttpRequest.newBuilder()
            .uri(URI.create("https://google.com"))
            .headers(
                "User-Agent", "tcpshield-fabric/1.0",
                "Pragma", "no-cache",
                "Cache-Control", "no-cache"
            )
            .build();
        
        long readTime = System.currentTimeMillis();
        var response = client.send(request, HttpResponse.BodyHandlers.discarding());
        var serverDate = parseDate(response.headers());
        // We only get second resolution from Alphabet, round it off
        offset = Math.round(((serverDate.getTime() - readTime) / 1000.0)) * 1000;
    }
    
    /**
     * Parses a "Date" header from a Http request's response headers.
     *
     * @param headers The headers to parse
     * @return The date
     * @throws IllegalStateException If a Date header was not present
     */
    private static Date parseDate(HttpHeaders headers) {
        return headers.firstValue("Date")
            .map((date)->{
                var format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
                try {
                    return format.parse(date);
                } catch (ParseException e) {
                    throw new IllegalStateException("Failed to parse date in header: " + date, e);
                }
            })
            .orElseThrow(()->new IllegalStateException("Could not find date in headers"));
    }
    
    @Override
    public boolean validate(int timestamp) {
        return Math.abs((System.currentTimeMillis() + offset) / 1000 - timestamp) <= ValidationHelper.getMaxTimestampOffset();
    }
}

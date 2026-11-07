package com.logpose.api.importer;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/**
 * Reads raw files from the configured data source, which is either a URL or a local directory.
 */
@Component
public class CardDataReader {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public String read(String source, String relativePath) {
        try {
            if (source.startsWith("http://") || source.startsWith("https://")) {
                URI uri = URI.create(stripTrailingSlash(source) + "/" + relativePath);
                HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(30)).GET().build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    throw new IllegalStateException("GET " + uri + " returned HTTP " + response.statusCode());
                }
                return response.body();
            }
            return Files.readString(Path.of(source, relativePath));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + relativePath + " from " + source, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while reading " + relativePath, e);
        }
    }

    private static String stripTrailingSlash(String s) {
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }
}

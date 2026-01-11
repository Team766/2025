package com.team766.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StaticFileHandlerTest {

    private HttpServer server;
    private Path tempDir;
    private HttpClient client;
    private String baseUrl;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("staticFileHandlerTest");

        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", new StaticFileHandler(tempDir.toString(), "/"));
        server.setExecutor(null);
        server.start();

        baseUrl = "http://localhost:" + server.getAddress().getPort();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() throws IOException {
        server.stop(0);
        // Recursive delete
        try (var stream = Files.walk(tempDir)) {
            stream.sorted((a, b) -> b.compareTo(a)) // Delete children first
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            // ignore
                        }
                    });
        }
    }

    @Test
    void testServeFile() throws IOException, InterruptedException {
        Path file = tempDir.resolve("index.html");
        Files.writeString(file, "<html>Hello World</html>");

        HttpRequest request =
                HttpRequest.newBuilder().uri(URI.create(baseUrl + "/index.html")).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("<html>Hello World</html>", response.body());
        assertEquals("text/html", response.headers().firstValue("Content-Type").orElse(""));
    }

    @Test
    void testContentTypeCss() throws IOException, InterruptedException {
        Path file = tempDir.resolve("style.css");
        Files.writeString(file, "body { color: red; }");

        HttpRequest request =
                HttpRequest.newBuilder().uri(URI.create(baseUrl + "/style.css")).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("text/css", response.headers().firstValue("Content-Type").orElse(""));
    }

    @Test
    void testContentTypeJs() throws IOException, InterruptedException {
        Path file = tempDir.resolve("script.js");
        Files.writeString(file, "console.log('hi');");

        HttpRequest request =
                HttpRequest.newBuilder().uri(URI.create(baseUrl + "/script.js")).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        String ct = response.headers().firstValue("Content-Type").orElse("");
        assertTrue(ct.contains("javascript"),
                "Content-Type '" + ct + "' should contain 'javascript'");
    }

    @Test
    void testNotFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/does-not-exist.txt")).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void testSubdirectory() throws IOException, InterruptedException {
        Path subDir = tempDir.resolve("subdir");
        Files.createDirectory(subDir);
        Path file = subDir.resolve("data.txt");
        Files.writeString(file, "some data");

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/subdir/data.txt"))
                .GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("some data", response.body());
    }

    @Test
    void testPathTraversal() throws IOException, InterruptedException {
        // Attempt to access parent of tempDir.
        // This relies on StaticFileHandler traversing logical paths, or filesystem checks.
        // StaticFileHandler code explicitly checks: if (relativePath.contains("..")) return 403;

        HttpRequest request =
                HttpRequest.newBuilder().uri(URI.create(baseUrl + "/../secret.txt")).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(403, response.statusCode());
    }
}

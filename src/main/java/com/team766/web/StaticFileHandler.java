package com.team766.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;

public class StaticFileHandler implements HttpHandler {
    private final String filesystemRoot;
    private final String urlPrefix;

    public StaticFileHandler(String filesystemRoot, String urlPrefix) {
        this.filesystemRoot = filesystemRoot;
        this.urlPrefix = urlPrefix;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if (!"GET".equalsIgnoreCase(method)) {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            return;
        }

        URI uri = exchange.getRequestURI();
        String path = uri.getPath();

        if (!path.startsWith(urlPrefix)) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        String relativePath = path.substring(urlPrefix.length());

        // Sanitize path to prevent traversal
        if (relativePath.contains("..")) {
            exchange.sendResponseHeaders(403, -1); // Forbidden
            return;
        }

        File file = new File(filesystemRoot, relativePath);

        if (!file.exists() || !file.isFile()) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        // Determine content type (basic implementation)
        String contentType = "application/octet-stream";
        try {
            String probedType = Files.probeContentType(file.toPath());
            if (probedType != null) {
                contentType = probedType;
            } else if (file.getName().endsWith(".js")) {
                contentType = "application/javascript";
            } else if (file.getName().endsWith(".css")) {
                contentType = "text/css";
            } else if (file.getName().endsWith(".html")) {
                contentType = "text/html";
            }
        } catch (IOException e) {
            // ignore
        }

        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, file.length());

        try (OutputStream os = exchange.getResponseBody();
                FileInputStream fs = new FileInputStream(file)) {
            final byte[] buffer = new byte[0x10000];
            int count = 0;
            while ((count = fs.read(buffer)) >= 0) {
                os.write(buffer, 0, count);
            }
        }
    }
}

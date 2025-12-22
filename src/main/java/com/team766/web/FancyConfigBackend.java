package com.team766.web;

import com.team766.config.ConfigFileReader;
import com.team766.config.ConfigValueParseException;
import java.util.ArrayList;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class FancyConfigBackend implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        URI uri = exchange.getRequestURI();
        String path = uri.getPath();
        
        if ("GET".equalsIgnoreCase(method) && path.equals("/fancy-config-backend/load-json")) {
            String json = ConfigFileReader.getInstance().getJsonString();
            byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, jsonBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(jsonBytes);
            }
        } else if ("POST".equalsIgnoreCase(method) && path.equals("/fancy-config-backend/save-json")) {
            String newConfig = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            ArrayList<String> errors = new ArrayList<String>();
            try {
                ConfigFileReader.getInstance().reloadFromJson(newConfig);
            } catch (ConfigValueParseException ex) {
                errors.add("Invalid JSON: " + ex.getMessage());
            } catch (Exception ex) {
                errors.add("Unexpected Error: " + ex.toString());
            }

            if (errors.isEmpty()) {
                try {
                    ConfigFileReader.getInstance().saveFile(newConfig);
                } catch (IOException ex) {
                    errors.add("IO Exception: " + ex.getMessage());
                }
            }

            if (errors.isEmpty()) {
                String response = "Config saved successfully!";
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
            } else {
                String response = String.join("\n", errors);
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
            }
        } else {
            exchange.sendResponseHeaders(404, -1);
        }
    }
}
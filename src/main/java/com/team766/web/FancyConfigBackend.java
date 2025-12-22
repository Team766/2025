package com.team766.web;

import com.team766.config.ConfigFileReader;
import java.util.ArrayList;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
            byte[] jsonBytes = json.getBytes();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, jsonBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(jsonBytes);
            }
        }

        if ("POST".equalsIgnoreCase(method) && path.equals("/fancy-config-backend/save-json")) {
            InputStream inp = exchange.getRequestBody();
            StringBuilder response_builder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inp, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response_builder.append(line);
                }
            }
            String newConfig = response_builder.toString();
            ArrayList<String> errors = new ArrayList<String>();
            try {
                ConfigFileReader.getInstance().reloadFromJson(newConfig);
            } catch (Exception ex) {
                errors.add(ex.toString());
            }
            if (errors.isEmpty()) {
                ConfigFileReader.getInstance().saveFile(newConfig);
                String response = "Config saved successfully!";
                byte[] responseBytes = response.getBytes();
                exchange.sendResponseHeaders(200, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
            } else {
                String response = "ERROR:" + errors.get(0);
                byte[] responseBytes = response.getBytes();
                exchange.sendResponseHeaders(200, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
            }
        }
    }
}
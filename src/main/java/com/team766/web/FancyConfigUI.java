package com.team766.web;

import com.team766.config.ConfigFileReader;
import com.team766.config.ConfigValueParseException;
import com.team766.web.WebServer.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class FancyConfigUI implements Handler {
    @Override
    public String endpoint() {
        return "/fancy-config";
    }

    @Override
    public String handle(final Map<String, Object> params) {
        return "<meta http-equiv=\"refresh\" content=\"0; url=/html/editor.html\">";
    }

    @Override
    public String title() {
        return "Fancy Config Editor";
    }

    public static class LoadJson implements ApiHandler {
        public String endpoint() {
            return "/fancy-config-backend/load-json";
        }

        public ApiResponse handle(ApiRequest request) {
            if (request.kind != ApiRequest.Kind.GET) {
                return new ApiResponse(405);
            }
            String json = ConfigFileReader.getInstance().getJsonString();
            return new ApiResponse(json, 200, ContentType.APPLICATION_JSON);
        }
    }

    public static class SaveJson implements ApiHandler {
        public String endpoint() {
            return "/fancy-config-backend/save-json";
        }

        public ApiResponse handle(ApiRequest request) {
            if (request.kind != ApiRequest.Kind.POST) {
                return new ApiResponse(405);
            }
            ArrayList<String> errors = new ArrayList<String>();
            boolean toDisk = false;

            try {
                ConfigFileReader.getInstance().reloadFromJson(request.body);
            } catch (ConfigValueParseException ex) {
                errors.add("Invalid JSON: " + ex.getMessage());
            } catch (Exception ex) {
                errors.add("Unexpected Error: " + ex.toString());
            }

            if (errors.isEmpty() && request.params != null) {
                boolean isPersistent = "true".equals(request.params.get("persistent"));
                if (isPersistent) {
                    try {
                        ConfigFileReader.getInstance().saveFile(request.body);
                        toDisk = true;
                    } catch (IOException ex) {
                        errors.add("IO Exception: " + ex.getMessage());
                    }
                }
            }

            if (errors.isEmpty()) {
                String response =
                        "Config generation "
                                + ConfigFileReader.getInstance().getGeneration()
                                + " saved successfully";
                if (toDisk) {
                    response = response + " to disk";
                }
                response = response + "!";
                return new ApiResponse(response, 200);
            } else {
                String response = String.join("\n", errors);
                return new ApiResponse(response, 400);
            }
        }
    }
}

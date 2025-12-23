package com.team766.web;

import java.util.Map;

public class FancyConfigUI implements WebServer.Handler {
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
}

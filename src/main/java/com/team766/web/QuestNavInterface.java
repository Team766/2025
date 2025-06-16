package com.team766.web;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;
import com.team766.logging.Category;

public class QuestNavInterface implements WebServer.Handler {
    private static final String ENDPOINT = "/QuestNav";
    private static final String TITLE = "Team 766: QuestNav Interface";
    @Override
    public String endpoint() {
        return ENDPOINT;
    }

    @Override
    public String title() {
        return TITLE;
    }

    @Override
    public String handle(Map<String, Object> params) {
        String page = "<h1> QuestNav Interface </h1>";
        page += "<form action=\"" + ENDPOINT + "\">";
        page += "<p>";
        page += "<label for=\"rotation\">Rotation degrees:</label><br>";
        page += "<input type=\"number\" id=\"rotation\" name=\"rotation\"><br>";

        page += "<label for=\"x\">ROBOT Translation X from origin:</label><br>";
        page += "<input type=\"number\" id=\"x\" name=\"x\"><br>";

        page += "<label for=\"y\">ROBOT Translation Y from origin:</label><br>";
        page += "<input type=\"number\" id=\"y\" name=\"y\"><br>";

        page += "<label for=\"z\">ROBOT Translation Z from origin:</label><br>";
        page += "<input type=\"number\" id=\"z\" name=\"z\"><br>";

        page += "<input type=\"submit\" value=\"Publish Values\">";
        page += "</p></form>";
                
        return page;
    }
    
}

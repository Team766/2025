package com.team766.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 * Creates an HTTP Server on the robot that can change the
 * settings of it.  It is initially just used to change robot
 * values.  Hopefully this will be later updated to support
 * graphing and even small changes in the robot's actual code.
 *
 * Can be reached at:
 * 	roboRio-766.local:5800/values
 */

public class WebServer implements Closeable {

    public interface Handler {
        String endpoint();

        default boolean showInMenu() {
            return true;
        }

        String title();

        String handle(Map<String, Object> params);
    }

    private HttpServer server;
    private LinkedHashMap<String, Handler> pages = new LinkedHashMap<String, Handler>();
    private String staticUrlPrefix;
    private HttpHandler staticFileHandler;

    public WebServer() {
        addHandler(
                new Handler() {
                    @Override
                    public String endpoint() {
                        return "/";
                    }

                    @Override
                    public String title() {
                        return "Menu";
                    }

                    @Override
                    public boolean showInMenu() {
                        return false;
                    }

                    @Override
                    public String handle(final Map<String, Object> params) {
                        return "";
                    }
                });
    }

    @Override
    public void close() {
        server.stop(5);
    }

    public void addHandler(final Handler handler) {
        pages.put(handler.endpoint(), handler);
    }

    public void setStaticFileHandler(String urlPrefix, String filesystemRoot) {
        if (!urlPrefix.startsWith("/")) {
            urlPrefix = "/" + urlPrefix;
        }
        this.staticUrlPrefix = urlPrefix;
        this.staticFileHandler = new StaticFileHandler(filesystemRoot, urlPrefix);
    }

    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(5800), 0);
            setupApiHandlers();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (Map.Entry<String, Handler> page : pages.entrySet()) {
            HttpHandler httpHandler =
                    new HttpHandler() {
                        @Override
                        public void handle(final HttpExchange exchange) throws IOException {
                            Map<String, Object> params = parseParams(exchange);
                            String response = "<!DOCTYPE html><html><body>";
                            response += buildPageHeader();
                            try {
                                response += page.getValue().handle(params);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                throw ex;
                            }
                            response += "</body></html>";
                            exchange.getResponseHeaders().set("Content-Type", "text/html");
                            exchange.sendResponseHeaders(200, response.getBytes().length);
                            try (OutputStream os = exchange.getResponseBody()) {
                                os.write(response.getBytes());
                            }
                        }
                    };
            server.createContext(page.getKey(), httpHandler);
        }

        if (staticFileHandler != null) {
            server.createContext(staticUrlPrefix, staticFileHandler);
        }

        addLineNumbersSvgHandler();
        server.start();
    }

    protected String buildPageHeader() {
        String result = "";
        result += "<style>\n";
        result += "textarea {\n";
        result += "  background: url(/line_numbers.svg);\n";
        result += "  background-attachment: local;\n";
        result += "  background-repeat: no-repeat;\n";
        result += "  padding-left: 45px;\n";
        result += "  border-color:#ccc;\n";
        result += "  line-height: 1.15em;\n";
        result += "  font-size: 14px;\n";
        result += "  font-family: monospace;\n";
        result += "}\n";
        result += "</style>\n";
        result += "<p>\n";
        for (Map.Entry<String, Handler> page : pages.entrySet()) {
            if (page.getValue().showInMenu()) {
                result +=
                        String.format(
                                "<a href=\"%s\">%s</a><br>\n",
                                page.getKey(), page.getValue().title());
            }
        }
        result += "</p>\n";
        return result;
    }

    private void addLineNumbersSvgHandler() {
        HttpHandler httpHandler =
                new HttpHandler() {
                    @Override
                    public void handle(final HttpExchange exchange) throws IOException {
                        String response = "<?xml version=\"1.0\"?>\n";
                        response +=
                                "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n";
                        response += "<svg xmlns=\"http://www.w3.org/2000/svg\">\n";
                        response +=
                                "<text x=\"0\" y=\"0\" text-anchor=\"end\" font-family=\"monospace\" font-size=\"14px\">\n";
                        for (int i = 1; i < 1000; ++i) {
                            response += "<tspan x=\"45px\" dy=\"1.143em\">" + i + ".</tspan>\n";
                        }
                        response += "</text>\n";
                        response += "</svg>";
                        exchange.getResponseHeaders().set("Content-Type", "image/svg+xml");
                        exchange.sendResponseHeaders(200, response.getBytes().length);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(response.getBytes());
                        }
                    }
                };
        server.createContext("/line_numbers.svg", httpHandler);
    }

    public Map<String, Object> parseParams(final HttpExchange exchange) throws IOException {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parseGetParameters(exchange, parameters);
        parsePostParameters(exchange, parameters);
        return parameters;
    }

    private void parseGetParameters(
            final HttpExchange exchange, final Map<String, Object> parameters)
            throws UnsupportedEncodingException {
        URI requestedUri = exchange.getRequestURI();
        String query = requestedUri.getRawQuery();
        parseQuery(query, parameters);
    }

    private void parsePostParameters(
            final HttpExchange exchange, final Map<String, Object> parameters) throws IOException {

        if ("post".equalsIgnoreCase(exchange.getRequestMethod())) {
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String query = br.readLine();
            parseQuery(query, parameters);
        }
    }

    private void parseQuery(final String query, final Map<String, Object> parameters)
            throws UnsupportedEncodingException {

        if (query != null) {
            String[] pairs = query.split("[&]");

            for (String pair : pairs) {
                String[] param = pair.split("[=]");

                String key = null;
                String value = null;
                if (param.length > 0) {
                    key = URLDecoder.decode(param[0], System.getProperty("file.encoding"));
                }

                if (param.length > 1) {
                    value = URLDecoder.decode(param[1], System.getProperty("file.encoding"));
                }

                if (parameters.containsKey(key)) {
                    Object obj = parameters.get(key);
                    if (obj instanceof List<?>) {
                        @SuppressWarnings("unchecked")
                        List<String> values = (List<String>) obj;

                        values.add(value);
                    } else if (obj instanceof String str) {
                        List<String> values = new ArrayList<String>();
                        values.add(str);
                        values.add(value);
                        parameters.put(key, values);
                    }
                } else {
                    parameters.put(key, value);
                }
            }
        }
    }

    public interface ApiHandler {
        String endpoint();
        ApiResponse handle(ApiRequest request);
    }

    public static class ApiRequest {
        public enum Kind {
            GET, POST, PUT, DELETE, PATCH
        }

        public final Kind kind;
        public final Map<String, Object> params;
        public final String body;

        public ApiRequest(Kind kind, Map<String, Object> params, String body) {
            this.kind = kind;
            this.params = params;
            this.body = body;
        }
    }

    public static class ApiResponse {
        public final String content;
        public final int statusCode;
        public final String contentType;

        public ApiResponse(int statusCode) {
            this("", statusCode, "text/plain");
        }

        public ApiResponse(String content, int statusCode) {
            this(content, statusCode, "text/plain");
        }

        public ApiResponse(String content, int statusCode, String contentType) {
            this.content = content;
            this.statusCode = statusCode;
            this.contentType = contentType;
        }

        public ApiResponse(String content, int statusCode, ContentType contentType) {
            this.content = content;
            this.statusCode = statusCode;
            this.contentType = contentType.getValue();
        }
    }

    public enum ContentType {
        TEXT_PLAIN("text/plain"),
        APPLICATION_JSON("application/json"),
        TEXT_HTML("text/html"),
        APPLICATION_XML("application/xml"),
        IMAGE_JPEG("image/jpeg"),
        IMAGE_PNG("image/png"),
        IMAGE_SVG("image/svg+xml");

        private final String value;

        ContentType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private List<ApiHandler> apiHandlers = new ArrayList<>();

    public void addApiHandler(ApiHandler handler) {
        apiHandlers.add(handler);
    }

    // Note: This uses reflection to enumerate endpoints, which I believe is convenient and reduces boilerplate. However, I'm also happy to implement a more explicit version
    public void addApiHandlers(Object container) {
        for (Class<?> innerClass : container.getClass().getDeclaredClasses()) {
            if (ApiHandler.class.isAssignableFrom(innerClass)) {
                try {
                    ApiHandler handler = (ApiHandler) innerClass.getDeclaredConstructor().newInstance();
                    apiHandlers.add(handler);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setupApiHandlers() {
        for (ApiHandler handler : apiHandlers) {
            String endpoint = handler.endpoint();
            if (!endpoint.startsWith("/")) {
                endpoint = "/" + endpoint;
            }

            HttpHandler httpHandler = new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    try {
                        String method = exchange.getRequestMethod().toUpperCase();
                        ApiRequest.Kind kind = ApiRequest.Kind.valueOf(method);
                        
                        Map<String, Object> params = new HashMap<>();
                        parseGetParameters(exchange, params);
                        
                        String body = "";
                        if (kind == ApiRequest.Kind.POST || kind == ApiRequest.Kind.PUT) {
                            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                            BufferedReader br = new BufferedReader(isr);
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = br.readLine()) != null) {
                                sb.append(line);
                            }
                            body = sb.toString();
                        }

                        ApiRequest request = new ApiRequest(kind, params, body);
                        ApiResponse response = handler.handle(request);

                        exchange.getResponseHeaders().set("Content-Type", response.contentType);
                        byte[] bytes = response.content.getBytes();
                        exchange.sendResponseHeaders(response.statusCode, bytes.length);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(bytes);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        exchange.sendResponseHeaders(500, 0);
                        exchange.getResponseBody().close();
                    }
                }
            };

            server.createContext(endpoint, httpHandler);
        }
    }
}

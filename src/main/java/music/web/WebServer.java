package music.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import music.audio.effects.EffectType;

public class WebServer {

    private final WebController controller;
    private final HttpServer server;

    public WebServer(WebController controller, int port) throws IOException {
        this.controller = controller;
        this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/api/", this::api);
        server.createContext("/", this::page);
        server.setExecutor(Executors.newSingleThreadExecutor());
    }

    public void start() {
        server.start();
    }

    public URI address() {
        return URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/");
    }

    public void stop() {
        server.stop(0);
    }

    private void page(HttpExchange exchange) throws IOException {
        try (InputStream in = WebServer.class.getResourceAsStream("/web/index.html")) {
            byte[] body = in.readAllBytes();
            send(exchange, 200, "text/html; charset=utf-8", body);
        }
    }

    private void api(HttpExchange exchange) throws IOException {
        String action = exchange.getRequestURI().getPath().substring("/api/".length());
        Map<String, String> query = query(exchange.getRequestURI().getRawQuery());
        try {
            switch (action) {
                case "state" -> {
                }
                case "play" -> controller.play(query.getOrDefault("id", ""));
                case "playpause" -> controller.playPause();
                case "next" -> controller.next();
                case "prev" -> controller.previous();
                case "effect" -> controller.toggleEffect(EffectType.valueOf(query.getOrDefault("name", "").toUpperCase()));
                case "order" -> controller.nextOrder();
                case "undo" -> controller.undo();
                default -> throw new IllegalArgumentException("unknown action " + action);
            }
            send(exchange, 200, "application/json", controller.stateJson().getBytes(StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            send(exchange, 400, "application/json",
                    ("{\"error\":" + Json.text(String.valueOf(e.getMessage())) + "}").getBytes(StandardCharsets.UTF_8));
        }
    }

    private static Map<String, String> query(String raw) {
        Map<String, String> result = new HashMap<>();
        if (raw == null) {
            return result;
        }
        for (String pair : raw.split("&")) {
            String[] parts = pair.split("=", 2);
            result.put(parts[0], parts.length > 1 ? java.net.URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "");
        }
        return result;
    }

    private static void send(HttpExchange exchange, int status, String type, byte[] body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", type);
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }
}

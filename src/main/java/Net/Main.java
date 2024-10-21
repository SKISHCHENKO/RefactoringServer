package Net;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class Main {

    final static String GET = "GET";
    final static String POST = "POST";

    public static void main(String[] args) {
        final var server = new Server(9999, 64);

        // Регистрация хендлеров
        server.addHandler(GET, "/messages", Main::handleGetMessages);
        server.addHandler(POST, "/messages", Main::handlePostMessages);
        server.addHandler(GET, "/error", Main::handleError);

        server.listen();
    }

    // Метод для обработки GET /messages
    private static void handleGetMessages(Request request, OutputStream out) throws IOException {
        String responseMessage = createHtmlResponse("Method: GET", "Path: /messages", "String query: " + request.getPath());
        sendResponse(out, 200, "OK", responseMessage);
    }

    // Метод для обработки POST /messages
    private static void handlePostMessages(Request request, OutputStream out) throws IOException {
        String responseMessage = createHtmlResponse("Method: POST", "Path: /messages", "String query: " + request.getPath());
        sendResponse(out, 200, "OK", responseMessage);
    }

    // Метод для обработки GET /error
    private static void handleError(Request request, OutputStream out) throws IOException {
        String responseMessage = createHtmlResponse("Method: GET", "Path: /error", "Message: ERROR");
        sendResponse(out, 500, "Internal Server Error", responseMessage);
    }

    // Вспомогательный метод для создания HTML-ответа
    private static String createHtmlResponse(String... lines) {
        StringBuilder response = new StringBuilder("<html><body>");
        for (String line : lines) {
            response.append("<h1>").append(line).append("</h1><br>");
        }
        response.append("</body></html>");
        return response.toString();
    }

    // Вспомогательный метод для отправки ответа
    private static void sendResponse(OutputStream out, int statusCode, String statusMessage, String responseMessage) throws IOException {
        String statusLine = "HTTP/1.1 " + statusCode + " " + statusMessage + "\r\n";
        String headers = "Content-Length: " + responseMessage.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                "Content-Type: text/html\r\n" +
                "Connection: close\r\n\r\n";
        out.write(statusLine.getBytes());
        out.write(headers.getBytes());
        out.write(responseMessage.getBytes(StandardCharsets.UTF_8));
        out.flush();
    }
}
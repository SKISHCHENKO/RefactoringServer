package Net;

import java.io.BufferedOutputStream;
import java.io.IOException;

public class Response {
    public static void sendResponse(BufferedOutputStream out, int statusCode, String statusMessage, String body) throws IOException {
        String response = String.format(
                "HTTP/1.1 %d %s\r\n" +
                        "Content-Length: %d\r\n" +
                        "Connection: close\r\n" +
                        "\r\n%s",
                statusCode, statusMessage, body.length(), body
        );
        out.write(response.getBytes());
        out.flush();
    }

    public static void badRequestError(BufferedOutputStream out) throws IOException {
        sendResponse(out, 400, "Bad Request", "<html><body><h1>400 Bad Request</h1></body></html>");
    }

    public static void methodNotAllowed(BufferedOutputStream out) throws IOException {
        sendResponse(out, 405, "Method Not Allowed", "<html><body><h1>405 Method Not Allowed</h1></body></html>");
    }

    public static void notFound(BufferedOutputStream out) throws IOException {
        sendResponse(out, 404, "Not Found", "<html><body><h1>404 Not Found</h1></body></html>");
    }

    public static void internalServerError(BufferedOutputStream out) throws IOException {
        sendResponse(out, 500, "Internal Server Error", "<html><body><h1>500 Internal Server Error</h1></body></html>");
    }

    // Добавление ответа с кастомным статусом
    public static void customError(BufferedOutputStream out, int statusCode, String statusMessage) throws IOException {
        String body = String.format("<html><body><h1>%d %s</h1></body></html>", statusCode, statusMessage);
        sendResponse(out, statusCode, statusMessage, body);
    }
}

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
        sendResponse(out, 400, "Bad Request", "");
    }

    public static void methodNotAllowed(BufferedOutputStream out) throws IOException {
        sendResponse(out, 405, "Method Not Allowed", "");
    }

    public static void notFound(BufferedOutputStream out) throws IOException {
        sendResponse(out, 404, "Not Found", "");
    }

    public static void internalServerError(BufferedOutputStream out) throws IOException {
        sendResponse(out, 500, "Internal Server Error", "");
    }
}

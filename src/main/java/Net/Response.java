package Net;

import java.io.BufferedOutputStream;
import java.io.IOException;

public class Response {
    public static void badRequestError(BufferedOutputStream out) throws IOException {
        out.write((
                """
                        HTTP/1.1 400 Bad Request\r
                        Content-Length: 0\r
                        Connection: close\r
                        \r
                        """
        ).getBytes());
        out.flush();
    }

    public static void methodNotAllowed(BufferedOutputStream out) throws IOException {
        out.write((
                """
                        HTTP/1.1 405 Method Not Allowed\r
                        Content-Length: 0\r
                        Connection: close\r
                        \r
                        """
        ).getBytes());
        out.flush();
    }

    public static void notFound(BufferedOutputStream out) throws IOException {
        out.write((
                """
                        HTTP/1.1 404 Not Found\r
                        Content-Length: 0\r
                        Connection: close\r
                        \r
                        """
        ).getBytes());
        out.flush();
    }

    public static void internalServerError(BufferedOutputStream out) throws IOException {
        out.write((
                """
                        HTTP/1.1 500 Internal Server Error\r
                        Content-Length: 0\r
                        Connection: close\r
                        \r
                        """
        ).getBytes());
        out.flush();
    }
}
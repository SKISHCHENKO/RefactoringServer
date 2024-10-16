package Net;


import java.nio.charset.StandardCharsets;

public class Main {

    final static String GET = "GET";
    final static String POST = "POST";

    public static void main(String[] args) {
        final var server = new Server(9999, 64);

        // добавление хендлеров (обработчиков)
        server.addHandler(GET, "/messages", (request, out) -> {
            String responseMessage = "<html><body><h1> New Handler: <br>" +
                    "Method: GET <br>" +
                    "Path: /messages <br>" +
                    "String query: "+ request.getPath() + "<br>";

            out.write((
                    "HTTP/1.1 405 Method Not Allowed\r\n" +
                            "Content-Length: " + responseMessage.length() + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());

            out.write(responseMessage.getBytes(StandardCharsets.UTF_8));
            out.flush();
        });

        server.addHandler(GET, "/error", (request, out) -> {
            String responseMessage = "<html><body><h1>New Handler: <br>" +
                    "Method: GET <br>" +
                    "Path: /messages <br>" +
                    "String query: "+ request.getPath() + "<br>" +
                    "Message: ERROR</h1></body></html>";
            out.write((
                    "HTTP/1.1 405 Method Not Allowed\r\n" +
                            "Content-Length: " + responseMessage.length() + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());

            out.write(responseMessage.getBytes(StandardCharsets.UTF_8));
            out.flush();
        });


        server.addHandler(POST, "/messages", (request, out) -> {
            String responseMessage = "<html><body><h1> New Handler: <br>" +
                    "Method: POST <br>" +
                    "Path: /messages <br>" +
                    "String query: "+ request.getPath() + "</h1></body></html>";
            out.write((
                    "HTTP/1.1 405 Method Not Allowed\r\n" +
                            "Content-Length: " + responseMessage.length() + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());

            out.write(responseMessage.getBytes(StandardCharsets.UTF_8));
            out.flush();
        });

        server.listen();
    }
}
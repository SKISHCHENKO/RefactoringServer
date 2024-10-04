package Net;


import java.nio.charset.StandardCharsets;

public class Main {

    final static String GET = "GET";
    final static String POST = "POST";

    public static void main(String[] args) {
        final var server = new Server(9999, 64);

        // добавление хендлеров (обработчиков)
        server.addHandler(GET, "/messages", (request, out) -> {
            String responseMessage = "New Handler: Method: GET, Path: /messages";

            Request response = new Request(GET, "/messages", "200 OK", "text/plain", responseMessage.getBytes().length, responseMessage, request.getVersion());

            out.write(response.createRequest().getBytes());
            out.write(responseMessage.getBytes(StandardCharsets.UTF_8));
            out.flush();
        });

        server.addHandler(GET, "/error", (request, out) -> {
            String responseMessage = "New Handler: Method: GET, Path: /error" + "\r\n" +
                    "Message: ERROR";

            Request response = new Request(GET, "/messages", "200 OK", "text/plain", responseMessage.getBytes().length, responseMessage, request.getVersion());

            out.write(response.createRequest().getBytes());
            out.write(responseMessage.getBytes(StandardCharsets.UTF_8));
            out.flush();
        });


        server.addHandler(POST, "/messages", (Handler) (request, out) -> {
            String responseMessage = "New Handler: Method: POST, Path: /messages";

            Request response = new Request(POST, "/messages", "200 OK", "text/plain", responseMessage.getBytes().length, responseMessage, request.getVersion());

            out.write(response.createRequest().getBytes());
            out.write(responseMessage.getBytes(StandardCharsets.UTF_8));
            out.flush();
        });

        server.listen();

    }
}
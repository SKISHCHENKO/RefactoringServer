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
                    "String query: <br>"+ request.getQuery() + "<br>" +
                    "Search login in query: "+ request.getQueryParam("login")+"</h1></body></html>";

            Request response = new Request(GET, "/messages", "200 OK", request.getVersion());
            response.setContent(responseMessage);
            out.write(response.createRequest().getBytes());
            out.write(responseMessage.getBytes(StandardCharsets.UTF_8));
            out.flush();
        });

        server.addHandler(GET, "/error", (request, out) -> {
            String responseMessage = "<html><body><h1>New Handler: <br>" +
                    "Method: GET <br>" +
                    "Path: /messages <br>" +
                    "String query: "+ request.getQuery() + "<br>" +
                    "Message: ERROR</h1></body></html>";

            Request response = new Request(GET, "/error", "200 OK", request.getVersion());
            response.setContent(responseMessage);
            out.write(response.createRequest().getBytes());
            out.write(responseMessage.getBytes(StandardCharsets.UTF_8));
            out.flush();
        });


        server.addHandler(POST, "/messages", (Handler) (request, out) -> {
            String responseMessage = "<html><body><h1> New Handler: <br>" +
                    "Method: POST <br>" +
                    "Path: /messages <br>" +
                    "String query: "+ request.getQuery() + "</h1></body></html>";

            Request response = new Request(POST, "/messages", "200 OK", request.getVersion());
            response.setContent(responseMessage);
            out.write(response.createRequest().getBytes());
            out.write(responseMessage.getBytes(StandardCharsets.UTF_8));
            out.flush();
        });

        server.listen();

    }
}
package Net;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    final private int PORT;
    final private ExecutorService threadPool;
    final static List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    final static Map<String, Map<String, Handler>> handlers = new ConcurrentHashMap<>();
    final static String GET = "GET";
    final static String POST = "POST";

    public Server(int port, int numberThreads) {
        this.PORT = port;
        this.threadPool = Executors.newFixedThreadPool(numberThreads);
    }

    public void listen() {
        try (final var serverSocket = new ServerSocket(PORT)) {
            while (true) {
                try {
                    final var socket = serverSocket.accept();
                    threadPool.submit(() -> defaultHandler(socket, validPaths));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void defaultHandler(Socket socket, List<String> validPaths) {
        try (final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             final var out = new BufferedOutputStream(socket.getOutputStream())) {


            final var requestLine = in.readLine();
            System.out.println("Request: " + requestLine);

            if (requestLine == null) {
                return;
            }

            final var parts = requestLine.split(" ");
            if (parts.length != 3) {
                sendResponse(out, new Request("400 Bad Request"));
                return;
            }

            final var method = parts[0];
            final var path = parts[1];
            final var version = parts[2];

            // Обработка через динамические хендлеры
            if (handlers.containsKey(method) && handlers.get(method).containsKey(path)) {
                if (!new Request("").allowedMethods.contains(method)) {
                    // Если метод не разрешен, отправляем ответ с ошибкой 405 Method Not Allowed
                    sendResponse(out, new Request("405 Method Not Allowed"));
                    return;
                }
                // Получаем и обрабатываем запрос с допустимым методом
                Handler handler = handlers.get(method).get(path);
                handler.handle(new Request(method, path, "200 OK", "text/html", version), out);
                return;
            }

            // Обработка статических файлов
            final var filePath = Path.of(".", "public", path);
            if (!Files.exists(filePath)) {
                sendResponse(out, new Request("404 Not Found"));
                return;
            }

            final var mimeType = Files.probeContentType(filePath);
            if (mimeType == null) {
                sendResponse(out, new Request("415 Unsupported Media Type"));
                return;
            }

            if (path.equals("/classic.html")) {
                final var template = Files.readString(filePath);
                final var content = template.replace("{time}", LocalDateTime.now().toString());

                // Создаем объект Request с телом контента
                Request request = new Request(method, path, "200 OK", mimeType, content.getBytes().length, content, version);

                out.write(request.createRequest().getBytes());
                out.write(content.getBytes());
                out.flush();
            } else {
                final var contentLength = (int) Files.size(filePath);

                // Создаем объект Request без тела контента, т.к. будем копировать файл напрямую
                Request request = new Request(method, path, "200 OK", mimeType, contentLength, version);

                out.write(request.createRequest().getBytes());
                Files.copy(filePath, out);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendResponse(BufferedOutputStream out, Request request) throws IOException {
        // В случае ошибки добавляем сообщение об ошибке как HTML-страницу
        if (request.getHeader().startsWith("4") || request.getHeader().startsWith("5")) {
            String errorMessage = "<html><body><h1>" + request.getHeader() + "</h1></body></html>";
            byte[] errorContent = errorMessage.getBytes();

            String errorResponse = request.getVersion() + " " + request.getHeader() + "\r\n" +
                    "Content-Type: text/html" + "\r\n" +
                    "Content-Length: " + errorContent.length + "\r\n" +
                    "Connection: close" + "\r\n" +
                    "\r\n";
            out.write(errorResponse.getBytes());
            out.write(errorContent);
        }
        else{
            String response = request.createRequest();
            out.write(response.getBytes());
            if (request.getContentBody() != null) {
                out.write(request.getContentBody().getBytes());
            }
        }
        out.flush();
    }
    public void addHandler(String method, String path, Handler handler) {
        handlers.computeIfAbsent(method, k -> new ConcurrentHashMap<>()).put(path, handler);
    }
}

package Net;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
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
        try (final var in = new BufferedInputStream(socket.getInputStream());
             final var out = new BufferedOutputStream(socket.getOutputStream())) {

            // лимит на request line + заголовки
            final var limit = 4096;

            in.mark(limit);
            final var buffer = new byte[limit];
            final var read = in.read(buffer);

            // ищем request line
            final var requestLineDelimiter = new byte[]{'\r', '\n'};
            final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);

            if (requestLineEnd == -1) {
                sendResponse(out, new Request("400 Bad Request"));
                return;
            }

            final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
            if (requestLine.length != 3) {
                sendResponse(out, new Request("400 Bad Request"));
                return;
            }

            final var method = requestLine[0];
            final var query = requestLine[1];
            final var version = requestLine[2];

            Request request = new Request(method, query,"200 OK", version);
            final var path = request.getPath();

            System.out.println("method = " + method);
            System.out.println("query = " + query);
            System.out.println("path = " + path);
            System.out.println("version = " + version);

            // Обработка через динамические хендлеры
            if (handlers.containsKey(method) && handlers.get(method).containsKey(path)) {
                if (!new Request("").allowedMethods.contains(method)) {
                    // Если метод не разрешен, отправляем ответ с ошибкой 405 Method Not Allowed
                    sendResponse(out, new Request("405 Method Not Allowed"));
                    return;
                }
                // Получаем и обрабатываем запрос с допустимым методом
                Handler handler = handlers.get(method).get(path);
                handler.handle(request, out);
                return;
            }
            // Если запрос GET и параметры отсутствуют
            if (query.length() < 2 && method.equals(GET)) {
                request.setContent("<html><body><h1>String query is empty!</h1></body></html>");
                sendResponse(out, request);
                return;
            }
            // Обработка POST-запроса
            if (method.equals(POST)) {
                // Определяем конец заголовков
                final var headersEnd = indexOf(buffer, new byte[]{'\r', '\n', '\r', '\n'}, requestLineEnd, read);
                if (headersEnd == -1) {
                    sendResponse(out, new Request("400 Bad Request"));
                    return;
                }

                // Чтение тела POST-запроса
                final var contentLengthHeader = "Content-Length: ";
                String headers = new String(Arrays.copyOfRange(buffer, requestLineEnd + 2, headersEnd));
                int contentLength = 0;
                if (headers.contains(contentLengthHeader)) {
                    int contentLengthIndex = headers.indexOf(contentLengthHeader) + contentLengthHeader.length();
                    int contentLengthEnd = headers.indexOf('\r', contentLengthIndex);
                    contentLength = Integer.parseInt(headers.substring(contentLengthIndex, contentLengthEnd));
                }

                if (contentLength > 0) {
                    byte[] body = new byte[contentLength];
                    in.read(body);

                    // Парсинг тела запроса
                    String postBody = new String(body, StandardCharsets.UTF_8);
                    request.parsePostParams(postBody);

                    // Ответ с POST параметрами
                    request.setContent("<html><body><h2>" + request.getPostParams() + "</h2></body></html>");
                    sendResponse(out, request);
                    return;
                }
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
            request.setMimeType(Files.probeContentType(filePath));
            // Обработка и вывод на html страницу параметров query
            if (query.contains("?")) {
                request.setContent("<html><body><h2>" + request.getQueryParams() + "</h2></body></html>");
                sendResponse(out, request);
                return;
            }

            if (path.equals("/classic.html")) {
                final var template = Files.readString(filePath);
                final var content = template.replace("{time}", LocalDateTime.now().toString());
                request.setContent(content);

                out.write(request.createRequest().getBytes());
                out.write(content.getBytes());
                out.flush();
            } else {
                final var contentLength = (int) Files.size(filePath);
                request.setContentLength(contentLength);
                out.write(request.createRequest().getBytes());
                Files.copy(filePath, out);
                out.flush();
            }
        } catch (IOException e) {
            // Ловим любые ошибки ввода-вывода и отправляем ответ с кодом 500
            try (final var out = new BufferedOutputStream(socket.getOutputStream())) {
                System.err.println("Internal Server Error: " + e.getMessage());
                sendResponse(out, new Request("500 Internal Server Error"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            // Ловим все остальные ошибки и отправляем ответ с кодом 500
            try (final var out = new BufferedOutputStream(socket.getOutputStream())) {
                System.err.println("Unexpected Error: " + e.getMessage());
                sendResponse(out, new Request("500 Internal Server Error"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void sendResponse(BufferedOutputStream out, Request request) throws IOException {
        // В случае ошибки добавляем сообщение об ошибке как HTML-страницу
        if (request.getHeader().startsWith("4") || request.getHeader().startsWith("5")) {
            String errorMessage = "<html><body><h1>" + request.getHeader() + "</h1></body></html>";

            out.write(request.badRequest().getBytes());
            out.write(errorMessage.getBytes());
        }
        else{
            String response = request.createRequest();
            out.write(response.getBytes());
            if (request.getContent() != null) {
                out.write(request.getContent().getBytes());
            }
        }
        out.flush();
    }
    public void addHandler(String method, String path, Handler handler) {
        handlers.computeIfAbsent(method, k -> new ConcurrentHashMap<>()).put(path, handler);
    }

    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}

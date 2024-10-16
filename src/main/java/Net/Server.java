package Net;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final List<String> allowedMethods = List.of(GET, POST);

    private final int port;
    private final ExecutorService threadPool;
    private final Map<String, Map<String, Handler>> handlers = new ConcurrentHashMap<>();

    public Server(int port, int numberThreads) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(numberThreads);
    }

    public void listen() {
        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    final var socket = serverSocket.accept();
                    threadPool.execute(() -> connectionHandler(socket));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connectionHandler(Socket socket) {
        try (final var in = new BufferedInputStream(socket.getInputStream());
             final var out = new BufferedOutputStream(socket.getOutputStream())) {

            var request = RequestParser.parse(in);

            if (request == null) {
                Response.badRequestError(out);
                return;
            }

            var method = request.getMethod();
            var path = request.getPath();

            // Обработка через динамические хендлеры
            if (handlers.containsKey(method) && handlers.get(method).containsKey(path)) {
                if (!Server.allowedMethods.contains(method)) {
                    // Если метод не разрешен, отправляем ответ с ошибкой 405 Method Not Allowed
                    Response.methodNotAllowed(out);
                    return;
                }
                // Получаем и обрабатываем запрос с допустимым методом
                Handler handler = handlers.get(method).get(path);
                try {
                    handler.handle(request, out);
                } catch (Exception e) {
                    Response.internalServerError(out);
                }
            } else {
                Response.notFound(out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addHandler(String method, String path, Handler handler) {
        handlers.computeIfAbsent(method, k -> new ConcurrentHashMap<>()).put(path, handler);
    }
}


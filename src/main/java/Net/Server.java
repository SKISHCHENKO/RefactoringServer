package Net;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Server {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    static {
        try {
            // Создание обработчика, который будет записывать логи в файл
            FileHandler fileHandler = new FileHandler("server.log", true);
            fileHandler.setLevel(Level.ALL); // Устанавливаем уровень логирования
            fileHandler.setFormatter(new SimpleFormatter()); // Форматируем лог
            LOGGER.addHandler(fileHandler); // Добавляем обработчик к логгеру
            LOGGER.setLevel(Level.ALL); // Логировать все уровни (от INFO до SEVERE)
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to set up file handler for logger", e);
        }
    }
    //public static final String GET = "GET";
    //public static final String POST = "POST";
    private static final Set<HttpMethod> allowedMethods = EnumSet.of(HttpMethod.GET, HttpMethod.POST);

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
                    LOGGER.log(Level.SEVERE, "Open Socket error", e);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "IO exception during open socket", e);
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

            var methodString = request.getMethod();
            HttpMethod method;
            try {
                method = HttpMethod.valueOf(methodString);
            } catch (IllegalArgumentException e) {
                // Если метод не распознан, отправляем ошибку 405 Method Not Allowed
                Response.methodNotAllowed(out);
                return;
            }

            var path = request.getPath();

            // Обработка через динамические хендлеры
            if (allowedMethods.contains(method)) {
                if (handlers.containsKey(method.name()) && handlers.get(method.name()).containsKey(path)) {
                    Handler handler = handlers.get(method.name()).get(path);
                    try {
                        handler.handle(request, out);
                    } catch (Exception e) {
                        Response.internalServerError(out);
                    }
                } else {
                    Response.notFound(out);
                }
            } else {
                Response.methodNotAllowed(out);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "IO Exception BufferedInputStream and BufferedOutputStream", e);
        }
    }

    public void addHandler(String method, String path, Handler handler) {
        handlers.computeIfAbsent(method, k -> new ConcurrentHashMap<>()).put(path, handler);
    }
}


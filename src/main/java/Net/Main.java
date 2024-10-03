package Net;


public class Main {
    public static void main(String[] args) {
        final var server = new Server(64);
        server.listen(9999);

    }
}
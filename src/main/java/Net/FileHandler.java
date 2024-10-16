package Net;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class FileHandler {
    private Path baseDir;

    public FileHandler(String baseDir) {
        this.baseDir = Path.of(baseDir);
    }

    public Optional<Path> resolveFile(String path) {
        Path filePath = baseDir.resolve(path).normalize();
        if (!filePath.startsWith(baseDir)) {
            return Optional.empty(); // предотвратить directory traversal
        }
        return Files.exists(filePath) ? Optional.of(filePath) : Optional.empty();
    }

    public void handleStaticFile(Path filePath, BufferedOutputStream out) throws IOException {
        String mimeType = Files.probeContentType(filePath);
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }
        byte[] content = Files.readAllBytes(filePath);
        String responseHeader = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: " + mimeType + "\r\n" +
                "Content-Length: " + content.length + "\r\n" +
                "Connection: close\r\n\r\n";
        out.write(responseHeader.getBytes(StandardCharsets.UTF_8));
        out.write(content);
        out.flush();
    }
}
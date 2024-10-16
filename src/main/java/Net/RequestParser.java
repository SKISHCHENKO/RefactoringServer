package Net;

import org.apache.commons.fileupload.FileItem;
import org.apache.hc.core5.net.URLEncodedUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class RequestParser {

    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_LENGTH_HEADER = "Content-Length";

    public static Request parse(BufferedInputStream in) throws IOException {
        // Лимит на request line + заголовки
        final var limit = 4096;

        in.mark(limit);
        final var buffer = new byte[limit];
        final var read = in.read(buffer);

        // Ищем request line
        final var requestLineDelimiter = new byte[]{'\r', '\n'};
        final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);

        if (requestLineEnd == -1) {
            return null;
        }

        final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
        if (requestLine.length != 3) {
            return null;
        }

        final var method = requestLine[0];
        final var query = requestLine[1];
        final var version = requestLine[2];
        byte[] body = null;

        // Получаем заголовки
        final var headersEnd = indexOf(buffer, new byte[]{'\r', '\n', '\r', '\n'}, requestLineEnd, read);
        if (headersEnd == -1) {
            return null;
        }

        var headers = Arrays.asList(new String(Arrays.copyOfRange(buffer, requestLineEnd + 2, headersEnd)).split("\r\n"));

        String contentType = null;
        int contentLength = 0;

        // Получаем Content-Type и Content-Length
        for (String header : headers) {
            if (header.startsWith(CONTENT_TYPE_HEADER)) {
                contentType = header.substring(header.indexOf(":") + 1).trim();
            } else if (header.startsWith(CONTENT_LENGTH_HEADER)) {
                contentLength = Integer.parseInt(header.substring(header.indexOf(":") + 1).trim());
            }
        }

        // Обработка POST-запроса
        if ("POST".equalsIgnoreCase(method) && contentLength > 0) {
            body = new byte[contentLength];
            in.read(body);
        }

        URI uri;
        try {
            uri = new URI(query);
        } catch (URISyntaxException e) {
            System.out.println("Ошибка URI: " + e.getMessage());
            return null;
        }

        var queryParams = URLEncodedUtils.parse(uri, StandardCharsets.UTF_8);
        List<FileItem> fileItems = new ArrayList<>();

        // Если это multipart-запрос, парсим его
        if (isMultipartContent(contentType)) {
            fileItems = MultipartParser.parse(new ByteArrayInputStream(body), contentType, contentLength);
        }

        return new Request(method, uri.getPath(), version, headers, body, queryParams, fileItems);
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

    public static boolean isMultipartContent(String contentType) {
        return contentType != null && contentType.startsWith("multipart/");
    }
    public static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }
}
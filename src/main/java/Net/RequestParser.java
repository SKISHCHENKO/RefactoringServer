package Net;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URLEncodedUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class RequestParser {
    public final static String GET = "GET";
    public final static String POST = "POST";



    private static final String contentLengthHeaderName = "Content-Length";

    public static Request parse(BufferedInputStream in) throws IOException {
        // лимит на request line + заголовки
        final var limit = 4096;

        in.mark(limit);
        final var buffer = new byte[limit];
        final var read = in.read(buffer);

        // ищем request line
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

        System.out.println("method = " + method);
        System.out.println("query = " + query);
        System.out.println("version = " + version);

        final var headersEnd = indexOf(buffer, new byte[]{'\r', '\n', '\r', '\n'}, requestLineEnd, read);
        if (headersEnd == -1) {
            return null;
        }

        final var headers = Arrays.asList(new String(Arrays.copyOfRange(buffer, requestLineEnd + 2, headersEnd)).split("\r\n"));

        // Обработка POST-запроса
        if (method.equals(POST)) {
            int contentLength = 0;
            var optionalContentLength = extractHeader(headers, contentLengthHeaderName);
            if (optionalContentLength.isPresent()) {
                contentLength = Integer.parseInt(optionalContentLength.get());
            }

            if (contentLength > 0) {
                body = new byte[contentLength];
                in.read(body);
            }
        }

        URI uri;
        try {
            uri = new URI(query);
        } catch (URISyntaxException e) {
            System.out.println("Ошибка URI" + e.getMessage());
            return null;
        }

        var queryParams = URLEncodedUtils.parse(uri, StandardCharsets.UTF_8);

        List<NameValuePair> postParams = new ArrayList<>();

        var optionalContentType = extractHeader(headers, "Content-Type");
        if (optionalContentType.isPresent() && optionalContentType.get().equals("application/x-www-form-urlencoded")) {
            // убрать под if для Content-Type: application/x-www-form-urlencoded
            URI uriFromBody = null;
            try {
                uriFromBody = new URI(new String(body));
            } catch (URISyntaxException e) {
                System.out.println("Ошибка парсинга тела запроса: " + e.getMessage());
            }
            if (uriFromBody != null) {
                postParams = URLEncodedUtils.parse(uriFromBody, StandardCharsets.UTF_8);
            } else {
                System.out.println("Ошибка: uriFromBody оказался null.");
            }
        }

        return new Request(method, uri.getPath(), version, headers, body, queryParams, postParams);
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

    public static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }
}

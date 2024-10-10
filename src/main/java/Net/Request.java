package Net;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Request {
    final static String GET = "GET";
    final static String POST = "POST";
    final List<String> allowedMethods = List.of(GET, POST);
    List<NameValuePair> params = null;

    private String query;

    private String method;
    private String path;
    private String header;
    private String mimeType;
    private String content;
    private String version = "HTTP/1.1 ";
    private int contentLength = 0;

    public Request(String method, String query, String header, String version) {
        this.method = method;
        this.query = query;
        this.mimeType = "text/html";
        URI uri = null;
        try {
            uri = new URI(this.query);
        } catch (URISyntaxException e) {
            System.out.println("Ошибка URI" + e.getMessage());
        }
        // Получение пути
        assert uri != null;
        this.path = uri.getPath();
        this.header = header;
        this.version = version;
    }

    public Request(String header) {
        this.header = header;
        this.mimeType = "text/html";
        this.content = "";
        this.version = "HTTP/1.1";
    }

    public String getQuery() {
        return query;
    }

    public String getPath() {
        return path;
    }

    public String getHeader() {
        return header;
    }


    public String getVersion() {
        return version;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public String createRequest() {
        return this.version + " " + this.header + "\r\n" +
                "Content-Type: " + this.mimeType + "\r\n" +
                "Content-Length: " + (contentLength == 0 ? content.getBytes().length : contentLength) + "\r\n" +
                "Connection: close" + "\r\n" +
                "\r\n";
    }

    public String badRequest() {
        String errorMessage = "<html><body><h1>" + this.getHeader() + "</h1></body></html>";
        byte[] errorContent = errorMessage.getBytes();
        String errorResponse = this.getVersion() + " " + this.getHeader() + "\r\n" +
                "Content-Type: text/html" + "\r\n" +
                "Content-Length: " + errorContent.length + "\r\n" +
                "Connection: close" + "\r\n" +
                "\r\n";
        return errorResponse;
    }

    public String getQueryParams() {
        StringBuilder str = new StringBuilder();
        try {
            // Парсинг строки URI
            URI uri = new URI(this.query);
            // Получение пути
            String path = uri.getPath();
            System.out.println("Path: " + path);
            // Извлечение параметров из строки запроса
            params = URLEncodedUtils.parse(uri, StandardCharsets.UTF_8);
            str.append("String query :").append("<br>"); // <br> - перевод строки в html
            for (NameValuePair param : params) {
                System.out.println(param.getName() + " = " + param.getValue());
                str.append(param.getName() + " = " + param.getValue()).append("<br>");
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return str.toString();
    }

    public String getQueryParam(String name) {
        try {
        URI uri = new URI(this.query);
        // Получение пути
        String path = uri.getPath();
        System.out.println("Path: " + path);
        // Извлечение параметров из строки запроса
        params = URLEncodedUtils.parse(uri, StandardCharsets.UTF_8);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        if (params == null || params.isEmpty()) {
            return "String Query is Empty!";
        }
        for (NameValuePair param : params) {
            if (param.getName().equals(name)) {
                return param.getName() + " = " + param.getValue();
            }
        }
        return "String Query not contain : " + name;
    }
}
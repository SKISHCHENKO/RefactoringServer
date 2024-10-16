package Net;

import org.apache.hc.core5.http.NameValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.fileupload.FileItem;

public class Request {
    private final List<NameValuePair> queryParams;  // Параметры GET-запроса
    private final List<NameValuePair> postParams;   // Параметры POST-запроса
    private final List<FileItem> fileItems;         // Загруженные файлы

    private final String method;
    private final String path;
    private final byte[] body;
    private final String version;
    private final List<String> headers;

    public Request(String method, String path, String version, List<String> headers, byte[] body,
                   List<NameValuePair> queryParams, List<FileItem> fileItems) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.version = version;
        this.body = body;
        this.queryParams = queryParams;
        this.fileItems = fileItems;
        this.postParams = new ArrayList<>(); // Инициализация postParams, если нужно
    }

    public String getPath() {
        return path;
    }

    public String getVersion() {
        return version;
    }

    public byte[] getBody() {
        return body;
    }

    public List<NameValuePair> getQueryParams() {
        return queryParams;
    }

    public List<NameValuePair> getPostParams() {
        return postParams;
    }

    public List<FileItem> getFileItems() {
        return fileItems;
    }

    public List<NameValuePair> getQueryParam(String name) {
        return queryParams
                .stream()
                .filter(x -> Objects.equals(x.getName(), name))
                .toList();
    }

    public List<NameValuePair> getPostParam(String name) {
        return postParams
                .stream()
                .filter(x -> Objects.equals(x.getName(), name))
                .toList();
    }

    public Optional<String> getHeader(String name) {
        return RequestParser.extractHeader(headers, name);
    }

    public String getMethod() {
        return method;
    }
}
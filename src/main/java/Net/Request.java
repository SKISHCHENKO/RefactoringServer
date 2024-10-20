package Net;

import org.apache.hc.core5.http.NameValuePair;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Request {
    private final List<NameValuePair> queryParams;  // Параметры GET-запроса
    private final List<NameValuePair> postParams;   // Параметры POST-запроса

    private final String method;
    private final String path;
    private final byte[] body;
    private final String version;
    private final List<String> headers;

    public Request(String method, String path, String version, List<String> headers, byte[] body, List<NameValuePair> queryParams, List<NameValuePair> postParams) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.version = version;
        this.body = body;
        this.queryParams = queryParams;
        this.postParams = postParams;
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

    public List<NameValuePair> getQueryParam(String name) {
        return queryParams
                .stream()
                .filter(x -> Objects.equals(x.getName(), name))
                .toList();
    }

    // Метод для получения всех POST-параметров
    public List<NameValuePair> getPostParams() {
        return postParams;
    }

    // Метод для получения одного POST-параметра по имени
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

    @Override
    public String toString() {
        return "Request{" +
                "method='" + method + '\'' +
                ", path='" + path + '\'' +
                ", version='" + version + '\'' +
                ", headers=" + headers +
                ", queryParams=" + queryParams +
                ", postParams=" + postParams +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Request request = (Request) o;
        return Objects.equals(method, request.method) &&
                Objects.equals(path, request.path) &&
                Objects.equals(version, request.version) &&
                Objects.equals(headers, request.headers) &&
                Objects.equals(queryParams, request.queryParams) &&
                Objects.equals(postParams, request.postParams);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, path, version, headers, queryParams, postParams);
    }
}

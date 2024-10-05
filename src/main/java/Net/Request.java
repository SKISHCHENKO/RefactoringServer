package Net;

import java.util.List;

public class Request {
    final static String GET = "GET";
    final static String POST = "POST";
    final List<String> allowedMethods = List.of(GET, POST);
    private final String DELIM = "\r\n";

    private String method;
    private String path;
    private final String header;
    private final String mimeType;
    private String contentBody;
    private int content;
    private String version = "HTTP/1.1 ";

    public Request(String method, String path, String header, String mimeType, int content, String contentBody, String version) {
        this.method = method;
        this.path = path;
        this.header = header;
        this.mimeType = mimeType;
        this.content = content;
        this.contentBody = contentBody;
        this.version = version;
    }

    public Request(String method, String path, String header, String mimeType, int content, String version) {
        this.method = method;
        this.path = path;
        this.header = header;
        this.mimeType = mimeType;
        this.content = content;
        this.version = version;
    }
    public Request(String method, String path, String header, String mimeType, String version) {
        this.method = method;
        this.path = path;
        this.header = header;
        this.mimeType = mimeType;
        this.version = version;
    }

    public Request(String header) {
        this.header = header;
        this.mimeType = "text/html";
        this.content = 0;
        this.version = "HTTP/1.1";
    }

    public String getHeader() {
        return header;
    }

    public String getContentBody() {
        return contentBody;
    }

    public String getVersion() {
        return version;
    }

    public String createRequest() {
        return this.version + " " + this.header + DELIM +
                "Content-Type: " + this.mimeType + DELIM +
                "Content-Length: " + (this.contentBody != null ? contentBody.getBytes().length : this.content) + DELIM +
                "Connection: close" + DELIM +
                DELIM;
    }
}
package Net;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MultipartParser {

    public static List<FileItem> parse(InputStream inputStream, String contentType, int contentLength) throws IOException {
        List<FileItem> fileItems = new ArrayList<>();

        // Извлекаем boundary из Content-Type
        String boundary = extractBoundary(contentType);
        if (boundary == null) {
            throw new IllegalArgumentException("Не удалось извлечь boundary из Content-Type.");
        }

        byte[] buffer = new byte[contentLength];
        int bytesRead = inputStream.read(buffer);

        // Используем ByteArrayInputStream для парсинга данных
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer)) {
            byte[] lineBuffer = new byte[1024];
            StringBuilder lineBuilder = new StringBuilder();
            boolean isField = false;
            String fieldName = null;
            String fileName = null;
            String contentTypeHeader = null;

            while ((bytesRead = byteArrayInputStream.read(lineBuffer)) != -1) {
                lineBuilder.append(new String(lineBuffer, 0, bytesRead, StandardCharsets.UTF_8));

                // Проверяем наличие boundary
                if (lineBuilder.toString().contains("--" + boundary)) {
                    // Если нашли boundary, добавляем предыдущий элемент
                    if (isField) {
                        // Здесь должен быть ваш метод для добавления обычного поля
                        fileItems.add(new DiskFileItem(fieldName, contentTypeHeader, false, fileName, 0, null));
                    }
                    // Сброс
                    lineBuilder.setLength(0);
                    isField = false;
                }

                // Проверяем окончание поля
                if (lineBuilder.toString().contains("\r\n")) {
                    String[] headers = lineBuilder.toString().split("\r\n");

                    for (String header : headers) {
                        if (header.startsWith("Content-Disposition")) {
                            String[] parts = header.split(";");
                            for (String part : parts) {
                                if (part.trim().startsWith("name=")) {
                                    fieldName = part.substring(part.indexOf("=") + 1).replace("\"", "").trim();
                                    isField = true;
                                }
                                if (part.trim().startsWith("filename=")) {
                                    fileName = part.substring(part.indexOf("=") + 1).replace("\"", "").trim();
                                }
                            }
                        } else if (header.startsWith("Content-Type")) {
                            contentTypeHeader = header.split(":")[1].trim();
                        }
                    }
                    lineBuilder.setLength(0); // Сброс буфера
                }
            }
        }

        return fileItems;
    }

    private static String extractBoundary(String contentType) {
        if (contentType != null && contentType.startsWith("multipart/")) {
            String[] parts = contentType.split(";");
            for (String part : parts) {
                if (part.trim().startsWith("boundary=")) {
                    return part.substring(part.indexOf("=") + 1).trim();
                }
            }
        }
        return null;
    }
}
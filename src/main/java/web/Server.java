package web;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class Server {
    final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    ServerSocket serverSocket;

    public Server() throws IOException {
        serverSocket = new ServerSocket(9999);
    }

    public String getResponse(String status, String mimeType, long length) throws IOException {
        return "HTTP/1.1 " + status + "\r\n" +
                (mimeType.isEmpty() ? "" : "Content-Type: " + mimeType + "\r\n") +
                "Content-Length: " + length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n";
    }

    public void start() {
        try {

            try (
                    final var socket = serverSocket.accept();
                    final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    final var out = new BufferedOutputStream(socket.getOutputStream());
            ) {
                // read only request line for simplicity
                // must be in form GET /path HTTP/1.1
                final var requestLine = in.readLine();

                final var parts = requestLine.split(" ");

                if (parts.length != 3) {
                    // just close socket
                    out.write((getResponse("404 Not Found", "", 0)).getBytes());
                    out.flush();
                    return;
                }

                final var path = parts[1];
                if (!validPaths.contains(path)) {
                    out.write((getResponse("404 Not Found", "", 0)).getBytes());
                    out.flush();
                    return;
                }

                final var filePath = Path.of(".", "public", path);
                final var mimeType = Files.probeContentType(filePath);

                // special case for classic
                if (path.equals("/classic.html")) {
                    final var template = Files.readString(filePath);
                    final var content = template.replace(
                            "{time}",
                            LocalDateTime.now().toString()
                    ).getBytes();
                    out.write((getResponse("200 OK", mimeType, content.length)).getBytes());
                    out.write(content);
                    out.flush();
                    return;
                }

                final var length = Files.size(filePath);
                out.write((getResponse("200 OK", mimeType, length)
                ).getBytes());
                Files.copy(filePath, out);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

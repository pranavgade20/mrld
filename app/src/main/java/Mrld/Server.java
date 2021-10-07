package Mrld;

import jLHS.*;
import jLHS.exceptions.ProtocolFormatException;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Server extends jLHS.Server {
    int port;
    String basePath;

    public Server(int port, String basePath) throws IOException {
        super(port);
        this.port = port;
        this.basePath = basePath;
        this.on(Method.GET, "/", ((request, response) -> {
            try {
                response.writeHeader("Content-Type", "text/html");
                response.write(getClass().getClassLoader().getResourceAsStream("index.html"));
                response.end();
            } catch (ProtocolFormatException | IOException e) {
                e.printStackTrace();
            }
        }));
        this.on(Method.GET, "/public/\\S*", ((request, response) -> {
            try {
                // TODO implement partial content, but might not be worth it
                var url = getClass().getClassLoader().getResource(request.getPath().substring(1));
                if (url == null) {
                    response.setCode(404, "Not Found");
                    response.print("The requested URL was not found on this server.");
                    response.end();
                    return;
                }
                response.writeHeader("Content-Type", Files.probeContentType(new File(url.getPath()).toPath()));
                response.write(getClass().getClassLoader().getResourceAsStream(request.getPath().substring(1)));
                response.end();
            } catch (ProtocolFormatException | IOException e) {
                e.printStackTrace();
            }
        }));
        this.on(Method.GET, "/file/\\S*", ((request, response) -> {
            try {
                var file = new File(basePath + request.getPath().substring("/file".length()));
                // TODO implement partial content
                if (!file.isFile()) {
                    response.setCode(404, "Not Found");
                    response.print("The requested file was not found on this server.");
                    response.end();
                    return;
                }
                response.writeHeader("Content-Type", Files.probeContentType(file.toPath()));
                response.write(new FileInputStream(file));
                response.end();
            } catch (ProtocolFormatException | IOException e) {
                e.printStackTrace();
            }
        }));
        this.on(Method.GET, "/listFiles/\\S*", ((request, response) -> {
            try {
                var file = new File(basePath + request.getPath().substring("/listFiles".length()));
                // TODO implement partial content
                if (!file.exists()) {
                    response.print("{'error': 'does not exist'}");
                    response.end();
                    return;
                } else if (!file.isDirectory()) {
                    response.print("{'error': 'not a directory'}");
                    response.end();
                    return;
                }
                response.writeHeader("Content-Type", "text/json");
                response.print("{\n\"path\": \"" + request.getPath().substring("/listFiles/".length()) + "\"");
                response.print(",\n\"files\":");
                response.print(Arrays.stream(file.listFiles()).filter(File::isFile).map(f -> "\"" + f.getName() + "\"").collect(Collectors.toList()).toString());
                response.print(",\n\"directories\":");
                response.print(Arrays.stream(file.listFiles()).filter(File::isDirectory).map(f -> "\"" + f.getName() + "\"").collect(Collectors.toList()).toString());
                response.print("\n}");
                response.end();
            } catch (ProtocolFormatException | IOException e) {
                e.printStackTrace();
            }
        }));

    }
}

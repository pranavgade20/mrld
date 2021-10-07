package Mrld;

import jLHS.*;
import jLHS.exceptions.ProtocolFormatException;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
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
                String range = request.getHeader("Range");
                long from = 0, to = 0;
                if (range != null) {
                    if (!range.startsWith("bytes")) range = null;
                    else if (range.contains(",")) range = null;
                    else {
                        String[] ranges = range.substring("bytes ".length()).split("[-/]");
                        if (ranges[0].isBlank()) {
                            ranges[0] = "0";
                        }
                        from = Long.parseLong(ranges[0]);
                        to = ranges.length > 2 && ranges[1].isEmpty() ? Long.parseLong(ranges[1]) : Files.size(file.toPath());
                        if (from > to || to > Files.size(file.toPath())) {
                            range = null;
                        }
                    }
                }

                if (range == null) {
                    response.writeHeader("Content-Type", Files.probeContentType(file.toPath()));
                    response.write(new FileInputStream(file));
                    response.end();
                } else {
                    response.setCode(206, "Partial Content");
                    response.writeHeader("Content-Type", Files.probeContentType(file.toPath()));
                    response.writeHeader("Content-Length", String.valueOf(to - from));
                    response.writeHeader("Content-Range", "bytes " + to  + "-" + from + "/" + Files.size(file.toPath()));

                    FileInputStream in = new FileInputStream(file);
                    OutputStream out = response.getOutputStream();
                    long skipped = from;
                    while (skipped > 0) skipped -= in.skip(skipped);
                    long toTransfer = to-from;
                    int read;
                    for(byte[] buffer = new byte[8192]; (read = in.read(buffer, 0, (int) Math.min(8192, toTransfer))) >= 0; toTransfer -= read) {
                        out.write(buffer, 0, read);
                    }
                    out.flush();

                    response.end();
                }
            } catch (ProtocolFormatException | IOException e) {
                e.printStackTrace();
            }
        }));
        this.on(Method.GET, "/listFiles/\\S*", ((request, response) -> {
            try {
                var file = new File(basePath + request.getPath().substring("/listFiles".length()));
                if (!file.exists()) {
                    response.print("{'error': 'does not exist'}");
                    response.end();
                    return;
                } else if (!file.isDirectory()) {
                    response.print("{'error': 'not a directory'}");
                    response.end();
                    return;
                }
                response.writeHeader("Content-Type", "application/json");
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

        addDefaultHeaders(List.of("Accept-Ranges: bytes", "Connection: close"));
    }
}

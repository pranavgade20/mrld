package Mrld;

import jLHS.Method;
import jLHS.exceptions.ProtocolFormatException;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class Server extends jLHS.http1_1server.Server {
    int port;
    AtomicReference<String> basePath;

    public Server(int port, AtomicReference<String> basePath) throws IOException {
        super(port);
        super.MAX_CONCURRENT_CONNECTIONS = 16;
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
        this.on(Method.GET, "/ping", ((request, response) -> {
            try {
                response.writeHeader("Content-Type", "text/html");
                response.print("pong");
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
        this.on(Method.GET, "/file/[\\S\\s]*", ((request, response) -> {
            try {
                var file = new File(basePath.get() + request.getPath().substring("/file".length()));
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
                        to = ranges.length > 2 && ranges[1].isEmpty() ? Files.size(file.toPath()) : (Long.parseLong(ranges[1])+1);
                        if (from > to || to > Files.size(file.toPath())) {
                            response.setCode(416, "Range Not Satisfiable");
                            response.print("The requested range can not be satisfied.");
                            response.end();
                            return;
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
                    response.writeHeader("Content-Range", "bytes " + from  + "-" + (to-1) + "/" + Files.size(file.toPath()));

                    FileInputStream in = new FileInputStream(file);
                    in.skipNBytes(from);
                    long finalTo = to;
                    long finalFrom = from;
                    response.write(new InputStream() {
                        long remaining = finalTo - finalFrom;
                        @Override
                        public int read() throws IOException {
                            if (remaining-- > 0) return in.read();
                            return -1;
                        }

                        @Override
                        public int read(byte[] b, int off, int len) throws IOException {
                            if (remaining < 1) return -1;
                            int ret = in.read(b, off, (int) Math.min(remaining, len));
                            if (ret > 0) remaining -= ret;
                            return ret;
                        }
                    });

                    response.end();
                }
            } catch (ProtocolFormatException | IOException e) {
                e.printStackTrace();
            }
        }));
        this.on(Method.GET, "/listFiles/[\\S\\s]*", ((request, response) -> {
            try {
                var file = new File(basePath.get() + request.getPath().substring("/listFiles".length()));
                if (!file.exists()) {
                    response.print("{\"error\": \"does not exist\"}");
                    response.end();
                    return;
                } else if (!file.isDirectory()) {
                    response.print("{\"error\": \"not a directory\"}");
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

        this.on(Method.POST, "/upload/[\\S\\s]*", (((request, response) -> {
            try {
                var file = new File(basePath.get() + request.getPath().substring("/upload".length()));
                if (file.exists()) {
                    response.print("{\"error\": \"file already exists\"}");
                    response.end();
                    return;
                }

                var fileOutputStream = new FileOutputStream(file);
                request.getRequestReader().getFormData("file").orElseThrow().getFormData().transferTo(fileOutputStream);
                fileOutputStream.close();

                response.writeHeader("Content-Type", "application/json");
                response.print("{\"success\": \"file uploaded\"}");
                response.end();
            } catch (ProtocolFormatException | IOException e) {
                try {
                    response.setCode(500, "Internal server error");
                    response.end();
                } catch (ProtocolFormatException | IOException ex) {
                    ex.printStackTrace();
                }
                e.printStackTrace();
            }
        })));

        addDefaultHeaders(List.of("Accept-Ranges: bytes", "Connection: keep-alive", "Keep-Alive: timeout=5000, max=10000"));
    }
}

package clic.epfl.qrbill;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import net.codecrete.qrbill.generator.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import static clic.epfl.qrbill.Main.HTTPResponses.*;

public class Main {
    public static final int PORT = 8000;

    public static byte[] generateQR(JSONBill jsonBill) {
        var bill = jsonBill.toBill();

        // Set output format
        BillFormat format = new BillFormat();
        format.setGraphicsFormat(GraphicsFormat.SVG);
        format.setOutputSize(OutputSize.QR_BILL_ONLY);
        format.setLanguage(Language.FR);
        bill.setFormat(format);

        // Generate QR bill
        return QRBill.generate(bill);

    }

    public static void main(String[] args) {
        try {
            // Create an HTTP server on port 8000
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

            // Route to handle POST requests with JSON input
            server.createContext("/", exchange -> {
                if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    JSONBill bill = new ObjectMapper().readValue(exchange.getRequestBody(), JSONBill.class);

                    try (OutputStream outputStream = exchange.getResponseBody()) {
                        try {
                            var qr = generateQR(bill);
                            exchange.getResponseHeaders().add("Content-Type", "image/svg+xml");
                            exchange.sendResponseHeaders(OK.code(), qr.length);
                            outputStream.write(qr);
                        } catch (QRBillValidationError e) {
                            exchange.sendResponseHeaders(BAD_REQUEST.code(), e.getMessage().length());
                            outputStream.write(e.getMessage().getBytes());
                            exchange.close();
                        }
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                        exchange.sendResponseHeaders(SERVER_ERROR.code(), 0);
                        exchange.close();
                    }
                } else {
                    // Return 405 Method Not Allowed for non-POST requests
                    exchange.sendResponseHeaders(NOT_ALLOWED.code(), -1);
                }
            });

            // Start the server
            server.setExecutor(null); // Use default executor
            server.start();
            System.out.print("Server running at http://localhost:");
            System.out.print(server.getAddress().getPort());
            System.out.println('/');
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    enum HTTPResponses {
        OK(200),
        BAD_REQUEST(400),
        NOT_ALLOWED(405),
        SERVER_ERROR(500);

        private final int code;

        HTTPResponses(int code) {
            this.code = code;
        }

        int code() {
            return this.code;
        }
    }
}

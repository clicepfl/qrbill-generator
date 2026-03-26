package clic.epfl.qrbill;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import net.codecrete.qrbill.generator.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class Main {
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
            HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

            // Route to handle POST requests with JSON input
            server.createContext("/", exchange ->
            {
                if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    JSONBill bill = new ObjectMapper().readValue(exchange.getRequestBody(), JSONBill.class);

                    try (OutputStream outputStream = exchange.getResponseBody()) {
                        try {
                            var qr = generateQR(bill);
                            exchange.getResponseHeaders().add("Content-Type", "image/svg+xml");
                            exchange.sendResponseHeaders(200, qr.length);
                            outputStream.write(qr);
                        } catch (QRBillValidationError e) {
                            exchange.sendResponseHeaders(400, e.getMessage().length());
                            outputStream.write(e.getMessage().getBytes());
                            exchange.close();
                        }
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                        exchange.sendResponseHeaders(500, 0);
                        exchange.close();
                    }
                } else {
                    // Return 405 Method Not Allowed for non-POST requests
                    exchange.sendResponseHeaders(405, -1);
                }
            });

            // Start the server
            server.setExecutor(null); // Use default executor
            server.start();
            System.out.println("Server running at http://localhost:8000/");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

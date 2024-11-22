package net.codecrete.qrbill;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import net.codecrete.qrbill.generator.*;

import java.io.*;
import java.net.InetSocketAddress;

// Message model with Jackson annotations
class Message {
    @JsonProperty("message")
    private String message;

    public Message() {}

    public Message(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

public class Main {
    public static byte[] generateQR() {
        // Setup bill
        Bill bill = new Bill();
        bill.setAccount("CH2304835177498341002");
        bill.setAmountFromDouble(199.95);
        bill.setCurrency("CHF");

        // Set creditor
        Address creditor = new Address();
        creditor.setName("CLIC");
        creditor.setStreet("Rue du Lac");
        creditor.setHouseNo("1268/2/22");
        creditor.setPostalCode("1015");
        creditor.setTown("Lausanne");
        creditor.setCountryCode("CH");
        bill.setCreditor(creditor);

        // more bill data
        bill.setUnstructuredMessage("Abonnement für 2020");

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
                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    try (OutputStream outputStream = exchange.getResponseBody()) {
                        var qr = generateQR();
                        exchange.getResponseHeaders().add("Content-Type", "image/svg+xml");
                        exchange.sendResponseHeaders(200, qr.length);
                        outputStream.write(qr);
                    }
                    catch (Exception e)
                    {
                        System.err.println(e.getMessage());
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

    private static void handleJsonRequest(HttpExchange exchange) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Message responseMessage;

        try (InputStream inputStream = exchange.getRequestBody()) {
            // Deserialize JSON input to Message object
            Message inputMessage = objectMapper.readValue(inputStream, Message.class);

            // Prepare response
            responseMessage = new Message("You said: " + inputMessage.getMessage());
            String jsonResponse = objectMapper.writeValueAsString(responseMessage);

            // Send 200 response with the JSON
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(jsonResponse.getBytes());
            }
        } catch (Exception e) {
            // Handle invalid JSON or other errors
            String errorResponse = objectMapper.writeValueAsString(new Message("Invalid JSON input"));
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(400, errorResponse.getBytes().length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(errorResponse.getBytes());
            }
        }
    }
}

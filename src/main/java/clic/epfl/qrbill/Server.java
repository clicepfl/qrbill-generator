package clic.epfl.qrbill;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import net.codecrete.qrbill.generator.QRBillValidationError;

import java.io.IOException;
import java.io.OutputStream;

import static clic.epfl.qrbill.Server.HTTPResponses.*;

public class Server {
    public static void generateQRBillFromPost(HttpExchange exchange) {
        try {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                // Return 405 Method Not Allowed for non-POST requests
                exchange.sendResponseHeaders(NOT_ALLOWED.code(), -1);
                return;
            }
            JSONBill bill = new ObjectMapper().readValue(exchange.getRequestBody(), JSONBill.class);

            try (OutputStream outputStream = exchange.getResponseBody()) {
                try {
                    var qr = Main.generateQR(bill);
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

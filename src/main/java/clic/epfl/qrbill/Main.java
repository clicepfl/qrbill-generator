package clic.epfl.qrbill;

import com.sun.net.httpserver.HttpServer;
import net.codecrete.qrbill.generator.*;

import java.io.IOException;
import java.net.InetSocketAddress;

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
            server.createContext("/", Server::generateQRBillFromPost);

            // Start the server
            server.setExecutor(null); // Use default executor
            server.start();

            System.out.print("Server running at http://localhost:");
            System.out.print(server.getAddress().getPort());
            System.out.println('/');
        } catch (IOException e) {
            System.err.print("Failed launching server at http://localhost:");
            System.err.print(PORT);
            System.err.println('/');

            System.exit(1);
        }
    }
}

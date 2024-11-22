package clic.epfl.qrbill;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import net.codecrete.qrbill.generator.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

// JSON representation of Bill
record JSONBill(@JsonProperty("account") String account, @JsonProperty("amount") double amount,
                @JsonProperty("currency") String currency, @JsonProperty("creditor") JSONAddress creditor,
                @JsonProperty("message") String message)
{
    public Bill toBill()
    {
        Address creditor = creditor().toAddress();
        Bill bill = new Bill();
        bill.setAccount(account());
        bill.setAmountFromDouble(amount());
        bill.setCurrency(currency());
        bill.setUnstructuredMessage(message());
        bill.setCreditor(creditor);
        return bill;
    }

}

// JSON representation of Address
record JSONAddress(@JsonProperty("name") String name, @JsonProperty("street") String street,
                   @JsonProperty("houseNo") String houseNo, @JsonProperty("postalCode") String postalCode,
                   @JsonProperty("town") String town, @JsonProperty("countryCode") String countryCode)
{
    public Address toAddress()
    {
        Address address = new Address();
        address.setName(name());
        address.setStreet(street());
        address.setHouseNo(houseNo());
        address.setPostalCode(postalCode());
        address.setTown(town());
        address.setCountryCode(countryCode());
        return address;
    }

}


public class Main
{
    public static byte[] generateQR(JSONBill jsonBill)
    {
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

    public static void main(String[] args)
    {
        try
        {
            // Create an HTTP server on port 8000
            HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

            // Route to handle POST requests with JSON input
            server.createContext("/", exchange ->
            {
                if ("POST".equalsIgnoreCase(exchange.getRequestMethod()))
                {
                    JSONBill bill = new ObjectMapper().readValue(exchange.getRequestBody(), JSONBill.class);

                    try (OutputStream outputStream = exchange.getResponseBody())
                    {
                        try
                        {
                            var qr = generateQR(bill);
                            exchange.getResponseHeaders().add("Content-Type", "image/svg+xml");
                            exchange.sendResponseHeaders(200, qr.length);
                            outputStream.write(qr);
                        }
                        catch (QRBillValidationError e)
                        {
                            exchange.sendResponseHeaders(400, e.getMessage().length());
                            outputStream.write(e.getMessage().getBytes());
                            exchange.close();
                        }
                    }
                    catch (Exception e)
                    {
                        System.err.println(e.getMessage());
                        exchange.sendResponseHeaders(500, 0);
                        exchange.close();
                    }
                } else
                {
                    // Return 405 Method Not Allowed for non-POST requests
                    exchange.sendResponseHeaders(405, -1);
                }
            });

            // Start the server
            server.setExecutor(null); // Use default executor
            server.start();
            System.out.println("Server running at http://localhost:8000/");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}

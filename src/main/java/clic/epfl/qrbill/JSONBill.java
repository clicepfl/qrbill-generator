package clic.epfl.qrbill;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.codecrete.qrbill.generator.Address;
import net.codecrete.qrbill.generator.Bill;

public record JSONBill(@JsonProperty("account") String account, @JsonProperty("amount") double amount,
                @JsonProperty("currency") String currency, @JsonProperty("creditor") JSONAddress creditor,
                @JsonProperty("message") String message) {
    public Bill toBill() {
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

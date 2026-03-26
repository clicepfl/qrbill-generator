package clic.epfl.qrbill;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.codecrete.qrbill.generator.Address;

record JSONAddress(@JsonProperty("name") String name, @JsonProperty("street") String street,
                   @JsonProperty("houseNo") String houseNo, @JsonProperty("postalCode") String postalCode,
                   @JsonProperty("town") String town, @JsonProperty("countryCode") String countryCode) {
    public Address toAddress() {
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
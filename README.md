# QRBill generator API

Available at http://clic.epfl.ch/qrbill-generator.

### `POST

#### Request Body

```json
{
  "account": "CH2304835177498341002",
  "amount": 10,
  "currency": "CHF",
  "message": "Custom message",
  "creditor": {
    "name": "CLIC",
    "street": "Station 14, EPFL",
    "houseNo": "",
    "postalCode": "1015",
    "town": "Lausanne",
    "countryCode": "CH"
  }
}
```

#### Response body

Returns the QR Bill as a SVG image.

#### Example with cURL

```
curl -X POST https://clic.epfl.ch/qrbill-generator/ -d '{"account":"CH2304835177498341002","amount":10,"currency":"CHF", "message": "Souper de Faculté - Ludovic Mermod","creditor":{"name":"CLIC","street":"Station 14, EPFL","houseNo":"","postalCode":"1015","town":"Lausanne", "countryCode": "CH"}}'
```

# Cordova Plugin for Samsung Pay

This repository contains a Cordova plugin for integrating Samsung Pay into mobile applications.

## Installation

To install the plugin, use the following command in your Cordova project:

```bash
cordova plugin add https://github.com/os-adv-dev/cordova-plugin-samsungpay.git --variable APP_SERVICE_ID="YOUR_APP_SERVICE_ID" --variable APP_DEBUG_MODE="Y or N" --variable APP_DEBUG_API_KEY="YOUR_DEBUG_API_KEY" --variable APP_ISSUER_NAME="YOUR_ISSUER_NAME" --variable APP_SERVICE_TYPE="YOUR_SERVICE_TYPE"
```

## Service Types

When installing the plugin, you must specify the `APP_SERVICE_TYPE` variable according to the Samsung Pay service you intend to use. Below are the available service types:

- `APP2APP` - Service type for using Issuer service.
- `INAPP_PAYMENT` - Service type for using Online in-app payment service.
- `INTERNAL_APK` - Service type for using Samsung Pay internal service.
- `MOBILEWEB_PAYMENT` - Service type for using Mobile web payment service.
- `W3C` - Service type for using W3C payment service.
- `WEB_PAYMENT` - Service type for using Web payment service.

Please refer to the Samsung Pay API Reference for detailed information: [Samsung Pay API](https://developer.samsung.com/pay/api-reference/com/samsung/android/sdk/samsungpay/v2/SpaySdk.ServiceType.html)

Replace the placeholders with your actual configuration values above.

## Methods

The plugin offers the following methods:

### checkDeviceSupport

- **Usage**: `cordova.plugins.SamsungPayPlugin.checkDeviceSupport(successCallback, errorCallback)`
- **Response**:
  - **Error**: `{"message": "Samsung Pay is not supported!", "success": false}`
  - **Success**: `{"message": "Samsung Pay is supported.", "success": true}`

### getWalletInfo

- **Usage**: `cordova.plugins.SamsungPayPlugin.getWalletInfo(successCallback, errorCallback)`
- **Response**:
  - **Error**: `{"message": "Error getting wallet info", "success": false}`
  - **Success**: Example response: `{"clientDeviceId":"11111","walletAccountId":"1111"}`

### getAllCards

- **Usage**: `cordova.plugins.SamsungPayPlugin.getAllCards(successCallback, errorCallback)`
- **Response**:
  - **Error**: `{"message": "Error retrieving cards", "success": false}`
  - **Success**: Example response: `[{"cardId": "1", "cardType": "Credit", "last4FPan: "111", "last4DPan": "222", "cardIssuerName": "test issuer name" }, {"cardId": "2", "cardType": "Debit", "last4FPan: "222", "last4DPan": "333", "cardIssuerName": "test issuer" }]`

### addCard

- **Usage**: `cordova.plugins.SamsungPayPlugin.addCard(successCallback, errorCallback, args)`
- **Arguments**: `args` - Ex: abcd1w23sa1 - this is the value of your encrypted card.
- **Response**:
  - **Error**: `{"message": "Error adding card", "success": false}`
  - **Success**: Example response: `{"message": "Card added with success", "success": true}`

## Author

- Paulo Cesar Camilo
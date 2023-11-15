# Cordova Plugin for Samsung Pay

This repository contains a Cordova plugin for integrating Samsung Pay into mobile applications.

## Installation

To install the plugin, use the following command in your Cordova project:

```bash
cordova plugin add https://github.com/os-adv-dev/cordova-plugin-samsungpay.git --variable APP_SERVICE_ID="YOUR_APP_SERVICE_ID" --variable APP_DEBUG_MODE="Y or N" --variable APP_DEBUG_API_KEY="YOUR_DEBUG_API_KEY" --variable APP_ISSUER_NAME="YOUR_ISSUER_NAME" --variable APP_SERVICE_TYPE="YOUR_SERVICE_TYPE"
```

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
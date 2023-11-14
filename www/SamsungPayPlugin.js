var exec = require('cordova/exec');

exports.checkDeviceSupport = function (success, error) {
    exec(success, error, 'SamsungPayPlugin', 'checkDeviceSupport');
};

exports.getWalletInfo = function (success, error) {
    exec(success, error, 'SamsungPayPlugin', 'getWalletInfo');
};

exports.getAllCards = function (success, error) {
    exec(success, error, 'SamsungPayPlugin', 'getAllCards');
};

exports.addCard = function (success, error, args) {
    exec(success, error, 'SamsungPayPlugin', 'addCard', [args]);
};
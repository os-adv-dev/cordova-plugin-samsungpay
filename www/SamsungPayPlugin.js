var exec = require('cordova/exec');

exports.checkDeviceSupport = function (success, error) {
    exec(success, error, 'SamsungPayPlugin', 'checkDeviceSupport');
};

exports.addCard = function (success, error, args) {
    exec(success, error, 'SamsungPayPlugin', 'addCard', [args]);
};
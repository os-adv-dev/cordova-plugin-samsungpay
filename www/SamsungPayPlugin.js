var exec = require('cordova/exec');

exports.checkDeviceSupport = function (success, error, args) {
    exec(success, error, 'SamsungPayPlugin', 'checkDeviceSupport', [args]);
};

exports.addCard = function (success, error, args) {
    exec(success, error, 'SamsungPayPlugin', 'addCard', [args]);
};
package com.outsystems.experts.samsungpay

enum class SamsungPayErrors(val message: String) {
    NOT_SUPPORTED("Samsung Pay is not supported!"),
    IS_NOT_READY("Samsung Pay is not ready!"),
    IS_NOT_ALLOWED("Samsung Pay is not allowed temporally!"),
    IS_READY("Samsung Pay is ready!"),
    ADD_CARD_ERROR_INIT("You should call the checkDeviceSupport action before calling AddCard action and the Samsung Pay should be READY!")
}
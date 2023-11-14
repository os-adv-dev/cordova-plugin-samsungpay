package com.outsystems.experts.samsungpay

import android.os.Build
import android.os.Bundle
import android.util.Log
import com.google.gson.Gson
import com.samsung.android.sdk.samsungpay.v2.PartnerInfo
import com.samsung.android.sdk.samsungpay.v2.SamsungPay
import com.samsung.android.sdk.samsungpay.v2.SpaySdk
import com.samsung.android.sdk.samsungpay.v2.StatusListener
import com.samsung.android.sdk.samsungpay.v2.card.AddCardInfo
import com.samsung.android.sdk.samsungpay.v2.card.AddCardListener
import com.samsung.android.sdk.samsungpay.v2.card.Card
import com.samsung.android.sdk.samsungpay.v2.card.CardManager
import com.samsung.android.sdk.samsungpay.v2.card.GetCardListener
import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaInterface
import org.apache.cordova.CordovaPlugin
import org.apache.cordova.CordovaWebView
import org.apache.cordova.PluginResult
import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable


private const val TAG = "SamsungPayPlugin"
private const val CHECK_DEVICE_SUPPORT = "checkDeviceSupport"
private const val GET_WALLET_INFO = "getWalletInfo"
private const val GET_ALL_CARDS = "getAllCards"
private const val ADD_CARD = "addCard"

class SamsungPayPlugin : CordovaPlugin() {

    private var isSpayReady = false
    private lateinit var samsungPay: SamsungPay

    override fun initialize(cordova: CordovaInterface?, webView: CordovaWebView?) {
        super.initialize(cordova, webView)

        val serviceId = cordova?.activity?.getString(cordova.activity.resources.getIdentifier("app_service_id", "string", cordova.activity.packageName))
        val bundle = Bundle()
        bundle.putString(SamsungPay.EXTRA_ISSUER_NAME, "Al Salam Bank")
        bundle.putString(SamsungPay.PARTNER_SERVICE_TYPE, SpaySdk.ServiceType.APP2APP.toString())

        val partnerInfo = PartnerInfo(serviceId, bundle)
        samsungPay = SamsungPay(this.cordova.context, partnerInfo)
    }

    override fun execute(
        action: String,
        args: JSONArray,
        callbackContext: CallbackContext
    ): Boolean {

        if (action == ADD_CARD) {
            if ((args.get(0) as CharSequence).isEmpty()) {
                val result = JSONObject().apply { setJsonResult("encCard data argument not found or empty!", false) }
                callbackContext.error(result)
            } else {
                val cardData = args.getString(0)
                this.requestAddCard(callbackContext, cardData)
            }
            return true
        }

        if (action == CHECK_DEVICE_SUPPORT) {
            this.checkDeviceSupport(callbackContext)
            return true
        }

        if (action == GET_WALLET_INFO) {
            this.requestGetWalletInfo(callbackContext)
            return true
        }

        if (action == GET_ALL_CARDS) {
            this.requestGetAllCards(callbackContext)
            return true
        }

        return false
    }

    private fun requestGetAllCards(callbackContext: CallbackContext) {
        if (!isSpayReady) {
            val result = JSONObject().apply {
                put("message", SamsungPayErrors.ADD_CARD_ERROR_INIT.message)
                put("success", false)
            }
            sendErrorResult(callbackContext, result)
        } else {
            getAllCards(callbackContext)
        }
    }

    private fun getAllCards(callbackContext: CallbackContext) {
        val getCardListener: GetCardListener = object : GetCardListener {
            override fun onSuccess(cards: List<Card>) {
                val cardsData = mutableListOf<CardInfo>()
                if (cards.isEmpty()) {
                    sendSuccessResultArray(callbackContext, cardsData)
                    return
                } else {
                    for (s in cards) {
                        if (s.cardInfo != null) {
                            val card = CardInfo(
                                cardId = s.cardId,
                                last4FPan = s.cardInfo.getString(CardManager.EXTRA_LAST4_FPAN),
                                last4DPan = s.cardInfo.getString(CardManager.EXTRA_LAST4_DPAN),
                                cardType = s.cardInfo.getString(CardManager.EXTRA_CARD_TYPE),
                                cardIssuerName = s.cardInfo.getString(CardManager.EXTRA_ISSUER_NAME)
                            )
                            cardsData.add(card)
                        }
                    }
                    sendSuccessResultArray(callbackContext, cardsData)
                }
            }

            override fun onFail(errorCode: Int, errorData: Bundle) {
                val jsonResult = JSONObject().apply { setJsonResult("Error to getAllCards", false) }
                sendErrorResult(callbackContext, jsonResult)
            }
        }

        val serviceId = cordova?.activity?.getString(cordova.activity.resources.getIdentifier("app_service_id", "string", cordova.activity.packageName))
        val bundle = Bundle()
        bundle.putString(SamsungPay.PARTNER_SERVICE_TYPE, SpaySdk.ServiceType.APP2APP.toString())
        val pInfo = PartnerInfo(serviceId, bundle)

        val cardManager = CardManager(this.cordova.context, pInfo)
        val cardFilter = Bundle()
        cardFilter.putString(SamsungPay.EXTRA_ISSUER_NAME, "Al Salam Bank")
        cardManager.getAllCards(cardFilter, getCardListener)
    }

    private fun checkDeviceSupport(callbackContext: CallbackContext) {
        val jsonResult = JSONObject()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            jsonResult.setJsonResult(SamsungPayErrors.NOT_SUPPORTED.message, false)
            callbackContext.error(jsonResult)
        } else {
            val statusListener: StatusListener = object : StatusListener {
                override fun onSuccess(status: Int, bundle: Bundle) {
                    isSpayReady = status == SpaySdk.SPAY_READY
                    setCallbackOnSuccessStatusListener(jsonResult, status, callbackContext)
                }

                override fun onFail(errorCode: Int, bundle: Bundle) {
                    isSpayReady = false
                    jsonResult.setJsonResult("Error to check samsung pay status", false)
                    sendErrorResult(callbackContext, jsonResult)
                }
            }

            samsungPay.getSamsungPayStatus(statusListener)
        }
    }

    private fun setCallbackOnSuccessStatusListener(jsonResult: JSONObject, status: Int, callback: CallbackContext) {
        when(status) {
            SamsungPay.SPAY_NOT_SUPPORTED -> {
                jsonResult.setJsonResult(SamsungPayErrors.NOT_SUPPORTED.message, false)
                callback.error(jsonResult)
            }
            SamsungPay.SPAY_NOT_ALLOWED_TEMPORALLY -> {
                jsonResult.setJsonResult(SamsungPayErrors.IS_NOT_ALLOWED.message, false)
                callback.error(jsonResult)
            }
            SamsungPay.SPAY_NOT_READY -> {
                jsonResult.setJsonResult(SamsungPayErrors.IS_NOT_READY.message, false)
                callback.error(jsonResult)
            }
            SamsungPay.SPAY_READY -> {
                jsonResult.setJsonResult(SamsungPayErrors.IS_READY.message,  true)
                callback.success(jsonResult)
            }
        }
    }

    private fun requestAddCard(callbackContext: CallbackContext, cardInfo: String) {
        if (!isSpayReady) {
            val result = JSONObject().apply {
                put("message", SamsungPayErrors.ADD_CARD_ERROR_INIT.message)
                put("success", false)
            }
            sendErrorResult(callbackContext, result)
        } else {
            prepareAddCardToWallet(callbackContext, cardInfo)
        }
    }

    private fun requestGetWalletInfo(callbackContext: CallbackContext) {
        if (!isSpayReady) {
            val result = JSONObject().apply {
                put("message", SamsungPayErrors.ADD_CARD_ERROR_INIT.message)
                put("success", false)
            }
            sendErrorResult(callbackContext, result)
        } else {
            getWalletInfo(callbackContext)
        }
    }

    private fun getWalletInfo(callbackContext: CallbackContext) {
        val keys = ArrayList<String>()
        keys.add(SamsungPay.WALLET_DM_ID)
        keys.add(SamsungPay.DEVICE_ID)
        keys.add(SamsungPay.WALLET_USER_ID)
        val statusListener: StatusListener = object : StatusListener {
            override fun onSuccess(status: Int, walletData: Bundle) {
                // VISA requires DEVICE_ID for clientDeviceId, WALLET_USER_ID for walletAccountId. Please refer VISA spec document.
                val clientDeviceId: String? = walletData.getString(SamsungPay.DEVICE_ID)
                val clientWalletAccountId: String? = walletData.getString(SamsungPay.WALLET_USER_ID)

                val result = JSONObject()
                if (clientDeviceId == null && clientWalletAccountId == null) {
                    result.apply {
                        put("message", "Error to get the getWalletInfo (clientDeviceId & walletAccountId)")
                        put("success", false)
                    }
                } else {
                    result.apply {
                        put("clientDeviceId", clientDeviceId)
                        put("walletAccountId", clientWalletAccountId)
                    }
                }

                sendSuccessResult(callbackContext, result)
            }

            override fun onFail(errorCode: Int, errorData: Bundle?) {
                val result = JSONObject()
                val messageError = errorData?.getString(SpaySdk.EXTRA_ERROR_REASON_MESSAGE) ?: "Error to get walletInfo"

                result.apply {
                    put("message", messageError)
                    put("success", false)
                }

                sendErrorResult(callbackContext, result)
            }
        }

        val serviceId = cordova?.activity?.getString(cordova.activity.resources.getIdentifier("app_service_id", "string", cordova.activity.packageName))
        val bundle = Bundle()
        bundle.putString(SamsungPay.EXTRA_ISSUER_NAME, "Al Salam Bank")
        bundle.putString(SamsungPay.PARTNER_SERVICE_TYPE, SpaySdk.ServiceType.APP2APP.toString())
        val pInfo = PartnerInfo(serviceId, bundle)
        val samsungPay = SamsungPay(this.cordova.context, pInfo)
        samsungPay.getWalletInfo(keys, statusListener)
    }

    private fun prepareAddCardToWallet(
        callbackContext: CallbackContext,
        payloadEncrypted: String
    ) {
        val tokenizationProvider = AddCardInfo.PROVIDER_VISA
        val cardType = Card.CARD_TYPE_DEBIT
        val cardDetail = Bundle()
        cardDetail.putBoolean(AddCardInfo.EXTRA_SAMSUNG_PAY_CARD, false)
        cardDetail.putString(AddCardInfo.EXTRA_PROVISION_PAYLOAD, payloadEncrypted)

        val addCardInfo = AddCardInfo(cardType, tokenizationProvider, cardDetail)

        val addCardListener: AddCardListener = object : AddCardListener {
            override fun onSuccess(status: Int, card: Card) {
                val result = JSONObject().apply {
                    put("message", "Card added successfully!")
                    put("success", true)
                }
                sendSuccessResult(callbackContext, result)
            }

            override fun onFail(errorCode: Int, errorData: Bundle) {
                val result = JSONObject()
                val errorName = ErrorCode.getErrorCodeName(errorCode)
                val messageError = errorData.getString(SpaySdk.EXTRA_ERROR_REASON_MESSAGE) ?: "Error msg: $errorName - $errorCode"

                result.apply {
                    put("message", messageError)
                    put("success", false)
                }

                sendErrorResult(callbackContext, result)
            }

            override fun onProgress(currentCount: Int, totalCount: Int, bundleData: Bundle) {
                Log.d(TAG, "doAddCard onProgress : $currentCount / $totalCount")
            }
        }

        val serviceId = cordova?.activity?.getString(cordova.activity.resources.getIdentifier("app_service_id", "string", cordova.activity.packageName))
        val bundle = Bundle()
        bundle.putString(SamsungPay.EXTRA_ISSUER_NAME, "Al Salam Bank")
        bundle.putString(SamsungPay.PARTNER_SERVICE_TYPE, SpaySdk.ServiceType.APP2APP.toString())

        val partnerInfo = PartnerInfo(serviceId, bundle)
        val cardManager = CardManager(this.cordova.context, partnerInfo)
        cardManager.addCard(addCardInfo, addCardListener)
    }

    private fun sendSuccessResult(callbackContext: CallbackContext, jsonObject: JSONObject) {
        val result = PluginResult(PluginResult.Status.OK, jsonObject)
        callbackContext.sendPluginResult(result)
    }

    private fun sendSuccessResultArray(callbackContext: CallbackContext, data: List<CardInfo>) {
        val jsonResult = Gson().toJson(data)
        val result = PluginResult(PluginResult.Status.OK, jsonResult)
        callbackContext.sendPluginResult(result)
    }

    private fun sendErrorResult(callbackContext: CallbackContext, jsonObject: JSONObject) {
        val result = PluginResult(PluginResult.Status.ERROR, jsonObject)
        callbackContext.sendPluginResult(result)
    }
}

// Used to able to se message and available to response callback plugin
private fun JSONObject.setJsonResult(message: String, success: Boolean) {
    put("message", message)
    put("success", success)
}

data class CardInfo(
    val cardId: String?,
    val last4FPan: String?,
    val last4DPan: String?,
    val cardType: String?,
    val cardIssuerName: String?
)
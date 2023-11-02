package com.outsystems.experts.samsungpay;

import android.os.Build
import android.os.Bundle
import android.util.Log
import com.samsung.android.sdk.samsungpay.v2.PartnerInfo
import com.samsung.android.sdk.samsungpay.v2.SamsungPay
import com.samsung.android.sdk.samsungpay.v2.SpaySdk
import com.samsung.android.sdk.samsungpay.v2.StatusListener
import com.samsung.android.sdk.samsungpay.v2.card.AddCardInfo
import com.samsung.android.sdk.samsungpay.v2.card.AddCardListener
import com.samsung.android.sdk.samsungpay.v2.card.Card
import com.samsung.android.sdk.samsungpay.v2.card.CardManager
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView
import org.apache.cordova.PluginResult
import org.json.JSONArray;
import org.json.JSONObject

private const val TAG = "SamsungPayPlugin"
private const val CHECK_DEVICE_SUPPORT = "checkDeviceSupport"
private const val ADD_CARD = "addCard"

class SamsungPayPlugin : CordovaPlugin() {

    private var isSpayReady = false
    private lateinit var samsungPay: SamsungPay
    private lateinit var cardManager: CardManager

    override fun initialize(cordova: CordovaInterface?, webView: CordovaWebView?) {
        super.initialize(cordova, webView)

        val serviceId = cordova?.activity?.getString(cordova.activity.resources.getIdentifier("app_service_id", "string", cordova.activity.packageName))
        val bundle = Bundle()

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
            val argCardInfo = (args.get(0) as JSONObject).getString("cardInfo")

            if (argCardInfo.isNullOrEmpty()) {
                val result = JSONObject().apply { setJsonResult("CardIndo argument not found!", false) }
                callbackContext.error(result)
            } else {
                this.addCard(callbackContext, argCardInfo)
            }

            return true
        }

        if (action == CHECK_DEVICE_SUPPORT) {
            this.checkDeviceSupport(callbackContext)
            return true
        }

        return false
    }

    private fun checkDeviceSupport(callbackContext: CallbackContext) {
        val jsonResult = JSONObject()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            //Hide Samsung Pay button in this Android version
            jsonResult.setJsonResult(SamsungPayErrors.NOT_SUPPORTED.message, false)
            callbackContext.error(jsonResult)
        } else {
            val statusListener: StatusListener = object : StatusListener {
                override fun onSuccess(status: Int, bundle: Bundle) {
                    Log.d(TAG, "onSuccess callback is called, status=$status,bundle:$bundle")
                    isSpayReady = status == SpaySdk.SPAY_READY // update ready status
                    Log.v(TAG, "Status: $status - bundle: $bundle")
                    setCallbackOnSuccessStatusListener(jsonResult, status, callbackContext)
                }

                override fun onFail(errorCode: Int, bundle: Bundle) {
                    Log.d(TAG, "onFail callback is called, i=$errorCode,bundle:$bundle")
                    isSpayReady = false
                    Log.v(TAG, "errorCode: $errorCode - bundle: $bundle")
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

    private fun addCard(callbackContext: CallbackContext, cardInfo: String) {
        if (!isSpayReady) {
            val result = JSONObject().apply {
                put("message", SamsungPayErrors.ADD_CARD_ERROR_INIT.message)
                put("success", false)
            }
            sendErrorResult(callbackContext, result)
        } else {
            val serviceId = cordova?.activity?.getString(cordova.activity.resources.getIdentifier("app_service_id", "string", cordova.activity.packageName))
            val partnerInfo = PartnerInfo(serviceId)
            cardManager = CardManager(this.cordova.context, partnerInfo)
            prepareAddCardToWallet(cardManager, callbackContext, cardInfo)
        }
    }

    private fun prepareAddCardToWallet(
        cardManager: CardManager,
        callbackContext: CallbackContext,
        cardInfo: String
    ) {
        val tokenizationProvider = ""
        val payload = ""
        val cardType = Card.CARD_TYPE_CREDIT_DEBIT
        val cardDetail = Bundle()
        cardDetail.putString(AddCardInfo.EXTRA_PROVISION_PAYLOAD, payload) // encrypted payload

        Log.d(TAG, "addCard payload : $payload, tokenizationProvider : $tokenizationProvider")

        val addCardInfo = AddCardInfo(cardType, tokenizationProvider, cardDetail)

        val addCardListener: AddCardListener = object : AddCardListener {
            override fun onSuccess(status: Int, card: Card) {
                Log.d(TAG, "doAddCard onSuccess callback is called")
            }

            override fun onFail(errorCode: Int, errorData: Bundle) {
                Log.d(TAG, "doAddCard onFail callback is called, errorCode:$errorCode")
                if (errorData.containsKey(SpaySdk.EXTRA_ERROR_REASON_MESSAGE)) {
                    Log.e(
                        TAG,
                        "doAddCard onFail extra reason message: ${errorData.getString(SpaySdk.EXTRA_ERROR_REASON_MESSAGE)}"
                    )
                }
            }

            override fun onProgress(currentCount: Int, totalCount: Int, bundleData: Bundle) {
                Log.d(TAG, "doAddCard onProgress : $currentCount / $totalCount")
            }
        }

        cardManager.addCard(addCardInfo, addCardListener)
        /**
         *  val result = JSONObject().apply {
         *                 put("message", "Card added successfully!")
         *                 put("success", true)
         *             }
         *             sendSuccessResult(callbackContext, result)
         */
    }

    private fun sendSuccessResult(callbackContext: CallbackContext, jsonObject: JSONObject) {
        val result = PluginResult(PluginResult.Status.OK, jsonObject)
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
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
import java.util.ArrayList

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

            if (args.get(0) != null) {
                val result = JSONObject().apply { setJsonResult("CardIndo argument not found!", false) }
                callbackContext.error(result)
            } else {
                val cardData = args.getString(0)
                this.addCard(callbackContext, cardData)
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
            requestGetWalletInfo(callbackContext, cardInfo)
        }
    }

    private fun requestGetWalletInfo(callbackContext: CallbackContext, cardInfo: String) {
        val keys = ArrayList<String>()
        keys.add(SamsungPay.WALLET_DM_ID)
        keys.add(SamsungPay.DEVICE_ID)
        keys.add(SamsungPay.WALLET_USER_ID)
        val statusListener: StatusListener = object : StatusListener {
            override fun onSuccess(status: Int, walletData: Bundle) {
                // VISA requires DEVICE_ID for deviceID, WALLET_USER_ID for walletAccountId. Please refer VISA spec document.
                val clientDeviceId: String? = walletData.getString(SamsungPay.DEVICE_ID)
                val clientWalletAccountId: String? = walletData.getString(SamsungPay.WALLET_USER_ID)
                // Set clientDeviceId & clientWalletAccountId with values from Samsung Pay

                val payloadEncrypted = "eyJ0eXAiOiJKT1NFIiwiZW5jIjoiQTI1NkdDTSIsImlhdCI6MTY5OTgyNTUyOCwiYWxnIjoiUlNBLU9BRVAtMjU2Iiwia2lkIjoiNzUxYzA0ZmMtMGY0NS00MWQzLTljZjAtZGI2Zjk1ZGU2NzVlIn0.XYEt8Up-EuLGGjZGdazaKhAG50jFZD917tKY1HYvGQjtSlCp3oN9hMO0UQM-w5NzSJZgu6Xx3oUxv2vAh4rFkTzr8sjC4fqklJy05iPyRx5nlay4JwEsqqk6-ixuN4VLjCwbVXCqF9ZRkwnirqyONMu5Td9ggW9LsHqXsV7YEnSmXpRqrcfxrMTChp2oO5d5dkgJhSl46N79PmZxACTI__RMtrRjXf4tmVPf3Mn_B25J_pG8GuNEtRHaqd0oS_kUcn3FtyMSqVnIMjvznAmDdJ6iwjejYD2yogmzO1OeVY5AU-YxI4KUsi3N0Gvg2v9vVDFe_t4Vl30iOWS4xV64zg.7g5EhiS5ZDVsE-Lx.JHi7gtA0kQ6JeT9RMwQnl5ip2CDSZuMwq17NNpCE8O2TLZe3hNkLcnBJ0mekCXkOpRqYzMgCqNf9mB0ZaA-IqJ6Pt2N26ly-U-pdhDDjG5U0n9RP9mo9UQZeTf68nICDNqDl1JusaexAK0bAbFR8T82Oe2_HVp7oEB_FcK-HOldC2cNrQMlvq5sAyuRLifjjSplFmVA7snqklu_gIyF9sr0k3No9FNnrAka7RsCFxWd3q5BC6jVAzBCPBMVmB47K986pMs64SRSdT9zVGfVOq-1273U53CIxHSKNxO_pxC-MFp97BGWH5bpsiCbBYUig3qWFSboIIJuRj5jBr5aLEQRIzT3bE_Fr_EPTkxA_C75axHCXQboG21jWKfx1SBWaHZZwG1QzhRVagMbiEdgHXKBssQ_NyGZqV3UmGomQ0bYocLrEXCnKaVmJHrTSTW7mqocJnl6N_8g5eB-pPs-lgPUbvqgCC35Cg6JLYv8T6Xp4s3_PfioCwA86iNpPf6yiAijMX7Ul8MZ0EhsdRH5WIjlujTHG2Wr02IB5Ob9byw.f9tV3hoHkHBwcFvA4L9qtg"

                prepareAddCardToWallet(cardManager, callbackContext, payloadEncrypted)
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

        samsungPay.getWalletInfo(keys, statusListener)
    }

    private fun prepareAddCardToWallet(
        cardManager: CardManager,
        callbackContext: CallbackContext,
        payloadEncrypted: String
    ) {
        val tokenizationProvider = AddCardInfo.PROVIDER_VISA
        val cardType = Card.CARD_TYPE_CREDIT_DEBIT
        val cardDetail = Bundle()
        cardDetail.putString(AddCardInfo.EXTRA_PROVISION_PAYLOAD, payloadEncrypted)

        Log.d(TAG, "addCard payload : $payloadEncrypted, tokenizationProvider : $tokenizationProvider")

        val addCardInfo = AddCardInfo(cardType, tokenizationProvider, cardDetail)

        val addCardListener: AddCardListener = object : AddCardListener {
            override fun onSuccess(status: Int, card: Card) {
                Log.d(TAG, "doAddCard onSuccess callback is called")
                val result = JSONObject().apply {
                    put("message", "Card added successfully!")
                    put("success", true)
                }
                sendSuccessResult(callbackContext, result)
            }

            override fun onFail(errorCode: Int, errorData: Bundle) {
                val result = JSONObject()
                val messageError = errorData.getString(SpaySdk.EXTRA_ERROR_REASON_MESSAGE) ?: "Error to get walletInfo"

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

        cardManager.addCard(addCardInfo, addCardListener)
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
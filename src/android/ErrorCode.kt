package com.outsystems.experts.samsungpay

import android.util.Log
import android.util.SparseArray
import com.samsung.android.sdk.samsungpay.v2.SpaySdk
import com.samsung.android.sdk.samsungpay.v2.payment.PaymentManager
import com.samsung.android.sdk.samsungpay.v2.card.CardManager
import com.samsung.android.sdk.samsungpay.v2.AppToAppConstants
import com.samsung.android.sdk.samsungpay.v2.payment.MstManager
import com.samsung.android.sdk.samsungpay.v2.WatchManager
import java.lang.Exception

object ErrorCode {
    val TAG = ErrorCode::class.simpleName
    val errorCodeMap = SparseArray<String>()
    public fun createErrorCodeMap(c: Class<*>) {
        val fields = c.declaredFields
        for (fld in fields) {
            if (fld.type == Int::class.javaPrimitiveType) {
                try {
                    val v = fld.getInt(null)
                    val name = fld.name
                    if (name.startsWith("ERROR_") && v != SpaySdk.ERROR_NONE || name.startsWith("SPAY_")) {
                        // ERROR_NONE is not an error.
                        errorCodeMap.put(v, name)
                        println("❌ - Error code: $v -- Error name: $name")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "createErrorCodeMap - e : ${e.message}")
                }
            }
        }
    }

    public fun getErrorCodeName(i: Int): String {
        return errorCodeMap[i]
    }

    init {
        createErrorCodeMap(SpaySdk::class.java)
        createErrorCodeMap(PaymentManager::class.java)
        createErrorCodeMap(CardManager::class.java)
        createErrorCodeMap(AppToAppConstants::class.java)
        createErrorCodeMap(MstManager::class.java)
        createErrorCodeMap(WatchManager::class.java)
    }
}
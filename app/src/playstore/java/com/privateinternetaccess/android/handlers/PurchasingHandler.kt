package com.privateinternetaccess.android.handlers

import android.app.Activity
import com.android.billingclient.api.*
import com.privateinternetaccess.android.pia.PIAFactory
import com.privateinternetaccess.android.pia.interfaces.IPurchasing
import com.privateinternetaccess.android.pia.model.PurchaseData
import com.privateinternetaccess.android.pia.model.PurchaseObj
import com.privateinternetaccess.android.pia.model.enums.PurchasingType
import com.privateinternetaccess.android.pia.model.events.PurchasingInfoEvent
import com.privateinternetaccess.android.pia.model.events.SystemPurchaseEvent
import com.privateinternetaccess.android.pia.utils.DLog
import com.privateinternetaccess.core.utils.IPIACallback
import org.greenrobot.eventbus.EventBus

/**
 * Created by hfrede on 11/30/17.
 */
class PurchasingHandler : PurchasesUpdatedListener, IPurchasing {
    private var activity: Activity? = null
    private var availableProducts: List<String> = emptyList()
    private val productMap = mutableMapOf<String, ProductDetails>()
    private var mBillingClient: BillingClient? = null
    private var systemCallback: IPIACallback<SystemPurchaseEvent>? = null
    private var mIsServiceConnected = false
    private var mBillingClientResponseCode = 0
    private var purchase: Purchase? = null

    override fun init(
        activity: Activity,
        productsList: List<String>,
        systemCallback: IPIACallback<SystemPurchaseEvent>,
        eventBus: EventBus
    ) {
        this.activity = activity
        availableProducts = productsList
        this.systemCallback = systemCallback

        mBillingClient = BillingClient.newBuilder(activity)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        startServiceConnection {
            if (mBillingClientResponseCode == BillingClient.BillingResponseCode.OK) {
                // The billing client is ready. You can query purchases here.
                DLog.d(TAG, "Billing setup succeed")
                grabPurchases(eventBus)
            } else {
                DLog.d(TAG, "Billing setup failed $mBillingClientResponseCode")
            }
        }
    }

    private fun startServiceConnection(executable: Runnable?) {
        mBillingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                val billingResponseCode = billingResult.responseCode
                DLog.d(TAG, "Setup finished. Response code: $billingResponseCode")
                if (billingResponseCode == BillingClient.BillingResponseCode.OK) {
                    mIsServiceConnected = true
                    executable?.run()
                }
                mBillingClientResponseCode = billingResponseCode
            }

            override fun onBillingServiceDisconnected() {
                mIsServiceConnected = false
            }
        })
    }

    private fun executeServiceRequest(runnable: Runnable) {
        if (mIsServiceConnected) {
            runnable.run()
        } else {
            startServiceConnection(runnable)
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        var success = false
        var returnString = ""
        val responseCode = billingResult.responseCode
        if (responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            var purchase: Purchase? = null
            for (p in purchases) {
                purchase = p
                break
            }
            if (purchase != null) {
                DLog.d(TAG, "Purchases call completed")
                DLog.d(TAG, "purchases = $purchases")
                DLog.i(TAG, "Purchase successful.")
                DLog.d(TAG, "Purchase token: '" + purchase.purchaseToken + "'.")
                DLog.d(TAG, "Purchase order id: '" + purchase.orderId + "'.")
                savePurchaseForProcess(purchase)
                success = true
                returnString = purchase.products[0]
            }
        } else if (responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            DLog.d(TAG, "User Canceled Purchasing")
        } else {
            // Handle any other error codes.
            DLog.d(TAG, "Purchasing failed with this code($responseCode)")
        }
        sendBackSystemPurchase(success, returnString)
    }

    private fun sendBackSystemPurchase(b: Boolean, sku: String) {
        val event = SystemPurchaseEvent(b, sku)
        if (systemCallback != null) systemCallback!!.apiReturn(event)
    }

    private fun grabPurchases(eventBus: EventBus) {
        val runnable = Runnable {
            if (availableProducts.isEmpty()) {
                DLog.d(TAG, "You must enter a purchaseList for PurchasingHandler to work.")
                return@Runnable
            }
            val queryProductDetailsParams =
                QueryProductDetailsParams.newBuilder()
                    .setProductList(createProductsListForQuery())
                    .build()

            mBillingClient?.queryProductDetailsAsync(queryProductDetailsParams) { billingResult,
                                                                                  productDetailsList ->
                if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    DLog.d(TAG, "SkuDetails just aren't happening")
                    return@queryProductDetailsAsync
                }
                DLog.d(TAG, "SkuDetails have arrived")
                for (details in productDetailsList) {
                    productMap[details.productId] = details
                }
                val event = PurchasingInfoEvent(productMap)
                eventBus.post(event)
            }
        }
        executeServiceRequest(runnable)
    }

    private fun savePurchaseForProcess(purchase: Purchase) {
        val data = PurchaseData(
            purchase.purchaseToken,
            purchase.products[0],
            purchase.orderId
        )
        val account = PIAFactory.getInstance().getAccount(activity)
        account.saveTemporaryPurchaseData(data)
    }

    override fun purchase(productId: String) {
        val runnable = Runnable {
            if (mBillingClient != null) {
                activity?.let { currentActivity ->
                    productMap[productId]?.let { product ->
                        val billingFlowParams = BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(createProductQuery(product))
                            .build()

                        mBillingClient?.launchBillingFlow(currentActivity, billingFlowParams)
                    }
                }
            } else {
                DLog.d(TAG, "purchase is no longer valid at this time.")
            }
        }
        executeServiceRequest(runnable)
    }

    override fun getProductDetails(productId: String?): ProductDetails? {
        return productMap[productId]
    }

    override fun getPurchase(savePurchase: Boolean, eventBus: EventBus) {
        if (mBillingClient != null) {
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)

            mBillingClient?.queryPurchasesAsync(
                params.build()
            ) { p0, list ->
                for (p in list) {
                    if (availableProducts.contains(p.products[0])) {
                        purchase = p
                        break
                    }
                }

                purchase?.let {
                    if (savePurchase) {
                        savePurchaseForProcess(purchase!!)
                    }
                    eventBus.post(convertToPurchaseObject(purchase!!))
                    return@queryPurchasesAsync
                }?: run {
                    eventBus.post(PurchaseObj(null, null, null, 0, null))
                    return@queryPurchasesAsync
                }
            }
        } else {
            eventBus.post(PurchaseObj(null, null, null, 0, null))
            DLog.d(TAG, "getPurchase is no longer valid at this time.")
        }
    }

    private fun convertToPurchaseObject(purchase: Purchase): PurchaseObj {
        return PurchaseObj(
            purchase.orderId,
            purchase.packageName,
            purchase.products[0],
            purchase.purchaseTime,
            purchase.purchaseToken
        )
    }

    override fun dispose() {
        DLog.d(TAG, "disposed")
        try {
            if (mBillingClient != null) mBillingClient!!.endConnection()
            mBillingClient = null
            activity = null
        } catch (e: Exception) {
        }
    }

    override fun getType(): PurchasingType {
        return PurchasingType.GOOGLE
    }

    private fun createProductsListForQuery(): List<QueryProductDetailsParams.Product> {
        val result = mutableListOf<QueryProductDetailsParams.Product>()
        for (product in availableProducts) {
            result.add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(product)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )
        }
        return result
    }

    private fun createProductQuery(product: ProductDetails): List<BillingFlowParams.ProductDetailsParams> {
        val result = mutableListOf<BillingFlowParams.ProductDetailsParams>()
        val offerToken = product.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: ""
        result.add(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(product)
                .setOfferToken(offerToken)
                .build()
        )
        return result
    }

    companion object {
        const val TAG = "PurchasingHandler"
    }
}
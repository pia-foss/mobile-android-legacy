package com.privateinternetaccess.android.utils

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.amazon.device.iap.PurchasingListener
import com.amazon.device.iap.PurchasingService
import com.amazon.device.iap.model.*
import com.privateinternetaccess.android.model.events.PricingLoadedEvent
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler
import com.privateinternetaccess.android.pia.model.AmazonPurchaseData

class AmazonPurchaseUtil(private val context: Context) {

    private val monthlySkuPlan = "PIA-M1"
    private val yearlySkuPlan = "PIA-Y1"
    private val products = setOf(monthlySkuPlan, yearlySkuPlan)

    val observableProducts = MutableLiveData<PricingLoadedEvent?>()
    val observablePurchase = MutableLiveData<PurchaseResponse?>()
    val observableData = MutableLiveData<AmazonPurchaseData>()

    private var selectedProduct: String? = null

    init {
        PurchasingService.registerListener(context, object : PurchasingListener {
            override fun onUserDataResponse(userData: UserDataResponse?) {
                // currently do nothing
            }

            override fun onProductDataResponse(productData: ProductDataResponse?) {
                productData?.let {
                    observableProducts.postValue(PricingLoadedEvent(
                        it.productData[monthlySkuPlan]!!.price,
                        it.productData[yearlySkuPlan]!!.price
                    ))
                }
            }

            override fun onPurchaseResponse(purchase: PurchaseResponse?) {
                observablePurchase.postValue(purchase)
            }

            override fun onPurchaseUpdatesResponse(purchaseUpdate: PurchaseUpdatesResponse?) {
                purchaseUpdate?.let {
                    observableData.postValue(AmazonPurchaseData(it.userData.userId, it.receipts[0].receiptId))
                }
            }
        })
    }

    fun loadProducts() {
        PurchasingService.getProductData(products)
    }

    fun purchaseSelectedProduct() {
        PurchasingService.purchase(selectedProduct)
    }

    fun selectProduct(isYearly: Boolean) {
        selectedProduct = if (isYearly) {
            yearlySkuPlan
        } else {
            monthlySkuPlan
        }
    }

    fun getSelectedProductId(): String? {
        val subscriptions = PiaPrefHandler.availableSubscriptions(context)
        var subscriptionId: String? = null
        for (product in subscriptions.availableProducts) {
            if (!product.legacy && product.plan == selectedProduct?.lowercase()) {
                subscriptionId = product.id
                break
            }
        }
        return subscriptionId
    }

    fun getPurchaseUpdates() {
        PurchasingService.getPurchaseUpdates(false)
    }

}
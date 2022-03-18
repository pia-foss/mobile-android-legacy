package com.privateinternetaccess.android.utils

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.amazon.device.iap.PurchasingListener
import com.amazon.device.iap.PurchasingService
import com.amazon.device.iap.model.ProductDataResponse
import com.amazon.device.iap.model.PurchaseResponse
import com.amazon.device.iap.model.PurchaseUpdatesResponse
import com.amazon.device.iap.model.UserDataResponse
import com.privateinternetaccess.android.model.events.PricingLoadedEvent
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler
import org.greenrobot.eventbus.EventBus

class AmazonPurchaseUtil(private val context: Context) {

    private val monthlyPlan = "MONTHLY"
    private val yearlyPlan = "YEARLY"
    private val products = hashSetOf(monthlyPlan, yearlyPlan, "PIA-M1", "PIA-Y1")

    val observableProducts = MutableLiveData<PricingLoadedEvent?>()
    val observablePurchase = MutableLiveData<PurchaseResponse?>()

    private var selectedProduct: String? = null

    init {
        PurchasingService.registerListener(context, object : PurchasingListener {
            override fun onUserDataResponse(userData: UserDataResponse?) {
                TODO("Not yet implemented")
            }

            override fun onProductDataResponse(productData: ProductDataResponse?) {
                productData?.let {
                    observableProducts.postValue(PricingLoadedEvent(
                        it.productData[monthlyPlan]!!.price,
                        it.productData[yearlyPlan]!!.price
                    ))
                }
            }

            override fun onPurchaseResponse(purchase: PurchaseResponse?) {
                observablePurchase.postValue(purchase)
            }

            override fun onPurchaseUpdatesResponse(purchaseUpdate: PurchaseUpdatesResponse?) {
                TODO("Not yet implemented")
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
            yearlyPlan
        } else {
            monthlyPlan
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

}
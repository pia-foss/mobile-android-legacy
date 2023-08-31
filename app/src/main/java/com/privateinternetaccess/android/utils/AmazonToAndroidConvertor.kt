package com.privateinternetaccess.android.utils

import com.privateinternetaccess.account.model.response.AmazonSubscriptionsInformation
import com.privateinternetaccess.account.model.response.AndroidSubscriptionsInformation

fun AmazonSubscriptionsInformation.toAndroidSubscription(): AndroidSubscriptionsInformation {
    val products = mutableListOf<AndroidSubscriptionsInformation.AvailableProduct>()
    for (p in availableProducts) {
        products.add(p.toAndroidAvailableProduct())
    }
    return AndroidSubscriptionsInformation(products, status)
}

fun convertToAndroidSubscription(amazonSubscription: AmazonSubscriptionsInformation): AndroidSubscriptionsInformation {
    return amazonSubscription.toAndroidSubscription()
}

private fun AmazonSubscriptionsInformation.AvailableProduct.toAndroidAvailableProduct(): AndroidSubscriptionsInformation.AvailableProduct {
    return AndroidSubscriptionsInformation.AvailableProduct(id, legacy, plan, price.toString())
}
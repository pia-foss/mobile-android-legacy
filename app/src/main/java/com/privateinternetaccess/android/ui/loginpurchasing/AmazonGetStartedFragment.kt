package com.privateinternetaccess.android.ui.loginpurchasing

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import butterknife.ButterKnife
import com.amazon.device.iap.model.PurchaseResponse
import com.privateinternetaccess.account.model.response.AmazonSubscriptionsInformation
import com.privateinternetaccess.android.BuildConfig
import com.privateinternetaccess.android.PIAApplication
import com.privateinternetaccess.android.R
import com.privateinternetaccess.android.model.events.PricingLoadedEvent
import com.privateinternetaccess.android.pia.PIAFactory
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler
import com.privateinternetaccess.android.pia.handlers.ThemeHandler
import com.privateinternetaccess.android.pia.model.enums.RequestResponseStatus
import com.privateinternetaccess.android.pia.model.events.SystemPurchaseEvent
import com.privateinternetaccess.android.pia.utils.DLog
import com.privateinternetaccess.android.pia.utils.Toaster
import com.privateinternetaccess.android.ui.drawer.settings.DeveloperActivity
import com.privateinternetaccess.android.utils.toAndroidSubscription
import kotlinx.android.synthetic.main.fragment_get_started_new.*
import java.text.DecimalFormat
import java.util.*

class AmazonGetStartedFragment : Fragment() {

    private var pricesLoaded = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_get_started_new, container, false)
        ButterKnife.bind(this, view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        yearlySpinner.isVisible = true
        monthlySpinner.isVisible = true

        if (PIAApplication.isRelease()) {
            devButton.visibility = View.GONE
        } else {
            devButton.visibility = View.VISIBLE
            devButton.setOnClickListener {
                val i = Intent(context, DeveloperActivity::class.java)
                startActivity(i)
            }
        }

        footer.movementMethod = LinkMovementMethod.getInstance()

        if (PiaPrefHandler.availableSubscriptions(requireContext()) == null) {
            requestPrices()
        } else {
            pricesLoaded()
        }

        yearly.setOnClickListener {
            handleSelection(true)
        }

        monthly.setOnClickListener {
            handleSelection(false)
        }

        subscribe.setOnClickListener { subscribe() }
        login.setOnClickListener { login() }

        PIAApplication.amazonPurchaseUtil.observableProducts.observe(viewLifecycleOwner) {
            showAmazonPricing(it!!)
        }

        PIAApplication.amazonPurchaseUtil.observablePurchase.observe(viewLifecycleOwner) {
            onPurchaseResponse(it)
        }
    }

    private fun showAmazonPricing(event: PricingLoadedEvent) {
        yearlySpinner.isGone = true
        monthlySpinner.isGone = true
        pricesLoaded = !TextUtils.isEmpty(event.yearlyCost)
        description.text = getString(R.string.startup_region_message_new, event.yearlyCost)
        setUpCosts(event.monthlyCost, event.yearlyCost)
    }

    private fun onPurchaseResponse(p0: PurchaseResponse?) {
        if (p0 != null) {
            when (p0.requestStatus) {
                PurchaseResponse.RequestStatus.SUCCESSFUL -> {
                    PiaPrefHandler.saveAmazonPurchase(
                        requireContext(),
                        p0.userData.userId,
                        p0.receipt.receiptId
                    )
                    val selectedProductId: String? = PIAApplication.amazonPurchaseUtil.getSelectedProductId()
                    if(BuildConfig.DEBUG) {
                        Log.i(TAG, "onPurchaseResponse - success: sku = '${p0.receipt.sku}'; selected product: '$selectedProductId'")
                    }
                    (requireActivity() as LoginPurchaseActivity).onSystemPurchaseEvent(
                        SystemPurchaseEvent(
                            true,
                                selectedProductId
                        )
                    )
                }
                PurchaseResponse.RequestStatus.ALREADY_PURCHASED -> {
                    Toaster.l(
                            context,
                            R.string.error_active_subscription
                    )
                    if(BuildConfig.DEBUG) {
                        Log.i(TAG, "onPurchaseResponse - already purchased: sku = ${p0.receipt.sku}")
                    }
                }
                PurchaseResponse.RequestStatus.FAILED -> {
                    if(BuildConfig.DEBUG) {
                        Log.i(TAG, "onPurchaseResponse - purchase failed: sku = ${p0.receipt.sku}")
                    }
                }
                PurchaseResponse.RequestStatus.INVALID_SKU -> {
                    if(BuildConfig.DEBUG) {
                        Log.i(TAG, "onPurchaseResponse - invalid sku: sku = ${p0.receipt.sku}")
                    }
                }
                PurchaseResponse.RequestStatus.NOT_SUPPORTED -> {
                    if(BuildConfig.DEBUG) {
                        Log.i(TAG, "onPurchaseResponse - not supported: sku = ${p0.receipt.sku}")
                    }
                }

                else -> {
                    // do nothing
                }
            }
        } else {
            showPaymentError()
        }
    }

    private fun pricesLoaded() {
        PIAApplication.amazonPurchaseUtil.selectProduct(true)
        handleSelection(true)
    }

    private fun handleSelection(yearlySelected: Boolean) {
        val theme = ThemeHandler.getCurrentTheme(requireContext())
        if (yearlySelected) {
            yearly.isSelected = true
            monthly.isSelected = false
            PIAApplication.amazonPurchaseUtil.selectProduct(true)
            yearlyIcon.setImageResource(if (theme == ThemeHandler.Theme.DAY) R.drawable.ic_selection_checked else R.drawable.ic_selection_checked_dark)
            monthlyIcon.setImageResource(if (theme == ThemeHandler.Theme.DAY) R.drawable.ic_selection else R.drawable.ic_selection_dark)
        } else {
            monthly.isSelected = true
            yearly.isSelected = false
            PIAApplication.amazonPurchaseUtil.selectProduct(false)
            yearlyIcon.setImageResource(if (theme == ThemeHandler.Theme.DAY) R.drawable.ic_selection else R.drawable.ic_selection_dark)
            monthlyIcon.setImageResource(if (theme == ThemeHandler.Theme.DAY) R.drawable.ic_selection_checked else R.drawable.ic_selection_checked_dark)
        }
    }

    private fun setUpCosts(monthly: String, yearly: String) {
        monthlyCost.text = String.format(getString(R.string.purchasing_monthly_ending), monthly)
        yearlyCost.text = getString(R.string.yearly_sub_text, yearly)
        if (!TextUtils.isEmpty(yearly)) {
            pricesLoaded = true
            val sb = StringBuilder()
            sb.append("#")
            val c = Currency.getInstance(Locale.getDefault())
            val fractionNumber = c.defaultFractionDigits
            if (fractionNumber > 0) {
                sb.append(".")
                for (i in 0 until fractionNumber) {
                    sb.append("#")
                }
            }
            val format = DecimalFormat(sb.toString())
            val cleaned = yearly.replace("\\D+".toRegex(), "")
            val currency = yearly.replace("[0-9.,]".toRegex(), "")
            try {
                var year = cleaned.toFloat()
                DLog.d("Purchasing", "year = $year cleaned = $cleaned")
                year = year / 100 / 12
                DLog.d("PurchasingFragment", "mYearlyCost = " + format.format(year))
                yearlyTotal.text = String.format(
                    getString(R.string.purchasing_yearly_month_ending),
                    format.format(year),
                    currency
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun requestPrices() {
        PIAFactory.getInstance()
            .getAccount(context)
            .amazonSubscriptions { subscriptionsInformation: AmazonSubscriptionsInformation?, responseStatus: RequestResponseStatus ->
                when (responseStatus) {
                    RequestResponseStatus.SUCCEEDED -> {
                        PiaPrefHandler.setAvailableSubscriptions(
                            context,
                            subscriptionsInformation?.toAndroidSubscription()
                        )
                        pricesLoaded()
                    }
                    RequestResponseStatus.AUTH_FAILED, RequestResponseStatus.THROTTLED, RequestResponseStatus.OP_FAILED, RequestResponseStatus.ACCOUNT_EXPIRED -> {
                        DLog.d(
                            AmazonGetStartedFragment::class.simpleName,
                            "amazonSubscriptions unsuccessful $responseStatus"
                        )
                    }
                }
                DLog.d(
                    AmazonGetStartedFragment::class.simpleName,
                    "Requesting amazonSubscriptions response: $responseStatus"
                )
            }
    }

    private fun subscribe() {
        try {
            PIAApplication.amazonPurchaseUtil.purchaseSelectedProduct()
        } catch (e: java.lang.Exception) {
            showPaymentError()
            e.printStackTrace()
        }
    }

    private fun login() {
        if (activity is LoginPurchaseActivity) {
            (activity as LoginPurchaseActivity).switchToLogin()
        }
    }

    private fun showPaymentError() {
        AlertDialog.Builder(context)
            .setMessage(R.string.hide_payment_amazon)
            .setPositiveButton(R.string.ok) { dialogInterface, _ -> dialogInterface.dismiss() }
            .setOnCancelListener { onDestroy() }
            .show()
    }

    companion object {
        private val TAG: String = AmazonGetStartedFragment::class.simpleName ?: ""
    }
}
package com.privateinternetaccess.android.ui.loginpurchasing

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import com.privateinternetaccess.account.model.response.AndroidSubscriptionsInformation
import com.privateinternetaccess.android.PIAApplication
import com.privateinternetaccess.android.R
import com.privateinternetaccess.android.databinding.FragmentGetStartedNewBinding
import com.privateinternetaccess.android.model.events.PricingLoadedEvent
import com.privateinternetaccess.android.pia.PIAFactory
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler
import com.privateinternetaccess.android.pia.handlers.ThemeHandler
import com.privateinternetaccess.android.pia.model.enums.RequestResponseStatus
import com.privateinternetaccess.android.pia.model.events.SubscriptionsEvent
import com.privateinternetaccess.android.pia.utils.DLog
import com.privateinternetaccess.android.ui.connection.MainActivityHandler
import com.privateinternetaccess.android.ui.drawer.settings.DeveloperActivity
import com.privateinternetaccess.android.utils.SubscriptionsUtils.getMonthlySubscriptionId
import com.privateinternetaccess.android.utils.SubscriptionsUtils.getYearlySubscriptionId
import com.privateinternetaccess.android.utils.SubscriptionsUtils.isPlayStoreFlavour
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.text.DecimalFormat
import java.util.*

class NewGetStartedFragment : Fragment() {

    private var pricesLoaded = false
    private var selectedProduct: String? = null
    private lateinit var binding: FragmentGetStartedNewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGetStartedNewBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (PIAApplication.isRelease()) {
            binding.devButton.visibility = View.GONE
        } else {
            binding.devButton.visibility = View.VISIBLE
            binding.devButton.setOnClickListener {
                val i = Intent(context, DeveloperActivity::class.java)
                startActivity(i)
            }
        }

        binding.footer.movementMethod = LinkMovementMethod.getInstance()

        if (PiaPrefHandler.availableSubscriptions(requireContext()) == null) {
            requestPrices()
        } else {
            pricesLoaded()
        }

        binding.yearly.setOnClickListener {
            handleSelection(true)
        }

        binding.monthly.setOnClickListener {
            handleSelection(false)
        }

        binding.subscribe.setOnClickListener { subscribe() }
        binding.login.setOnClickListener { login() }
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(sticky = true)
    fun loadPricing(event: PricingLoadedEvent) {
        requireActivity().runOnUiThread {
            pricesLoaded = !TextUtils.isEmpty(event.yearlyCost)
            binding.description.text = String.format(
                getString(R.string.startup_region_message_new),
                event.yearlyCost,
                getString(R.string.startup_region_message_cancel_anytime)
            )
            setUpCosts(event.monthlyCost, event.yearlyCost)
        }
    }

    private fun pricesLoaded() {
        binding.yearlySpinner.isGone = true
        binding.monthlySpinner.isGone = true
        EventBus.getDefault().postSticky(SubscriptionsEvent())
        selectedProduct = getYearlySubscriptionId(requireContext())
        handleSelection(true)
    }

    private fun requestPrices() {
        PIAFactory.getInstance()
            .getAccount(context)
            .availableSubscriptions { subscriptionsInformation: AndroidSubscriptionsInformation?, responseStatus: RequestResponseStatus ->
                when (responseStatus) {
                    RequestResponseStatus.SUCCEEDED -> {
                        PiaPrefHandler.setAvailableSubscriptions(
                            context,
                            subscriptionsInformation
                        )
                        pricesLoaded()
                    }
                    RequestResponseStatus.AUTH_FAILED, RequestResponseStatus.THROTTLED, RequestResponseStatus.OP_FAILED, RequestResponseStatus.ACCOUNT_EXPIRED -> {
                        DLog.d(
                            GetStartedFragment.TAG,
                            "availableSubscriptions unsuccessful $responseStatus"
                        )
                    }
                }
                DLog.d(GetStartedFragment.TAG, "Requesting subscriptions response: $responseStatus")
            }
    }

    private fun handleSelection(yearlySelected: Boolean) {
        val theme = ThemeHandler.getCurrentTheme(requireContext())
        if (yearlySelected) {
            binding.yearly.isSelected = true
            binding.monthly.isSelected = false
            selectedProduct = getYearlySubscriptionId(requireContext())
            binding.yearlyIcon.setImageResource(if (theme == ThemeHandler.Theme.DAY) R.drawable.ic_selection_checked else R.drawable.ic_selection_checked_dark)
            binding.monthlyIcon.setImageResource(if (theme == ThemeHandler.Theme.DAY) R.drawable.ic_selection else R.drawable.ic_selection_dark)
        } else {
            binding.monthly.isSelected = true
            binding.yearly.isSelected = false
            selectedProduct = getMonthlySubscriptionId(requireContext())
            binding.yearlyIcon.setImageResource(if (theme == ThemeHandler.Theme.DAY) R.drawable.ic_selection else R.drawable.ic_selection_dark)
            binding.monthlyIcon.setImageResource(if (theme == ThemeHandler.Theme.DAY) R.drawable.ic_selection_checked else R.drawable.ic_selection_checked_dark)
        }
    }

    private fun setUpCosts(monthly: String, yearly: String) {
        binding.monthlyCost.text = String.format(getString(R.string.purchasing_monthly_ending), monthly)
        binding.yearlyCost.text = getString(R.string.yearly_sub_text, yearly)
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
                binding.yearlyTotal.text = String.format(
                    getString(R.string.purchasing_yearly_month_ending),
                    format.format(year),
                    currency
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun subscribe() {
        if (PiaPrefHandler.isFeatureActive(context, MainActivityHandler.FEATURE_HIDE_PAYMENT)) {
            showPaymentError()
            return
        }

        if (pricesLoaded) {
            (activity as LoginPurchaseActivity).onSubscribeClicked(selectedProduct)
        } else {
            if (!isPlayStoreFlavour()) {
                (activity as LoginPurchaseActivity).navigateToBuyVpnSite()
            } else {
                (activity as LoginPurchaseActivity).showConnectionError()
            }
        }
    }

    private fun login() {
        if (activity is LoginPurchaseActivity) {
            (activity as LoginPurchaseActivity).switchToLogin()
        }
    }

    private fun showPaymentError() {
        AlertDialog.Builder(context)
            .setMessage(R.string.hide_payment)
            .setPositiveButton(R.string.ok) { dialogInterface, _ -> dialogInterface.dismiss() }
            .setOnCancelListener { onDestroy() }
            .show()
    }
}
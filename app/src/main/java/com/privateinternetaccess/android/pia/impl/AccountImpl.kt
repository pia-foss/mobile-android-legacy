/*
 *  Copyright (c) 2020 Private Internet Access, Inc.
 *
 *  This file is part of the Private Internet Access Android Client.
 *
 *  The Private Internet Access Android Client is free software: you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  The Private Internet Access Android Client is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU General Public License along with the Private
 *  Internet Access Android Client.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.privateinternetaccess.android.pia.impl

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.request.AmazonSignupInformation
import com.privateinternetaccess.account.model.request.AndroidSignupInformation
import com.privateinternetaccess.account.model.response.*
import com.privateinternetaccess.account.model.response.DedicatedIPInformationResponse.DedicatedIPInformation
import com.privateinternetaccess.android.BuildConfig
import com.privateinternetaccess.android.model.events.ExpiredApiTokenEvent
import com.privateinternetaccess.android.pia.account.PIAAccount
import com.privateinternetaccess.android.pia.api.PiaApi
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler
import com.privateinternetaccess.android.pia.interfaces.IAccount
import com.privateinternetaccess.android.pia.model.AccountInformation
import com.privateinternetaccess.android.pia.model.PurchaseData
import com.privateinternetaccess.android.pia.model.enums.RequestResponseStatus
import com.privateinternetaccess.android.pia.providers.ModuleClientStateProvider
import com.privateinternetaccess.android.pia.utils.DLog
import com.privateinternetaccess.android.ui.connection.MainActivityHandler
import com.privateinternetaccess.android.utils.CSIHelper
import com.privateinternetaccess.android.utils.CSIHelper.Companion.CSI_TEAM_IDENTIFIER
import com.privateinternetaccess.csi.*
import org.greenrobot.eventbus.EventBus


class AccountImpl(private val context: Context) : IAccount {

    companion object {
        private const val TAG = "AccountImpl"
        private const val STORE = "google_play"
        private const val AMAZON = "amazon_app_store"
        private lateinit var csi: CSIAPI
    }

    init {
        val csiHelper = CSIHelper(context)
        csi = CSIBuilder()
            .setTeamIdentifier(CSI_TEAM_IDENTIFIER)
            .setAppVersion(BuildConfig.VERSION_NAME)
            .setCertificate(ModuleClientStateProvider.CERTIFICATE)
            .setUserAgent(PiaApi.ANDROID_HTTP_CLIENT)
            .setEndPointProvider(ModuleClientStateProvider(context))
            .addLogProviders(
                csiHelper.applicationInformationProvider,
                csiHelper.deviceInformationProvider,
                csiHelper.lastKnownExceptionProvider,
                csiHelper.protocolInformationProvider,
                csiHelper.regionInformationProvider,
                csiHelper.userSettingsProvider
            )
            .build()
    }

    private var androidAccountAPI: AndroidAccountAPI? = null
        get() {
            if (field == null) {
                androidAccountAPI = PIAAccount.getApi(context)
            }
            return field
        }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun setAndroidAccountAPI(androidAccountAPI: AndroidAccountAPI) {
        this.androidAccountAPI = androidAccountAPI
    }

    override fun apiToken(): String? =
        androidAccountAPI?.apiToken()

    override fun vpnToken(): String? =
        androidAccountAPI?.vpnToken()

    override fun migrateApiToken(
        apiToken: String,
        callback: (status: RequestResponseStatus) -> Unit
    ) {
        androidAccountAPI?.migrateApiToken(apiToken) { errors ->
            if (errors.isNotEmpty()) {
                DLog.w(TAG, "migrateToken error: $errors")
                logoutIfNeeded(errors)
                callback(adaptResponseCode(errors.last().code))
                return@migrateApiToken
            }

            callback(RequestResponseStatus.SUCCEEDED)
        }
    }

    override fun signUp(
        orderId: String,
        token: String,
        sku: String,
        callback: (information: SignUpInformation?, status: RequestResponseStatus) -> Unit
    ) {
        val receipt = AndroidSignupInformation.Receipt(orderId, token, sku)
        androidAccountAPI?.signUp(AndroidSignupInformation(
            store = STORE,
            receipt = receipt)
        ) { details, errors ->
            if (errors.isNotEmpty()) {
                DLog.w(TAG, "signUp error: $errors")
                logoutIfNeeded(errors)
                callback(null, adaptResponseCode(errors.last().code))
                return@signUp
            }

            if (details == null) {
                DLog.w(TAG, "signUp Invalid response")
                callback(null, adaptResponseCode(errors.last().code))
                return@signUp
            }

            callback(details, RequestResponseStatus.SUCCEEDED)
        }
    }

    override fun amazonSignUp(
        userId: String,
        receiptId: String,
        callback: (information: SignUpInformation?, status: RequestResponseStatus) -> Unit
    ) {
        androidAccountAPI?.amazonSignUp(
            AmazonSignupInformation(
                userId,
                receiptId,
                ""
            )
        ) { details: SignUpInformation?, errors: List<AccountRequestError> ->
            if (errors.isNotEmpty()) {
                DLog.w(TAG, "amazon signUp error: $errors")
                logoutIfNeeded(errors)
                callback(null, adaptResponseCode(errors.last().code))
                return@amazonSignUp
            }

            if (details == null) {
                DLog.w(TAG, "amazon signUp Invalid response")
                callback(null, adaptResponseCode(errors.last().code))
                return@amazonSignUp
            }

            callback(details, RequestResponseStatus.SUCCEEDED)
        }
    }

    override fun loginLink(email: String, callback: (status: RequestResponseStatus) -> Unit) {
        androidAccountAPI?.loginLink(email) { errors ->
            if (errors.isNotEmpty()) {
                DLog.w(TAG, "loginLink error: $errors")
                logoutIfNeeded(errors)
                callback(adaptResponseCode(errors.last().code))
                return@loginLink
            }

            callback(RequestResponseStatus.SUCCEEDED)
        }
    }

    override fun loginWithCredentials(
        username: String,
        password: String,
        callback: (status: RequestResponseStatus) -> Unit
    ) {
        androidAccountAPI?.loginWithCredentials(username, password) { errors ->
            if (errors.isNotEmpty()) {
                DLog.w(TAG, "loginWithCredentials error: $errors")
                logoutIfNeeded(errors)
                callback(adaptResponseCode(errors.last().code))
                return@loginWithCredentials
            }

            callback(RequestResponseStatus.SUCCEEDED)
        }
    }

    override fun loginWithReceipt(
        receiptToken: String,
        productId: String,
        callback: (status: RequestResponseStatus) -> Unit
    ) {
        androidAccountAPI?.loginWithReceipt(
            STORE,
            receiptToken,
            productId,
            context.packageName
        ) { errors ->
            if (errors.isNotEmpty()) {
                DLog.w(TAG, "loginWithReceipt error: $errors")
                logoutIfNeeded(errors)
                callback(adaptResponseCode(errors.last().code))
                return@loginWithReceipt
            }

            callback(RequestResponseStatus.SUCCEEDED)
        }
    }

    override fun amazonLoginWithReceipt(
        receiptId: String,
        userId: String,
        callback: (status: RequestResponseStatus) -> Unit
    ) {
        androidAccountAPI?.amazonLoginWithReceipt(receiptId, userId, AMAZON) { errors ->
            if (errors.isNotEmpty()) {
                DLog.w(TAG, "amazonLoginWithReceipt error: $errors")
                logoutIfNeeded(errors)
                callback(adaptResponseCode(errors.last().code))
                return@amazonLoginWithReceipt
            }

            callback(RequestResponseStatus.SUCCEEDED)
        }
    }

    override fun updateEmail(
        email: String,
        resetPassword: Boolean,
        callback: (temporaryPassword: String?, status: RequestResponseStatus) -> Unit
    ) {
        androidAccountAPI?.setEmail(email, false) { temporaryPassword, errors ->
            if (errors.isNotEmpty()) {
                DLog.w(TAG, "setEmail error: $errors")
                logoutIfNeeded(errors)
                callback(temporaryPassword, adaptResponseCode(errors.last().code))
                return@setEmail
            }

            if (resetPassword && temporaryPassword.isNullOrEmpty()) {
                DLog.w(TAG, "setEmail Invalid response")
                callback(temporaryPassword, RequestResponseStatus.OP_FAILED)
                return@setEmail
            }

            callback(temporaryPassword, RequestResponseStatus.SUCCEEDED)
        }
    }

    override fun loggedIn(): Boolean {
        return PiaPrefHandler.isUserLoggedIn(context)
    }

    override fun logout() {
        PiaPrefHandler.setUserIsLoggedIn(context, false)
        androidAccountAPI?.logout { errors ->
            if (errors.isNotEmpty()) {
                DLog.w(TAG, "logout error: $errors")
                return@logout
            }
        }
    }

    override fun persistedAccountInformation(): AccountInformation? {
        return PiaPrefHandler.getAccountInformation(context)
    }

    override fun accountInformation(
        callback: (accountInformation: AccountInformation?, status: RequestResponseStatus) -> Unit
    ) {
        androidAccountAPI?.accountDetails { details, errors ->
            if (errors.isNotEmpty()) {
                DLog.w(TAG, "accountInformation error: $errors")
                logoutIfNeeded(errors)
                callback(null, adaptResponseCode(errors.last().code))
                return@accountDetails
            }

            if (details == null) {
                DLog.w(TAG, "accountInformation Invalid response")
                callback(null, RequestResponseStatus.OP_FAILED)
                return@accountDetails
            }

            val accountInformation = AccountInformation(
                details.email,
                details.active,
                details.expired,
                details.renewable,
                details.expireAlert,
                details.plan,
                details.expirationTime * 1000L,
                details.username
            )
            callback(accountInformation, RequestResponseStatus.SUCCEEDED)
        }
    }

    override fun dedicatedIPs(
        ipTokens: List<String>,
        callback: (details: List<DedicatedIPInformation>, status: RequestResponseStatus) -> Unit
    ) {
        androidAccountAPI?.dedicatedIPs(ipTokens) { details, errors ->
            if (errors.isNotEmpty()) {
                DLog.w(TAG, "dedicatedIPs error: $errors")
                logoutIfNeeded(errors)
                callback(emptyList(), adaptResponseCode(errors.last().code))
                return@dedicatedIPs
            }

            callback(details, RequestResponseStatus.SUCCEEDED)
        }
    }

    override fun renewDedicatedIP(
        ipToken: String,
        callback: (status: RequestResponseStatus) -> Unit
    ) {
        if (PiaPrefHandler.isFeatureActive(context, MainActivityHandler.DIP_CHECK_EXPIRATION_REQUEST)) {
            androidAccountAPI?.renewDedicatedIP(ipToken) { errors ->
                if (errors.isNotEmpty()) {
                    DLog.w(TAG, "renewDedicatedIP error: $errors")
                    logoutIfNeeded(errors)
                    callback(adaptResponseCode(errors.last().code))
                    return@renewDedicatedIP
                }

                callback(RequestResponseStatus.SUCCEEDED)
            }
        } else {
            DLog.w(TAG, "renewDedicatedIP error: Feature flag missing.")
            callback(RequestResponseStatus.OP_FAILED)
        }
    }

    override fun createTrialAccount(
        email: String,
        code: String,
        callback: (
            username: String?,
            password: String?,
            message: String?,
            status: RequestResponseStatus
        ) -> Unit
    ) {
        androidAccountAPI?.redeem(email, code) { details, errors ->
            if (errors.isNotEmpty()) {
                DLog.w(TAG, "createTrialAccount error: $errors")
                logoutIfNeeded(errors)
                callback(null, null, null, adaptResponseCode(errors.last().code))
                return@redeem
            }

            if (details == null) {
                DLog.w(TAG, "createTrialAccount Invalid response")
                callback(null, null, null, RequestResponseStatus.OP_FAILED)
                return@redeem
            }

            callback(
                details.username,
                details.password,
                details.message,
                RequestResponseStatus.SUCCEEDED
            )
        }
    }

    override fun sendInvite(
        recipientEmail: String,
        recipientName: String,
        callback: (status: RequestResponseStatus) -> Unit
    ) {
        androidAccountAPI?.sendInvite(recipientEmail, recipientName) { errors ->
            if (errors.isNotEmpty()) {
                DLog.w(TAG, "sendInvite error: $errors")
                logoutIfNeeded(errors)
                callback(adaptResponseCode(errors.last().code))
                return@sendInvite
            }

            callback(RequestResponseStatus.SUCCEEDED)
        }
    }

    override fun invites(
        callback: (details: InvitesDetailsInformation?, status: RequestResponseStatus) -> Unit
    ) {
        androidAccountAPI?.invitesDetails() { details, errors ->
            if (errors.isNotEmpty()) {
                DLog.w(TAG, "invites error: $errors")
                logoutIfNeeded(errors)
                callback(null, adaptResponseCode(errors.last().code))
                return@invitesDetails
            }

            if (details == null) {
                DLog.w(TAG, "invites Invalid response")
                callback(null, RequestResponseStatus.OP_FAILED)
                return@invitesDetails
            }

            callback(details, RequestResponseStatus.SUCCEEDED)
        }
    }

    override fun clientStatus(
        callback: (details: ClientStatusInformation?, status: RequestResponseStatus) -> Unit
    ) {
        androidAccountAPI?.clientStatus { details, errors ->
            if (errors.isNotEmpty()) {
                DLog.w(TAG, "clientStatus error: $errors")
                logoutIfNeeded(errors)
                callback(null, adaptResponseCode(errors.last().code))
                return@clientStatus
            }

            if (details == null) {
                DLog.w(TAG, "clientStatus Invalid response")
                callback(null, RequestResponseStatus.OP_FAILED)
                return@clientStatus
            }

            callback(details, RequestResponseStatus.SUCCEEDED)
        }
    }

    override fun availableSubscriptions(
        callback: (
            subscriptions: AndroidSubscriptionsInformation?,
            status: RequestResponseStatus
        ) -> Unit
    ) {
        androidAccountAPI?.subscriptions { details, errors ->
            if (errors.isNotEmpty()) {
                DLog.w(TAG, "availableSubscriptions error: $errors")
                logoutIfNeeded(errors)
                callback(null, adaptResponseCode(errors.last().code))
                return@subscriptions
            }

            if (details == null) {
                DLog.w(TAG, "availableSubscriptions Invalid response")
                callback(null, RequestResponseStatus.OP_FAILED)
                return@subscriptions
            }

            callback(details, RequestResponseStatus.SUCCEEDED)
        }
    }

    override fun amazonSubscriptions(callback: (subscriptions: AmazonSubscriptionsInformation?, status: RequestResponseStatus) -> Unit) {
        androidAccountAPI?.amazonSubscriptions { details, errors ->
            if (errors.isNotEmpty()) {
                DLog.w(TAG, "amazonSubscriptions error: $errors")
                logoutIfNeeded(errors)
                callback(null, adaptResponseCode(errors.last().code))
                return@amazonSubscriptions
            }

            if (details == null) {
                DLog.w(TAG, "amazonSubscriptions Invalid response")
                callback(null, RequestResponseStatus.OP_FAILED)
                return@amazonSubscriptions
            }

            callback(details, RequestResponseStatus.SUCCEEDED)
        }
    }

    override fun message(
        callback: (
            message: MessageInformation?,
            status: RequestResponseStatus
        ) -> Unit
    ) {
        androidAccountAPI?.message(BuildConfig.VERSION_NAME) { message, errors ->
            if (errors.isNotEmpty()) {
                DLog.w(TAG, "messages error: $errors")
                logoutIfNeeded(errors)
                callback(null, adaptResponseCode(errors.last().code))
                return@message
            }

            if (message == null) {
                DLog.w(TAG, "message Invalid response")
                callback(null, RequestResponseStatus.OP_FAILED)
                return@message
            }

            callback(message, RequestResponseStatus.SUCCEEDED)
        }
    }

    override fun featureFlags(
        callback: (
            featureFlags: FeatureFlagsInformation?,
            status: RequestResponseStatus
        ) -> Unit) {
        androidAccountAPI?.featureFlags { flags, errors ->
            if (errors.isNotEmpty()) {
                DLog.w(TAG, "feature flags error: $errors")
                logoutIfNeeded(errors)
                callback(null, adaptResponseCode(errors.last().code))
                return@featureFlags
            }

            if (flags == null) {
                DLog.w(TAG, "feature flags Invalid response")
                callback(null, RequestResponseStatus.OP_FAILED)
                return@featureFlags
            }

            callback(flags, RequestResponseStatus.SUCCEEDED)
        }
    }

    override fun sendDebugReport(
        callback: (reportIdentifier: String?, status: RequestResponseStatus) -> Unit
    ) {
        csi.send(true) { reportIdentifier, errors ->

            if (errors.isNotEmpty()) {
                DLog.w(TAG, "sendDebugReport errors: $errors")
            }

            if (reportIdentifier.isNullOrEmpty()) {
                DLog.w(TAG, "sendDebugReport invalid response")
                callback(null, RequestResponseStatus.OP_FAILED)
                return@send
            }

            callback(reportIdentifier, RequestResponseStatus.SUCCEEDED)
        }
    }

    override fun temporaryPurchaseData(): PurchaseData? {
        return PiaPrefHandler.getPurchasingData(context)
    }

    override fun saveTemporaryPurchaseData(data: PurchaseData) {
        PiaPrefHandler.savePurchasingTask(context, data.orderId, data.token, data.productId)
    }

    // region private
    private fun logoutIfNeeded(errors: List<AccountRequestError>) {
        errors.firstOrNull { adaptResponseCode(it.code) == RequestResponseStatus.AUTH_FAILED }?.let {
            EventBus.getDefault().post(ExpiredApiTokenEvent())
        }
    }

    private fun adaptResponseCode(responseCode: Int) = when (responseCode) {
        200 -> RequestResponseStatus.SUCCEEDED
        401 -> RequestResponseStatus.AUTH_FAILED
        // iva - 23/12/2021 todo:// uncomment to re-introduce expired account flow
        // 402 -> RequestResponseStatus.ACCOUNT_EXPIRED
        429 -> RequestResponseStatus.THROTTLED
        else -> RequestResponseStatus.OP_FAILED
    }
    // endregion
}
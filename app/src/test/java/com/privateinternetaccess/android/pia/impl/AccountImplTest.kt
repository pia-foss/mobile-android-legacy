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
import androidx.test.core.app.ApplicationProvider
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.request.AndroidSignupInformation
import com.privateinternetaccess.android.pia.PIAFactory
import com.privateinternetaccess.android.pia.interfaces.IAccount
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class AccountImplTest {

    private lateinit var account: IAccount
    private lateinit var context: Context

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        context = ApplicationProvider.getApplicationContext()
        account = PIAFactory.getInstance().getAccount(context)
    }

    @Test
    fun testSignUpModuleInvocation() {
        val androidAccountAPI = mock<AndroidAccountAPI>()
        (account as AccountImpl).setAndroidAccountAPI(androidAccountAPI)

        val orderId = "username"
        val token = "password"
        val sku = "sku"
        val argument = argumentCaptor<AndroidSignupInformation>()
        account.signUp(orderId, token, sku) { _, _ -> }
        verify(androidAccountAPI).signUp(argument.capture(), any())
        assertEquals(argument.firstValue.receipt.orderId, orderId)
        assertEquals(argument.firstValue.receipt.token, token)
        assertEquals(argument.firstValue.receipt.sku, sku)
    }

    @Test
    fun testLoginWithCredentialsModuleInvocation() {
        val androidAccountAPI = mock<AndroidAccountAPI>()
        (account as AccountImpl).setAndroidAccountAPI(androidAccountAPI)

        val username = "username"
        val password = "password"
        account.loginWithCredentials(username, password) { }
        verify(androidAccountAPI).loginWithCredentials(
                ArgumentMatchers.matches(username),
                ArgumentMatchers.matches(password),
                any()
        )
    }

    @Test
    fun testLoginWithReceiptModuleInvocation() {
        val androidAccountAPI = mock<AndroidAccountAPI>()
        (account as AccountImpl).setAndroidAccountAPI(androidAccountAPI)

        val productId = "productId"
        val token = "token"
        account.loginWithReceipt(token, productId) { }
        verify(androidAccountAPI).loginWithReceipt(
                ArgumentMatchers.matches("google_play"),
                ArgumentMatchers.matches(token),
                ArgumentMatchers.matches(productId),
                any(),
                any()
        )
    }

    @Test
    fun testAccountInformationModuleInvocation() {
        val androidAccountAPI = mock<AndroidAccountAPI>()
        (account as AccountImpl).setAndroidAccountAPI(androidAccountAPI)

        account.accountInformation { _, _ -> }
        verify(androidAccountAPI).accountDetails(any())
    }

    @Test
    fun testUpdateEmailModuleInvocation() {
        val androidAccountAPI = mock<AndroidAccountAPI>()
        (account as AccountImpl).setAndroidAccountAPI(androidAccountAPI)

        val email = "email"
        val resetPassword = false
        account.updateEmail(email, resetPassword) { _, _ -> }
        verify(androidAccountAPI).setEmail(
                ArgumentMatchers.matches(email),
                ArgumentMatchers.eq(resetPassword),
                any()
        )
    }

    @Test
    fun testCreateTrialAccountModuleInvocation() {
        val androidAccountAPI = mock<AndroidAccountAPI>()
        (account as AccountImpl).setAndroidAccountAPI(androidAccountAPI)

        val email = "email"
        val code = "code"
        account.createTrialAccount(email, code) { _, _, _, _ -> }
        verify(androidAccountAPI).redeem(
                ArgumentMatchers.matches(email),
                ArgumentMatchers.matches(code),
                any()
        )
    }

    @Test
    fun testLogoutModuleInvocation() {
        val androidAccountAPI = mock<AndroidAccountAPI>()
        (account as AccountImpl).setAndroidAccountAPI(androidAccountAPI)

        account.logout()
        verify(androidAccountAPI).logout(any())
    }

    @Test
    fun testClientStatusModuleInvocation() {
        val androidAccountAPI = mock<AndroidAccountAPI>()
        (account as AccountImpl).setAndroidAccountAPI(androidAccountAPI)

        account.clientStatus { _, _ -> }
        verify(androidAccountAPI).clientStatus(any())
    }

    @Test
    fun testAvailableSubscriptionsModuleInvocation() {
        val androidAccountAPI = mock<AndroidAccountAPI>()
        (account as AccountImpl).setAndroidAccountAPI(androidAccountAPI)

        account.availableSubscriptions { _, _ ->  }
        verify(androidAccountAPI).subscriptions(any())
    }
}
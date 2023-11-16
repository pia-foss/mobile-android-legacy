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

package com.privateinternetaccess.android.pia.handler

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.android.pia.PIAFactory
import com.privateinternetaccess.android.pia.handlers.PIAServerHandler
import com.privateinternetaccess.android.pia.impl.AccountImpl
import com.privateinternetaccess.android.pia.interfaces.IAccount
import com.privateinternetaccess.android.pia.interfaces.IFactory
import com.privateinternetaccess.android.pia.interfaces.IVPN
import com.privateinternetaccess.android.pia.utils.Prefs
import com.privateinternetaccess.android.utils.KeyStoreUtils
import com.privateinternetaccess.regions.RegionLowerLatencyInformation
import com.privateinternetaccess.regions.RegionsAPI
import com.privateinternetaccess.regions.model.ShadowsocksRegionsResponse
import com.privateinternetaccess.regions.model.VpnRegionsResponse
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PIAServerHandlerTest {

    private lateinit var context: Context
    private lateinit var mockedStatic: MockedStatic<PIAFactory>

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        context = ApplicationProvider.getApplicationContext()
        Prefs.setKeyStoreUtils(Mockito.mock(KeyStoreUtils::class.java))

        val accountMock = mock<IAccount>()
        Mockito.`when`(accountMock.loggedIn()).thenReturn(true)

        val vpnMock = mock<IVPN>()
        Mockito.`when`(vpnMock.isVPNActive()).thenReturn(false)

        val factoryMock = mock<IFactory>()
        Mockito.`when`(factoryMock.getAccount(context)).thenReturn(accountMock)
        Mockito.`when`(factoryMock.getVPN(context)).thenReturn(vpnMock)

        mockedStatic = Mockito.mockStatic(PIAFactory::class.java)
        mockedStatic.`when`<IFactory> { PIAFactory.getInstance() }.thenReturn(factoryMock)

        PIAServerHandler.releaseInstance()
    }

    @After
    fun cleanup() {
        mockedStatic.close()
    }

    @Test
    fun testGen4WorkersAllocation() {
        PIAServerHandler.getInstance(context).fetchServers(context, true)
        Assert.assertNotNull(PIAServerHandler.getInstance(context).pingIntent)
        Assert.assertNotNull(PIAServerHandler.getInstance(context).fetchServersIntent)
    }

    @Test
    fun testGen4ServerFetchingInvocationOnObjectInitialization() {
        val regionsSpy = Mockito.spy(MockRegionsApi(mockResponse = false))
        PIAServerHandler.setRegionModule(regionsSpy)

        PIAServerHandler.getInstance(context)
        verify(regionsSpy).fetchVpnRegions(any(), any())
    }

    @Test
    fun testGen4ServerFetchingInvocationAfterObjectInitialization() {
        val regionsSpy = Mockito.spy(MockRegionsApi(mockResponse = false))
        PIAServerHandler.setRegionModule(regionsSpy)

        PIAServerHandler.getInstance(context)
        verify(regionsSpy).fetchVpnRegions(any(), any())
    }

    @Test
    fun testGen4ServerPingAfterFetching() {
        val regionsSpy = Mockito.spy(MockRegionsApi(mockResponse = false))
        PIAServerHandler.setRegionModule(regionsSpy)

        PIAServerHandler.getInstance(context).fetchServers(context, true)

        // The initial request when the object is allocated + the explicit call to `fetchServers`
        verify(regionsSpy, Mockito.times(2)).pingRequests(any())
    }
}

private class MockRegionsApi(private val mockResponse: Boolean) : RegionsAPI {

    override fun fetchShadowsocksRegions(
        locale: String,
        callback: (response: List<ShadowsocksRegionsResponse>, error: Error?) -> Unit
    ) {
        callback(emptyList(), null)
    }

    override fun fetchVpnRegions(
        locale: String,
        callback: (response: VpnRegionsResponse?, Error?) -> Unit
    ) {
        if (mockResponse) {
            callback(Mockito.mock(VpnRegionsResponse::class.java), null)
        } else {
            callback(null, Error("Tests"))
        }
    }

    override fun pingRequests(
        callback: (response: List<RegionLowerLatencyInformation>, Error?) -> Unit
    ) {
        callback(emptyList(), null)
    }
}
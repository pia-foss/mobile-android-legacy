package com.privateinternetaccess.android.pia.migration

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.privateinternetaccess.android.pia.migration.mocks.*
import com.privateinternetaccess.android.pia.migration.utils.GivenUsecase
import com.privateinternetaccess.android.ui.connection.controller.data.externals.IOpenVpnConfiguration
import com.privateinternetaccess.android.ui.connection.controller.domain.usecases.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class IsOpenVpnUserConfigurationSameAsDefaultTest {

    @Test
    fun `succeed if the resulting openvpn configurations are the same`() = runTest {

        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val openVpnConfigurationMock: IOpenVpnConfiguration =
                OpenVpnConfigurationMock(
                        userDefinedConfigurationMock = "cipher AES-128-GCM auth SHA256",
                        applicationDefaultConfigurationMock = "cipher AES-128-GCM auth SHA256"
                )
        val useCase: IIsOpenVpnUserConfigurationSameAsDefault =
                GivenUsecase.isOpenVpnUserConfigurationSameAsDefault(
                        context = context,
                        openVpnConfiguration = openVpnConfigurationMock
                )

        // when
        val result = useCase()

        // then
        assert(result.isSuccess)
    }

    @Test
    fun `fail if the resulting openvpn configurations are the different`() = runTest {

        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val openVpnConfigurationMock: IOpenVpnConfiguration =
                OpenVpnConfigurationMock(
                        userDefinedConfigurationMock = "cipher AES-128-GCM auth SHA256",
                        applicationDefaultConfigurationMock = "cipher AES-256-GCM auth SHA256"
                )
        val useCase: IIsOpenVpnUserConfigurationSameAsDefault =
                GivenUsecase.isOpenVpnUserConfigurationSameAsDefault(
                        context = context,
                        openVpnConfiguration = openVpnConfigurationMock
                )

        // when
        val result = useCase()

        // then
        assert(result.isFailure)
    }
}
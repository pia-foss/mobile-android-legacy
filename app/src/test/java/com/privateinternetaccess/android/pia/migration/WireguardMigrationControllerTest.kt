package com.privateinternetaccess.android.pia.migration

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.privateinternetaccess.android.pia.migration.mocks.*
import com.privateinternetaccess.android.pia.migration.utils.GivenController
import com.privateinternetaccess.android.ui.connection.controller.IWireguardMigrationController
import com.privateinternetaccess.android.ui.connection.controller.domain.usecases.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class WireguardMigrationControllerTest {

    @Test
    fun `successfully migrate when all use cases succeed`() = runTest {

        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val isSelectedProtocolOpenVpnMock: IIsSelectedProtocolOpenVpn =
                IsSelectedProtocolOpenVpnMock(shouldSucceed = true)
        val hasWireguardMigrationNotBeenDoneMock: IHasWireguardMigrationNotBeenDone =
                HasWireguardMigrationNotBeenDoneMock(shouldSucceed = true)
        val isOpenVpnUserConfigurationSameAsDefaultMock: IIsOpenVpnUserConfigurationSameAsDefault =
                IsOpenVpnUserConfigurationSameAsDefaultMock(shouldSucceed = true)
        val setWireguardAsSelectedProtocolMock: ISetWireguardAsSelectedProtocol =
                SetWireguardAsSelectedProtocolMock(shouldSucceed = true)
        val markWireguardMigrationAsDoneMock: IMarkWireguardMigrationAsDone =
                MarkWireguardMigrationAsDoneMock(shouldSucceed = true)
        val migrateController: IWireguardMigrationController = GivenController.wireguardMigrationController(
                context = context,
                isSelectedProtocolOpenVpn = isSelectedProtocolOpenVpnMock,
                hasWireguardMigrationNotBeenDone = hasWireguardMigrationNotBeenDoneMock,
                isOpenVpnUserConfigurationSameAsDefault = isOpenVpnUserConfigurationSameAsDefaultMock,
                setWireguardAsSelectedProtocol = setWireguardAsSelectedProtocolMock,
                markWireguardMigrationAsDone = markWireguardMigrationAsDoneMock
        )

        // when
        val result = migrateController()

        // then
        assert(result.isSuccess)
    }

    @Test
    fun `fail migration if the selected protocol is not openvpn`() = runTest {

        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val isSelectedProtocolOpenVpnMock: IIsSelectedProtocolOpenVpn =
                IsSelectedProtocolOpenVpnMock(shouldSucceed = false)
        val hasWireguardMigrationNotBeenDoneMock: IHasWireguardMigrationNotBeenDone =
                HasWireguardMigrationNotBeenDoneMock(shouldSucceed = true)
        val isOpenVpnUserConfigurationSameAsDefaultMock: IIsOpenVpnUserConfigurationSameAsDefault =
                IsOpenVpnUserConfigurationSameAsDefaultMock(shouldSucceed = true)
        val setWireguardAsSelectedProtocolMock: ISetWireguardAsSelectedProtocol =
                SetWireguardAsSelectedProtocolMock(shouldSucceed = true)
        val markWireguardMigrationAsDoneMock: IMarkWireguardMigrationAsDone =
                MarkWireguardMigrationAsDoneMock(shouldSucceed = true)
        val migrateController: IWireguardMigrationController = GivenController.wireguardMigrationController(
                context = context,
                isSelectedProtocolOpenVpn = isSelectedProtocolOpenVpnMock,
                hasWireguardMigrationNotBeenDone = hasWireguardMigrationNotBeenDoneMock,
                isOpenVpnUserConfigurationSameAsDefault = isOpenVpnUserConfigurationSameAsDefaultMock,
                setWireguardAsSelectedProtocol = setWireguardAsSelectedProtocolMock,
                markWireguardMigrationAsDone = markWireguardMigrationAsDoneMock
        )

        // when
        val result = migrateController()

        // then
        assert(result.isFailure)
    }

    @Test
    fun `fail migration if the migration has been done already`() = runTest {

        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val isSelectedProtocolOpenVpnMock: IIsSelectedProtocolOpenVpn =
                IsSelectedProtocolOpenVpnMock(shouldSucceed = true)
        val hasWireguardMigrationNotBeenDoneMock: IHasWireguardMigrationNotBeenDone =
                HasWireguardMigrationNotBeenDoneMock(shouldSucceed = false)
        val isOpenVpnUserConfigurationSameAsDefaultMock: IIsOpenVpnUserConfigurationSameAsDefault =
                IsOpenVpnUserConfigurationSameAsDefaultMock(shouldSucceed = true)
        val setWireguardAsSelectedProtocolMock: ISetWireguardAsSelectedProtocol =
                SetWireguardAsSelectedProtocolMock(shouldSucceed = true)
        val markWireguardMigrationAsDoneMock: IMarkWireguardMigrationAsDone =
                MarkWireguardMigrationAsDoneMock(shouldSucceed = true)
        val migrateController: IWireguardMigrationController = GivenController.wireguardMigrationController(
                context = context,
                isSelectedProtocolOpenVpn = isSelectedProtocolOpenVpnMock,
                hasWireguardMigrationNotBeenDone = hasWireguardMigrationNotBeenDoneMock,
                isOpenVpnUserConfigurationSameAsDefault = isOpenVpnUserConfigurationSameAsDefaultMock,
                setWireguardAsSelectedProtocol = setWireguardAsSelectedProtocolMock,
                markWireguardMigrationAsDone = markWireguardMigrationAsDoneMock
        )

        // when
        val result = migrateController()

        // then
        assert(result.isFailure)
    }

    @Test
    fun `fail migration if the user and default openvpn configuration are different`() = runTest {

        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val isSelectedProtocolOpenVpnMock: IIsSelectedProtocolOpenVpn =
                IsSelectedProtocolOpenVpnMock(shouldSucceed = true)
        val hasWireguardMigrationNotBeenDoneMock: IHasWireguardMigrationNotBeenDone =
                HasWireguardMigrationNotBeenDoneMock(shouldSucceed = true)
        val isOpenVpnUserConfigurationSameAsDefaultMock: IIsOpenVpnUserConfigurationSameAsDefault =
                IsOpenVpnUserConfigurationSameAsDefaultMock(shouldSucceed = false)
        val setWireguardAsSelectedProtocolMock: ISetWireguardAsSelectedProtocol =
                SetWireguardAsSelectedProtocolMock(shouldSucceed = true)
        val markWireguardMigrationAsDoneMock: IMarkWireguardMigrationAsDone =
                MarkWireguardMigrationAsDoneMock(shouldSucceed = true)
        val migrateController: IWireguardMigrationController = GivenController.wireguardMigrationController(
                context = context,
                isSelectedProtocolOpenVpn = isSelectedProtocolOpenVpnMock,
                hasWireguardMigrationNotBeenDone = hasWireguardMigrationNotBeenDoneMock,
                isOpenVpnUserConfigurationSameAsDefault = isOpenVpnUserConfigurationSameAsDefaultMock,
                setWireguardAsSelectedProtocol = setWireguardAsSelectedProtocolMock,
                markWireguardMigrationAsDone = markWireguardMigrationAsDoneMock
        )

        // when
        val result = migrateController()

        // then
        assert(result.isFailure)
    }

    @Test
    fun `fail migration if setting wireguard protocol fails`() = runTest {

        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val isSelectedProtocolOpenVpnMock: IIsSelectedProtocolOpenVpn =
                IsSelectedProtocolOpenVpnMock(shouldSucceed = true)
        val hasWireguardMigrationNotBeenDoneMock: IHasWireguardMigrationNotBeenDone =
                HasWireguardMigrationNotBeenDoneMock(shouldSucceed = true)
        val isOpenVpnUserConfigurationSameAsDefaultMock: IIsOpenVpnUserConfigurationSameAsDefault =
                IsOpenVpnUserConfigurationSameAsDefaultMock(shouldSucceed = true)
        val setWireguardAsSelectedProtocolMock: ISetWireguardAsSelectedProtocol =
                SetWireguardAsSelectedProtocolMock(shouldSucceed = false)
        val markWireguardMigrationAsDoneMock: IMarkWireguardMigrationAsDone =
                MarkWireguardMigrationAsDoneMock(shouldSucceed = true)
        val migrateController: IWireguardMigrationController = GivenController.wireguardMigrationController(
                context = context,
                isSelectedProtocolOpenVpn = isSelectedProtocolOpenVpnMock,
                hasWireguardMigrationNotBeenDone = hasWireguardMigrationNotBeenDoneMock,
                isOpenVpnUserConfigurationSameAsDefault = isOpenVpnUserConfigurationSameAsDefaultMock,
                setWireguardAsSelectedProtocol = setWireguardAsSelectedProtocolMock,
                markWireguardMigrationAsDone = markWireguardMigrationAsDoneMock
        )

        // when
        val result = migrateController()

        // then
        assert(result.isFailure)
    }

    @Test
    fun `fail migration if marking the wireguard migration fails`() = runTest {

        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val isSelectedProtocolOpenVpnMock: IIsSelectedProtocolOpenVpn =
                IsSelectedProtocolOpenVpnMock(shouldSucceed = true)
        val hasWireguardMigrationNotBeenDoneMock: IHasWireguardMigrationNotBeenDone =
                HasWireguardMigrationNotBeenDoneMock(shouldSucceed = true)
        val isOpenVpnUserConfigurationSameAsDefaultMock: IIsOpenVpnUserConfigurationSameAsDefault =
                IsOpenVpnUserConfigurationSameAsDefaultMock(shouldSucceed = true)
        val setWireguardAsSelectedProtocolMock: ISetWireguardAsSelectedProtocol =
                SetWireguardAsSelectedProtocolMock(shouldSucceed = true)
        val markWireguardMigrationAsDoneMock: IMarkWireguardMigrationAsDone =
                MarkWireguardMigrationAsDoneMock(shouldSucceed = false)
        val migrateController: IWireguardMigrationController = GivenController.wireguardMigrationController(
                context = context,
                isSelectedProtocolOpenVpn = isSelectedProtocolOpenVpnMock,
                hasWireguardMigrationNotBeenDone = hasWireguardMigrationNotBeenDoneMock,
                isOpenVpnUserConfigurationSameAsDefault = isOpenVpnUserConfigurationSameAsDefaultMock,
                setWireguardAsSelectedProtocol = setWireguardAsSelectedProtocolMock,
                markWireguardMigrationAsDone = markWireguardMigrationAsDoneMock
        )

        // when
        val result = migrateController()

        // then
        assert(result.isFailure)
    }

    @Test
    fun `when failure upon wireguard already selected - Mark migration as done`() = runTest {

        // given
        val context: Context = ApplicationProvider.getApplicationContext()
        val isSelectedProtocolOpenVpnMock: IIsSelectedProtocolOpenVpn =
                IsSelectedProtocolOpenVpnMock(shouldSucceed = false)
        val markWireguardMigrationAsDoneMock: IMarkWireguardMigrationAsDone =
                MarkWireguardMigrationAsDoneMock(shouldSucceed = true)
        val migrateController: IWireguardMigrationController = GivenController.wireguardMigrationController(
                context = context,
                isSelectedProtocolOpenVpn = isSelectedProtocolOpenVpnMock,
                markWireguardMigrationAsDone = markWireguardMigrationAsDoneMock
        )

        // when
        val result = migrateController()

        // then
        assert(result.isFailure)
        assert((markWireguardMigrationAsDoneMock as MarkWireguardMigrationAsDoneMock).invoked)
    }
}
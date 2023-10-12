package com.privateinternetaccess.android.screens.steps

import com.privateinternetaccess.android.screens.objects.ProtocolPageObjects

class ProtocolStepObjects {

    val protocolPageObjects = ProtocolPageObjects()

    fun selectOpenVPN() {
        protocolPageObjects.openVPN.click()
        protocolPageObjects.save.click()
    }
}
package com.privateinternetaccess.android.pia.utils

/*
 *  Copyright (c) 2021 Private Internet Access, Inc.
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

import android.content.Context
import com.privateinternetaccess.android.model.listModel.PIALogItem
import de.blinkt.openvpn.core.VpnStatus
import java.io.BufferedReader
import java.io.InputStreamReader


object PIAVpnUtils {

    fun openVpnLogs(context: Context): MutableList<PIALogItem> {
        val items: MutableList<PIALogItem> = mutableListOf()
        val ovpnItems = VpnStatus.getlogbuffer()
        for (item in ovpnItems) {
            items.add(PIALogItem(item, context))
        }
        items.reverse()
        return items
    }

    fun wireguardLogs(): MutableList<PIALogItem> {
        val items: MutableList<PIALogItem> = mutableListOf()
        try {
            val process = Runtime.getRuntime().exec(
                    arrayOf("logcat", "-b", "all", "-t", "2000", "-d", "-v", "threadtime", "*:V")
            )
            BufferedReader(InputStreamReader(process.inputStream)).use { stdout ->
                BufferedReader(InputStreamReader(process.errorStream)).use { stderr ->
                    var line: String
                    while (stdout.readLine().also { line = it } != null) {
                        if (line.contains("Wire")) {
                            items.add(PIALogItem(logMessage(line), logTime(line)))
                        }
                    }
                    stdout.close()
                    if (process.waitFor() != 0) {
                        val errors = StringBuilder()
                        errors.append("Unable to run logcat:")
                        while (stderr.readLine().also { line = it } != null) errors.append(line)
                        throw Exception(errors.toString())
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        items.reverse()
        return items
    }

    // region private
    private fun logTime(line: String): String {
        val split: Array<String?> = line.split(" ").toTypedArray()
        return if (split[0] != null && split[1] != null) {
            split[0].toString() + " " + split[1]
        } else ""
    }

    private fun logMessage(line: String): String {
        val split = line.split(" ").toTypedArray()
        var message = ""
        for (i in 5 until split.size) {
            message += split[i] + " "
        }
        return message.trim { it <= ' ' }
    }
    // endregion
}
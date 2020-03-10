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

package com.privateinternetaccess.android.ui.connection;

import java.text.DecimalFormat;
import java.util.Locale;

import de.blinkt.openvpn.core.OpenVPNManagement;

/**
 * Created by hfrede on 11/13/17.
 */

public class GraphFragmentHandler {

    public static String getFormattedString(long bits, String graphUnit, String[] array){
        int position = 0;
        for (String item : array){
            if(item.equals(graphUnit)){
                break;
            }
            position++;
        }
        boolean mB = position % 2 != 0;
//        Log.d("Graph","mB = " + mB + " position" + position);
        String pre = getPrefix(graphUnit);
        String number = new DecimalFormat("#.##").format(cleanBytes(bits, graphUnit));
//        DLog.d("GraphFragment", "unit = " + unit + " pre = " + pre + " number = " +
//                number + " bits = " + bits + " mBits = " + (unit % 1000));
        if (!mB)
            return String.format(Locale.getDefault(), "%s %sbit", number, pre);
        else
            return String.format(Locale.getDefault(), "%s %sB", number, pre);
    }

    public static String getPrefix(String graphUnit){
        float unit = Float.parseFloat(graphUnit);
        String prefix = "";
        if(unit >= 1000000000){
            prefix = "G";
        } else if(unit >= 1000000){
            prefix = "M";
        } else if(unit >= 1000){
            prefix = "k";
        }
        return prefix;
    }

    public static float cleanBytes(long bits, String graphUnit){
        float unit = Float.parseFloat(graphUnit);
        bits = bits / OpenVPNManagement.mBytecountInterval;
        bits = bits * 8;
        return bits / unit;
    }
}

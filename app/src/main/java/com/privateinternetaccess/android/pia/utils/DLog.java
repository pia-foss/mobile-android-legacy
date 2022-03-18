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

package com.privateinternetaccess.android.pia.utils;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by half47 on 2/5/16.
 *
 * This might be made depercated as proguard can strip logs out.
 */
public class DLog {

    public static final String DEBUG_FILE_TXT = "debug_file.txt";
    public static final String LINE_SEPERATOR = "-~-";
    public static boolean DEV_MODE;
    public static boolean DEBUG_MODE;
    public static int DEBUG_LEVEL;
    public static File base;

    /**
     * Log out for Information on processes. (Locations hit in code, launches, even variables if needed)
     *
     */
    public static void i(String tag, String message) {
        if (DEV_MODE) {
            Log.i(tag, message);
            if (DEBUG_MODE && DEBUG_LEVEL == 0) {
                logToFile("i", tag, message);
            }
        }
    }

    /**
     * Log out for debug information (variables, elements, and block elements)
     *
     */
    public static void d(String tag, String message) {
        if (DEV_MODE) {
            Log.d(tag, message);
            if(DEBUG_MODE && DEBUG_LEVEL <= 1){
                logToFile("d", tag, message);
            }
        }
    }

    /**
     * Log out for Warnings (Caught exceptions, bad values, corrections)
     *
     */
    public static void w(String tag, String message) {
        if (DEV_MODE) {
            Log.w(tag, message);
            if (DEBUG_MODE && DEBUG_LEVEL <= 2) {
                logToFile("w", tag, message);
            }
        }
    }

    /**
     * Log out for error information (Exceptions, crashes)
     *
     */
    public static void e(String tag, String message) {
        if (DEV_MODE) {
            Log.e(tag, message);
            if (DEBUG_MODE && DEBUG_LEVEL <= 3) {
                logToFile("e", tag, message);
            }
        }
    }

    private static void logToFile(String level, String tag, String message){
//        Log.d("DLog","level = " + level + " tag = " + tag + " message = " + message);
        if(base != null){
            File debugFile = new File(base, DEBUG_FILE_TXT);
            String data = level + " : " + tag + " - " + message;
            if(!debugFile.exists()){
                try {
                    debugFile.createNewFile();
                } catch (IOException e) {
                }
            }
            try{
                FileWriter writer = new FileWriter(debugFile, true);
                BufferedWriter writer1 = new BufferedWriter(writer);
                writer1.append(data);
                writer1.append(LINE_SEPERATOR);
                writer1.flush();
                writer1.close();
                writer.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
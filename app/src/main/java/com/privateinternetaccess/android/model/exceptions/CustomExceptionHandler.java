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

package com.privateinternetaccess.android.model.exceptions;

import android.content.Context;
import android.util.Log;

import com.privateinternetaccess.android.pia.utils.DLog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by half47 on 2/3/17.
 *
 * This is a way we could send crashes to the server in the future.
 */
public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {

    public static final String ANDROID_CRASH_STACKTRACE_TXT = "android_crash_stacktrace.txt";
    private Thread.UncaughtExceptionHandler defaultUEH;

    private String localPath;

    /*
     * if any of the parameters is null, the respective functionality
     * will not be used
     */
    public CustomExceptionHandler(String localPath, String url) {
        this.localPath = localPath;
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
    }

    public void uncaughtException(Thread t, Throwable e) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        String stacktrace = result.toString();
        printWriter.close();

        if (localPath != null) {
            writeToFile(stacktrace, ANDROID_CRASH_STACKTRACE_TXT);
        }
//        if (url != null) {
//            sendToServer(stacktrace, filename);
//        }

        DLog.e("UNCAUGHT EXCEPTION", "message = " + e.getMessage() + " ||| mes = " + Log.getStackTraceString(e));

        defaultUEH.uncaughtException(t, e);
    }

    private void writeToFile(String stacktrace, String filename) {
        try {
            BufferedWriter bos = new BufferedWriter(new FileWriter(
                    localPath + "/" + filename));
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = Calendar.getInstance().getTime();
            bos.write("--- Time of crash: " + formatter.format(date) + " ---");
            bos.write(stacktrace);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getStackTrace(Context ctx){
        StringBuilder sb = new StringBuilder("\n\n\n~~~~~~~~ Crash log ~~~~~~~~\n\n");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(ctx.getFilesDir().getAbsolutePath()+ "/" + ANDROID_CRASH_STACKTRACE_TXT));
            while(reader.read() != -1){
                sb.append(reader.readLine() + "\n");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        sb.append("\n\n~~~~~~~~ End Crash Log ~~~~~~~~\n");

        return sb.toString();
    }

    public void setDefaultUEH(Thread.UncaughtExceptionHandler defaultUEH) {
        this.defaultUEH = defaultUEH;
    }


}

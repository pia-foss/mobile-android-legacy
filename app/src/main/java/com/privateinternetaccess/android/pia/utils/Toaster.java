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

import android.content.Context;
import android.widget.Toast;


/**
 * Created by half47 on 10/4/16.
 *
 * I'm bored waiting for QA so I wanted to clean up Toasts, with options. I can't tell if I'm being lazy or stupid.
 * Leave a comment below and let see if we can have a conversation on this.
 *
 *
 */

public class Toaster {

    public static Toast s(Context context, String text){
        return create(context, text, Toast.LENGTH_SHORT, -1, 0, 0, true);
    }

    public static Toast l(Context context, String text){
        return create(context, text, Toast.LENGTH_LONG, -1, 0, 0, true);
    }

    public static Toast s(Context context, int text){
        return create(context, text, Toast.LENGTH_SHORT, -1, 0, 0, true);
    }

    public static Toast l(Context context, int text){
        return create(context, text, Toast.LENGTH_LONG, -1, 0, 0, true);
    }

    public static Toast create(Context context, String text, int duration, int gravity, int offsetx, int offsety, boolean autorun){

        Toast t = Toast.makeText(context.getApplicationContext(), text, duration);
        if(gravity != -1){
            t.setGravity(gravity, offsetx, offsety);
        }
        if(autorun)
            t.show();
        return t;
    }

    public static Toast create(Context context, int text, int duration, int gravity, int offsetx, int offsety, boolean autorun){
        Toast t = Toast.makeText(context.getApplicationContext(), text, duration);
        if(gravity != -1){
            t.setGravity(gravity, offsetx, offsety);
        }
        if(autorun)
            t.show();
        return t;
    }

}

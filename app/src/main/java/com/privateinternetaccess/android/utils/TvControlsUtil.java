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

package com.privateinternetaccess.android.utils;

import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class TvControlsUtil {

    public final static int UP       = 0;
    public final static int LEFT     = 1;
    public final static int RIGHT    = 2;
    public final static int DOWN     = 3;
    public final static int CENTER   = 4;

    public static int getDirectionPressed(InputEvent event) {
        // If the input event is a MotionEvent, check its hat axis values.
        if (event instanceof MotionEvent) {

            // Use the hat axis value to find the D-pad direction
            MotionEvent motionEvent = (MotionEvent) event;
            float xaxis = motionEvent.getAxisValue(MotionEvent.AXIS_HAT_X);
            float yaxis = motionEvent.getAxisValue(MotionEvent.AXIS_HAT_Y);

            // Check if the AXIS_HAT_X value is -1 or 1, and set the D-pad
            // LEFT and RIGHT direction accordingly.
            if (Float.compare(xaxis, -1.0f) == 0) {
                return TvControlsUtil.LEFT;
            } else if (Float.compare(xaxis, 1.0f) == 0) {
                return TvControlsUtil.RIGHT;
            }
            // Check if the AXIS_HAT_Y value is -1 or 1, and set the D-pad
            // UP and DOWN direction accordingly.
            else if (Float.compare(yaxis, -1.0f) == 0) {
                return TvControlsUtil.UP;
            } else if (Float.compare(yaxis, 1.0f) == 0) {
                return TvControlsUtil.DOWN;
            }
        }
        // If the input event is a KeyEvent, check its key code.
        else if (event instanceof KeyEvent) {
            // Use the key code to find the D-pad direction.
            KeyEvent keyEvent = (KeyEvent) event;
            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                return TvControlsUtil.LEFT;
            } else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                return TvControlsUtil.RIGHT;
            } else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                return TvControlsUtil.UP;
            } else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                return TvControlsUtil.DOWN;
            } else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BUTTON_SELECT ||
                    keyEvent.getKeyCode() == KeyEvent.KEYCODE_BUTTON_A ||
                    keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER ||
                    keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER ||
                    keyEvent.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                return TvControlsUtil.CENTER;
            }
        }

        return -1;
    }

}

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

package com.privateinternetaccess.android.model.interfaces;

/**
 * I'm keeping otto out of this because of the very quick nature of this view and the potentials for lag in sending the events and more
 * makes me worried about using it there. Probably not a problem but it works.
 *
 * Created by half47 on 2/5/16.
 */
public interface IAllowedApps {

    boolean contains(String name);

    void toggleApp(String packageName, String name);

    boolean isProblem(String name);

    void appSelected(String name);

    boolean isSelectedApp(String name);
}

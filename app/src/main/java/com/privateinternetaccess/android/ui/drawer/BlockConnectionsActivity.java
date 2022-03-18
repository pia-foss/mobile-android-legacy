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

package com.privateinternetaccess.android.ui.drawer;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import com.privateinternetaccess.android.R;
import com.privateinternetaccess.android.ui.superclasses.BaseActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class BlockConnectionsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block_connection);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.activity_block_connections_button)
    public void onButtonPressed() {
        startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
    }

}

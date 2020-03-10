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

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.VpnService;
import android.os.Bundle;
import android.view.View;

import com.privateinternetaccess.android.PIAApplication;
import com.privateinternetaccess.android.R;
import com.privateinternetaccess.android.pia.utils.DLog;
import com.privateinternetaccess.android.ui.superclasses.BaseActivity;
import com.privateinternetaccess.android.ui.tv.DashboardActivity;

import de.blinkt.openvpn.core.VpnStatus;

/**
 * Created by hfrede on 1/8/18.
 */

public class VPNPermissionActivity extends BaseActivity {

    public static final int VPN_PERMISSION_CODE = 4747;

    public View bOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vpn_permissions);
        bindView();
    }

    private void bindView() {
        bOk = findViewById(R.id.activity_vpn_permissions_button);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initView();
    }

    private void initView() {
        bOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askForPermission();
            }
        });
    }

    private void askForPermission() {
        Intent intent = VpnService.prepare(getApplicationContext());
        if (intent != null) {
            // Start the query
            try {
                startActivityForResult(intent, VPN_PERMISSION_CODE);
            } catch (ActivityNotFoundException ane) {
                // Shame on you Sony! At least one user reported that
                // an official Sony Xperia Arc S image triggers this exception
                VpnStatus.logError(R.string.no_vpn_support_image);
            }
        } else {
            goToMainActivity(RESULT_CANCELED);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        DLog.d("VPNPermissionActivity", "request = " + requestCode + " result = " + resultCode);
        goToMainActivity(resultCode);
    }

    private void goToMainActivity(int resultCode) {
        if(resultCode == RESULT_OK) {
            overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
            if (getIntent().hasExtra(MainActivity.START_VPN_SHORTCUT))
                setResult(resultCode);
            else if (PIAApplication.isAndroidTV(getApplicationContext())){
                Intent i = new Intent(getApplicationContext(), DashboardActivity.class);
                startActivity(i);
            }
            else {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
            }
            finish();
        } else if(resultCode == RESULT_CANCELED) {
            Context context = this;
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("");
            builder.setMessage(R.string.permissions_message);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    askForPermission();
                }
            });
            builder.setNegativeButton(R.string.contact, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    String supportEmail = "helpdesk+vpnpermissions.android@privateinternetaccess.com";
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto",supportEmail, null));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Android VPN Permission Question");
                    startActivity(Intent.createChooser(emailIntent, getString(R.string.send_email)));
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }
}
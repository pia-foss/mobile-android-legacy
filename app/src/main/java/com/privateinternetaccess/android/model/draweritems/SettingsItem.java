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

package com.privateinternetaccess.android.model.draweritems;

import android.content.Context;
import android.view.View;
import android.widget.CompoundButton;

import com.privateinternetaccess.android.pia.utils.Prefs;
import com.privateinternetaccess.android.ui.adapters.SettingsWidgetAdapter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class SettingsItem {
    public enum SettingsType {
        TOGGLE,
        ACTION,
        DIALOG,
        OPTIONS_DIALOG,
        TEXT_DIALOG,
        CUSTOM,
        CATEGORY
    }

    public String key;
    public int title;
    public int summary;

    private SettingsType itemType;

    public void setType(SettingsType type) {
        itemType = type;
    }

    public void setType(SettingsType type, XmlPullParser parser) throws XmlPullParserException, IOException {
        itemType = type;
    }

    public void setupHolder(SettingsWidgetAdapter.SettingsHolder holder, Context context) {
        //If a resource is 0, we have no string for that field
        if (title == 0) {
            holder.tvTitle.setVisibility(View.GONE);
        }
        else {
            holder.tvTitle.setText(context.getString(title));
        }

        if (summary == 0) {
            holder.tvSummary.setVisibility(View.GONE);
        }
        else {
            holder.tvSummary.setText(context.getString(summary));
        }

        holder.sToggle.setVisibility(View.GONE);

        if (itemType != null) {
            switch (itemType) {
                case TOGGLE:
                    setupToggle(holder, context);
                    break;
                default:
                    break;
            }
        }
    }

    private void setupToggle(final SettingsWidgetAdapter.SettingsHolder holder, Context context) {
        final Prefs prefs = Prefs.with(context);
        boolean value = prefs.getBoolean(key);

        holder.sToggle.setChecked(value);
        holder.sToggle.setVisibility(View.VISIBLE);

        holder.sToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                prefs.set(key, b);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.sToggle.toggle();
            }
        });
    }

}

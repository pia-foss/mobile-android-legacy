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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.privateinternetaccess.android.R;
import com.privateinternetaccess.android.ui.drawer.settings.SettingsActivity;
import com.privateinternetaccess.android.ui.drawer.settings.colorpicker.ColorPickerPreference;
import com.privateinternetaccess.android.ui.widgets.WidgetBaseProvider;

import me.philio.preferencecompatextended.PreferenceFragmentCompat;

/**
 * Created by hfrede on 9/15/17.
 */

public class WidgetSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.fragment_preferences_widget);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.settings_widget_configuration);
        ((SettingsActivity) getActivity()).showHideActionBar(true);
        PreferenceManager.getDefaultSharedPreferences(findPreference("widgetBackgroundColor").getContext()).registerOnSharedPreferenceChangeListener(this);
        setColorPickerInfo();
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(findPreference("widgetBackgroundColor").getContext()).unregisterOnSharedPreferenceChangeListener(this);
    }

    private void setColorPickerInfo() {
        ((ColorPickerPreference) findPreference("widgetBackgroundColor")).setActivity(getActivity());
        ((ColorPickerPreference) findPreference("widgetTextColor")).setActivity(getActivity());
        ((ColorPickerPreference) findPreference("widgetUploadColor")).setActivity(getActivity());
        ((ColorPickerPreference) findPreference("widgetDownloadColor")).setActivity(getActivity());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        WidgetBaseProvider.updateWidget(getActivity(), false);
    }
}

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

import android.Manifest;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.privateinternetaccess.android.PIAApplication;
import com.privateinternetaccess.android.R;
import com.privateinternetaccess.android.model.events.TVAppBarExpandEvent;
import com.privateinternetaccess.android.model.interfaces.IAllowedApps;
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler;
import com.privateinternetaccess.android.pia.utils.DLog;
import com.privateinternetaccess.android.pia.utils.Prefs;
import com.privateinternetaccess.android.pia.utils.Toaster;
import com.privateinternetaccess.android.ui.adapters.AllowedAppsAdapter;
import com.privateinternetaccess.android.ui.superclasses.BaseActivity;
import com.privateinternetaccess.android.utils.PerAppSettingsUtils;

import org.greenrobot.eventbus.Subscribe;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AllowedAppsActivity extends BaseActivity implements IAllowedApps {

    public static final String EXTRA_SELECT_APP = "SelectApp";
    public static final String ORBOT = "org.torproject.android";

    private AllowedAppsAdapter mListAdapter;
    private HashSet<String> mExcludedApps;
    private Vector<ApplicationInfo> mPackages = new Vector<>();
    private PackageManager mPm;

    @BindView(R.id.appbar) AppBarLayout appBar;
    @BindView(R.id.header_save) View save;
    @BindView(android.R.id.list) RecyclerView rvListView;
    @BindView(R.id.activity_secondary_progress) View pbLoad;
    @BindView(R.id.search) EditText etSearch;
    @BindView(R.id.cancel_icon) ImageView ivCancelSearch;
    @BindView(R.id.allowed_apps_problem_apps_text) TextView tvAppProblemExplanation;

    private LinearLayoutManager layoutManager;

    private Map<String, String> mProblemApps;

    private Toast toastApp;

    private boolean selectApp;
    private String selectedApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secondary_list);
        initHeader(true, true);
        setBackground();
        setSecondaryGreenBackground();

        if(savedInstanceState == null){
            selectApp = getIntent().getBooleanExtra(EXTRA_SELECT_APP, false);
        } else {
            selectApp = savedInstanceState.getBoolean(EXTRA_SELECT_APP);
        }

        bindSnippetsToViews();

        ButterKnife.bind(this);

        showTopExtraArea();
        bindView();
        initView();
    }

    private void bindSnippetsToViews() {
        LinearLayout topArea = findViewById(R.id.activity_secondary_top_add_area);
        View top = getLayoutInflater().inflate(R.layout.snippet_per_app_settings_top, topArea, false);
        topArea.addView(top);
    }

    @Override
    protected void onResume() {
        super.onResume();
        selectedApp = PiaPrefHandler.getProxyApp(getApplicationContext());
        genInternetPackagesList();
        Set<String> prefPackages = PiaPrefHandler.getVpnExcludedApps(getApplicationContext());
        mExcludedApps = new HashSet<>();
        mExcludedApps.addAll(prefPackages);
        if(mListAdapter != null) {
            mListAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        saveSelectedApps();
        if(toastApp != null){
            toastApp.cancel();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA_SELECT_APP, selectApp);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            onBackPressed();
        }
        return super.onKeyDown(keyCode, event);
    }

    int position;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(PIAApplication.isAndroidTV(getApplicationContext())) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                    if (getCurrentFocus() instanceof GridLayout) {
                        handlerKeyEvent(event);
                    }
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public boolean handlerKeyEvent(KeyEvent event){
        int difference = 1;
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP)
            difference = -1;
        DLog.d("ServerList", "dif = " + difference);
        return mListAdapter.tryMoveSelection(difference);
    }

    private void bindView() {
        // set remote view the way we want in per apps
        rvListView.setPadding(0,0,0,0);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) rvListView.getLayoutParams();
        params.setMargins(0, 5,0,0);

        rvListView.setLayoutParams(params);

        if(PIAApplication.isAndroidTV(getApplicationContext()))
            etSearch.setBackgroundResource(R.drawable.shape_standard_background);
    }

    private void initView() {
        layoutManager = new LinearLayoutManager(rvListView.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        rvListView.setHasFixedSize(true);
        rvListView.setLayoutManager(layoutManager);

        mListAdapter = new AllowedAppsAdapter(getApplicationContext(), this, selectApp);
        rvListView.setAdapter(mListAdapter);

        etSearch.setHint(R.string.search_apps);

        hideIconButton();
        initToggleAndFilter();
        initClearSearch();
        initText();

        if(!selectApp) {
            setTitle(getString(R.string.per_app_settings));
            save.setVisibility(View.GONE);
        } else {
            setTitle(getString(R.string.proxy_selection_title));
            save.setVisibility(View.VISIBLE);
            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Prefs prefs = new Prefs(getApplicationContext());
                    if(!TextUtils.isEmpty(selectedApp)) {
                        String previousSelected = PiaPrefHandler.getProxyApp(getApplicationContext());
                        if(!selectedApp.equals(previousSelected)) {
                            if (!TextUtils.isEmpty(previousSelected)) {
                                mExcludedApps.remove(previousSelected);
                            }
                            mExcludedApps.add(selectedApp);
                            saveSelectedApps();

                            if (selectedApp.equals(ORBOT)) {
                                prefs.set(PiaPrefHandler.PROXY_ORBOT, true);
                            } else {
                                prefs.set(PiaPrefHandler.PROXY_ORBOT, false);
                            }

                            PiaPrefHandler.setProxyApp(getApplicationContext(), selectedApp);
                        }
                        finish();
                    } else
                        Toaster.s(getApplicationContext(), R.string.app_must_be_selected);
                }
            });
        }

    }

    private void initToggleAndFilter() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    if (mListAdapter != null) {
                        mListAdapter.filter(etSearch.getText().toString());
                    }
                } catch (Exception e) {
                    if(mListAdapter == null){
                        rvListView.setAdapter(null);
                        mListAdapter = new AllowedAppsAdapter(getApplicationContext(), AllowedAppsActivity.this, selectApp);
                    }
                    if(mPackages != null) {
                        mListAdapter.setmPackages(mPackages);
                        mListAdapter.filter(etSearch.getText().toString());
                    } else {
                        genInternetPackagesList();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void initClearSearch() {
        ivCancelSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etSearch.setText("");
            }
        });
    }

    private void initText() {
        Spannable wordtoSpan = new SpannableString(getString(R.string.per_app_warning));
        wordtoSpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getApplicationContext(), R.color.pia_gen_red)), 16, 19, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvAppProblemExplanation.setText(wordtoSpan);
    }

    private void genInternetPackagesList() {
        pbLoad.setVisibility(View.VISIBLE);

        mPm = getPackageManager();
        Vector<ApplicationInfo> apps = new Vector<>();
        List<ApplicationInfo> appInfoList = PerAppSettingsUtils.INSTANCE.getInstalledApps(mPm);

        boolean thereIsAProblemApp = false;
        for (ApplicationInfo appInfo : appInfoList) {
            if (!PerAppSettingsUtils.INSTANCE.containsPackageName(apps, appInfo.packageName) &&
                    !appInfo.packageName.equals(getPackageName())) {
                apps.add(appInfo);
                if(isProblem(appInfo.packageName))
                    thereIsAProblemApp = true;
            }
        }

        final boolean isthereaproblem = thereIsAProblemApp;
        tvAppProblemExplanation.post(new Runnable() {
            @Override
            public void run() {
                if(isthereaproblem){
                    tvAppProblemExplanation.setVisibility(View.VISIBLE);
                } else {
                    tvAppProblemExplanation.setVisibility(View.GONE);
                }
            }
        });

        Collections.sort(apps, new ApplicationInfo.DisplayNameComparator(mPm));

        mPackages = apps;

        rvListView.post(new Runnable() {
            @Override
            public void run() {
                pbLoad.setVisibility(View.GONE);
                if (mListAdapter != null) {
                    mListAdapter.setmPackages(mPackages);
                    if(!TextUtils.isEmpty(etSearch.getText().toString())){
                        mListAdapter.filter(etSearch.getText().toString());
                    }

                }
            }
        });
    }

    private void saveSelectedApps() {
        DLog.d("SaveSelectedApps", "excludedSet = " + mExcludedApps);
        PiaPrefHandler.setVpnExcludedApps(getApplicationContext(), mExcludedApps);
    }

    /**
     * returns if the app is not in the excluded list.
     *
     */
    @Override
    public boolean contains(String name) {
        return mExcludedApps.contains(name);
    }

    @Override
    public void toggleApp(String packageName, String name) {
        if(mExcludedApps != null){
            int resId;
                if (mExcludedApps.contains(packageName)) {
                    if(!isSelectedApp(packageName)) {
                        DLog.d("openvpn", "removing from excluded apps " + packageName);
                        mExcludedApps.remove(packageName);
                        resId = R.string.allowed_apps_toast_removed;
                    } else {
                        DLog.d("openvpn", "not removing app because its the proxy app");
                        resId = R.string.app_is_proxy;
                    }
                } else {
                    DLog.d("openvpn", "adding to excluded apps " + packageName);
                    mExcludedApps.add(packageName);
                    resId = R.string.allowed_apps_toast_added;
            }

            if(toastApp != null)
                toastApp.cancel();

            String message = getString(resId, name);
            toastApp = Toaster.s(getApplicationContext(), message);

            saveSelectedApps();
        }
    }

    @Override
    public boolean isProblem(String name) {
        if(mProblemApps == null) {
            mProblemApps = new HashMap<>();
            mProblemApps.put("com.estrongs.android.pop", "ES File explorer");
            mProblemApps.put("com.ape.apps.networkbrowser", "Network Browser");
        }
        return mProblemApps.containsKey(name);
    }

    @Override
    public void appSelected(String name) {
        selectedApp = name;
    }

    @Override
    public boolean isSelectedApp(String name) {
        return name.equals(selectedApp);
    }

    @Subscribe
    public void onAppBarEventRecieve(TVAppBarExpandEvent event){
        if(!event.isOpen()){
            appBar.setExpanded(false, true);
        } else {
            appBar.setExpanded(true, true);
        }
    }
}
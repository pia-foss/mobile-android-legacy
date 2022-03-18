package com.privateinternetaccess.android.ui.drawer;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.privateinternetaccess.account.model.response.DedicatedIPInformationResponse;
import com.privateinternetaccess.android.R;
import com.privateinternetaccess.android.pia.PIAFactory;
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler;
import com.privateinternetaccess.android.pia.interfaces.IAccount;
import com.privateinternetaccess.android.pia.model.enums.RequestResponseStatus;
import com.privateinternetaccess.android.pia.utils.Toaster;
import com.privateinternetaccess.android.ui.adapters.DedicatedIPAdapter;
import com.privateinternetaccess.android.ui.connection.MainActivityHandler;
import com.privateinternetaccess.android.ui.superclasses.BaseActivity;
import com.privateinternetaccess.android.utils.DedicatedIpUtils;
import com.privateinternetaccess.core.model.PIAServer;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DedicatedIPActivity extends BaseActivity {

    @BindView(R.id.snippet_dip_entry_field) EditText etDipToken;
    @BindView(R.id.snippet_dip_list) RecyclerView recyclerView;
    @BindView(R.id.snippet_dip_list_layout) LinearLayout lList;
    @BindView(R.id.snippet_dip_top_summary) LinearLayout lTopSummary;
    @BindView(R.id.snippet_dip_top_frame) LinearLayout lTopAddDIPFrame;

    @BindView(R.id.snippet_dip_progress_bar) ProgressBar progressBar;
    @BindView(R.id.snippet_dip_activate_button) Button bActivate;

    private DedicatedIPAdapter mAdapter;
    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secondary);

        initHeader(true, true);
        setTitle(getString(R.string.dip_menu_title));
        setBackground();
        setSecondaryGreenBackground();

        addSnippetToView();

        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupList();
    }

    private void addSnippetToView() {
        FrameLayout container = findViewById(R.id.activity_secondary_container);
        View view = getLayoutInflater().inflate(R.layout.snippet_dedicated_ip, container, false);
        container.addView(view);
    }

    private void setupList() {
        List<DedicatedIPInformationResponse.DedicatedIPInformation> ipList = PiaPrefHandler.getDedicatedIps(this);
        List<PIAServer> serverList = new ArrayList<>();

        if (ipList.size() <= 0) {
            lList.setVisibility(View.GONE);
            lTopSummary.setVisibility(View.GONE);
            lTopAddDIPFrame.setVisibility(View.VISIBLE);
        }
        else {
            lList.setVisibility(View.VISIBLE);

            if (PiaPrefHandler.isFeatureActive(this, MainActivityHandler.DIP_DISABLE_MULTIPLE_TOKENS)) {
                lTopAddDIPFrame.setVisibility(View.GONE);
                lTopSummary.setVisibility(View.VISIBLE);
            }

            for (DedicatedIPInformationResponse.DedicatedIPInformation dip : ipList) {
                PIAServer server = DedicatedIpUtils.serverForDip(dip, this);
                if (server != null) {
                    serverList.add(server);
                }
            }

            mAdapter = new DedicatedIPAdapter(this, serverList);
            layoutManager = new LinearLayoutManager(this);

            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(mAdapter);
        }
    }

    public void removeDip(DedicatedIPInformationResponse.DedicatedIPInformation dip) {
        PiaPrefHandler.removeDedicatedIp(this, dip);
        PiaPrefHandler.removeFavorite(this, dip.getIp());
        setupList();
    }

    @OnClick(R.id.snippet_dip_activate_button)
    public void onActivatePressed() {
        String dipToken = etDipToken.getText().toString();

        if (!TextUtils.isEmpty(dipToken)) {
            List<String> ipTokens = new ArrayList<>();
            ipTokens.add(dipToken);

            progressBar.setVisibility(View.VISIBLE);
            bActivate.setVisibility(View.INVISIBLE);

            IAccount account = PIAFactory.getInstance().getAccount(this);
            account.dedicatedIPs(ipTokens, (details, requestResponseStatus) -> {
                progressBar.setVisibility(View.INVISIBLE);
                bActivate.setVisibility(View.VISIBLE);

                if (requestResponseStatus != RequestResponseStatus.SUCCEEDED) {
                    etDipToken.setText("");
                    Toaster.l(this, R.string.dip_invalid);
                    return null;
                }

                for (DedicatedIPInformationResponse.DedicatedIPInformation ip : details) {
                    PIAServer server = DedicatedIpUtils.serverForDip(ip, this);
                    if (server == null) {
                        Toaster.l(this, R.string.dip_invalid);
                        continue;
                    }

                    if (ip.getStatus() == DedicatedIPInformationResponse.Status.active) {
                        PiaPrefHandler.addDedicatedIp(this, ip);
                        Toaster.l(this, R.string.dip_success);
                    }
                    else if (ip.getStatus() == DedicatedIPInformationResponse.Status.expired) {
                        Toaster.l(this, R.string.dip_expired_warning);
                    }
                    else if (ip.getStatus() == DedicatedIPInformationResponse.Status.invalid) {
                        Toaster.l(this, R.string.dip_invalid);
                    }
                }

                DedicatedIpUtils.refreshTokensAndInAppMessages(this);
                etDipToken.setText("");
                setupList();
                return null;
            });
        }
    }
}

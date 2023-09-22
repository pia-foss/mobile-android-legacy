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

package com.privateinternetaccess.android.ui.loginpurchasing;

import static com.privateinternetaccess.android.ui.features.WebviewActivity.PRIVACY_POLICY;
import static com.privateinternetaccess.android.ui.features.WebviewActivity.TERMS_OF_USE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.billingclient.api.ProductDetails;
import com.privateinternetaccess.android.BuildConfig;
import com.privateinternetaccess.android.PIAApplication;
import com.privateinternetaccess.android.R;
import com.privateinternetaccess.android.handlers.PurchasingHandler;
import com.privateinternetaccess.android.handlers.UpdateHandler;
import com.privateinternetaccess.android.model.events.PricingLoadedEvent;
import com.privateinternetaccess.android.pia.PIAFactory;
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler;
import com.privateinternetaccess.android.pia.interfaces.IAccount;
import com.privateinternetaccess.android.pia.interfaces.IPurchasing;
import com.privateinternetaccess.android.pia.model.PurchaseData;
import com.privateinternetaccess.android.pia.model.PurchaseObj;
import com.privateinternetaccess.android.pia.model.events.PurchasingInfoEvent;
import com.privateinternetaccess.android.pia.model.events.SubscriptionsEvent;
import com.privateinternetaccess.android.pia.model.events.SystemPurchaseEvent;
import com.privateinternetaccess.android.pia.utils.DLog;
import com.privateinternetaccess.android.pia.utils.Toaster;
import com.privateinternetaccess.android.ui.connection.MainActivityHandler;
import com.privateinternetaccess.android.ui.features.WebviewActivity;
import com.privateinternetaccess.android.ui.superclasses.BaseActivity;
import com.privateinternetaccess.android.utils.SubscriptionsUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by half47 on 3/13/17.
 */
public class LoginPurchaseActivity extends BaseActivity {

    public static final String EXTRA_GOTO_PURCHASING = "gotoPurchasing";

    public static final String TAG = "Purchasing";

    private final String EXTRA_MONTHLY_COST = "mMonthlyCost";
    private final String EXTRA_YEARLY_COST = "mYearlyCost";

    public String mMonthlyCost;
    public String mYearlyCost;

    private boolean showPurchasing;
    private String subscriptionType;
    private boolean isSignUpFlow;

    IPurchasing purchasingHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login_purchasing);

        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            mMonthlyCost = savedInstanceState.getString(EXTRA_MONTHLY_COST);
            mYearlyCost = savedInstanceState.getString(EXTRA_YEARLY_COST);
        } else {
            showPurchasing = getIntent().hasExtra(EXTRA_GOTO_PURCHASING);
        }
        setSwipeBackEnable(false);

        if (BuildConfig.FLAVOR_store.equals("noinapp")) {
            UpdateHandler.checkUpdates(this, UpdateHandler.UpdateDisplayType.SHOW_DIALOG);
        }
    }

    private void createIabHelper() {
        if (purchasingHandler == null) {
            String monthlySubscriptionId =
                    SubscriptionsUtils.INSTANCE.getMonthlySubscriptionId(getBaseContext());
            String yearlySubscriptionId =
                    SubscriptionsUtils.INSTANCE.getYearlySubscriptionId(getBaseContext());

            purchasingHandler = new PurchasingHandler();
            List<String> purchases = new ArrayList<>();
            purchases.add(monthlySubscriptionId);
            purchases.add(yearlySubscriptionId);
            purchasingHandler.init(this, purchases, systemPurchaseEvent -> onSystemPurchaseEvent(systemPurchaseEvent), EventBus.getDefault());
        }
    }

    public void showConnectionError() {
        Activity act = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(act);
        builder.setTitle(R.string.api_check_failure_title);
        builder.setMessage(R.string.api_check_message);
        builder.setPositiveButton(R.string.drawer_contact_support, (dialog, which) -> {
            Intent i = new Intent(LoginPurchaseActivity.this, WebviewActivity.class);
            i.putExtra(WebviewActivity.EXTRA_URL, "https://www.privateinternetaccess.com/helpdesk/new-ticket/");
            startActivity(i);
            dialog.dismiss();
        });
        builder.setNeutralButton(R.string.dismiss, (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (purchasingHandler != null)
            purchasingHandler.dispose();
    }

    @Override
    public void onBackPressed() {
        showPurchasing = false;

        Fragment frag = getSupportFragmentManager().findFragmentById(R.id.container);

        if (frag instanceof PurchasingProcessFragment) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.purchasing_sure);
            builder.setMessage(R.string.purchasing_return);
            builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                switchToStart();
                PiaPrefHandler.setHasSetEmail(LoginPurchaseActivity.this, false);
                PiaPrefHandler.clearAccountInformation(LoginPurchaseActivity.this);
            });
            builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss());
            builder.create().show();
        } else {
            super.onBackPressed();
        }
    }

    private void initView() {
        IAccount account = PIAFactory.getInstance().getAccount(this);
        if (account.loggedIn()) {
            goToMainActivity();
            return;
        }

        Fragment frag = getSupportFragmentManager().findFragmentById(R.id.container);
        if (frag == null) {
            if (PIAApplication.isAmazon()) {
                frag = new AmazonGetStartedFragment();
            } else if (!PiaPrefHandler.hasSetEmail(this) && !PiaPrefHandler.isPurchasingProcessDone(this)) {
                switchToPurchasingProcess(true, false, false);
                return;
            } else if (PiaPrefHandler.isFeatureActive(this, MainActivityHandler.FEATURE_NEW_INITIAL_SCREEN)
                    && !BuildConfig.FLAVOR_store.equals("noinapp")) {
                frag = new NewGetStartedFragment();
            } else {
                frag = new GetStartedFragment();
            }
            FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
            trans.replace(R.id.container, frag);
            trans.commit();

            if (showPurchasing) {
                switchToPurchasing();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_MONTHLY_COST, mMonthlyCost);
        outState.putString(EXTRA_YEARLY_COST, mYearlyCost);
    }

    public void switchToStart() {
        runOnUiThread(() -> {
            Fragment frag = new NewGetStartedFragment();
            FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
            trans.replace(R.id.container, frag);
            trans.commit();
        });

    }

    public void switchToPurchasing() {
        runOnUiThread(() -> {
            if (!BuildConfig.FLAVOR_store.equals("playstore")) {
                navigateToBuyVpnSite();
                return;
            }

            PurchasingFragment.open(getSupportFragmentManager().beginTransaction(), false);
        });
    }

    public void switchToRenewal() {
        runOnUiThread(() -> {
            if (BuildConfig.FLAVOR_store.equals("playstore")) {
                PurchasingFragment.open(
                        getSupportFragmentManager().beginTransaction(), true
                );
            } else {
                NoInAppPurchasingFragment.Companion.open(
                        getSupportFragmentManager().beginTransaction()
                );
            }
        });
    }

    public void switchToPurchasingProcess(final boolean fireOffPurchasing, final boolean isTrial, final boolean isLoginWithReceipt) {
        runOnUiThread(() -> {
            KPIShareEventsFragment frag = new KPIShareEventsFragment();
            frag.setFireOffPurchasing(fireOffPurchasing);
            frag.setTrial(isTrial);
            frag.setLoginWithReceipt(isLoginWithReceipt);
            getSupportFragmentManager().beginTransaction().replace(R.id.container, frag).commit();
        });
    }

    public void switchToTrialAccount() {
        runOnUiThread(() -> {
            Fragment frag = new TrialFragment();
            FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
            trans.setCustomAnimations(R.anim.left_to_right, R.anim.right_to_left, R.anim.right_to_left_exit, R.anim.left_to_right_exit);
            trans.replace(R.id.container, frag);
            trans.addToBackStack("trial");
            trans.commit();
        });
    }

    public void switchToLogin() {
        runOnUiThread(() -> {
            LoginFragment frag = new LoginFragment();
            FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
            trans.setCustomAnimations(R.anim.left_to_right, R.anim.right_to_left, R.anim.right_to_left_exit, R.anim.left_to_right_exit);
            trans.replace(R.id.container, frag);
            trans.addToBackStack("login");
            trans.commit();
        });
    }

    public void switchToMagicLogin() {
        runOnUiThread(() -> {
            MagicLoginFragment frag = new MagicLoginFragment();
            FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
            trans.setCustomAnimations(R.anim.left_to_right, R.anim.right_to_left, R.anim.right_to_left_exit, R.anim.left_to_right_exit);
            trans.replace(R.id.container, frag);
            trans.addToBackStack("magic_login");
            trans.commit();
        });
    }

    public void onSubscribeClicked(String subscriptionType) {
        isSignUpFlow = true;
        this.subscriptionType = subscriptionType;
        showPurchasing = false;
        purchasingHandler.getPurchase(false, EventBus.getDefault());

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSubscriptionReceived(PurchaseObj subscription) {
        if (isSignUpFlow) {
            if (!subscription.isValid()) {
                purchasingHandler.purchase(subscriptionType);
            } else {
                if (!PIAApplication.isQA())
                    Toaster.l(getApplicationContext(), R.string.error_active_subscription);
            }
        } else {
            if (!subscription.isValid()) {
                Toaster.l(this, R.string.purchasing_no_subscription);
            } else if (subscription.isValid()) {
                switchToPurchasingProcess(true, false, true);
            } else if (PiaPrefHandler.hasSetEmail(this)) {
                Toaster.l(this, R.string.error_active_subscription);
            } else {
                switchToPurchasingProcess(true, false, false);
            }
        }
    }

    public void onContinuePurchasingClicked(String subscriptionType) {
        if (TextUtils.isEmpty(mMonthlyCost)) {
            showConnectionError();
            return;
        }

        PurchasingFinalizeFragment finalizeFragment = new PurchasingFinalizeFragment();
        PurchasingFinalizeFragment.PRODUCT_ID_SELECTED = subscriptionType;

        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        trans.setCustomAnimations(R.anim.left_to_right, R.anim.right_to_left, R.anim.right_to_left_exit, R.anim.left_to_right_exit);
        trans.replace(R.id.container, finalizeFragment);
        trans.addToBackStack("finalize");
        trans.commit();
    }

    public void goToMainActivity() {
        super.goToMainActivity();
    }

    public void onSystemPurchaseEvent(SystemPurchaseEvent event) {
        if (event.isSuccess()) {
            PiaPrefHandler.setHasSetEmail(this, false);
            switchToPurchasingProcess(true, false, false);
        }
    }

    @Subscribe(sticky = true)
    public void onReceivedSubscriptions(SubscriptionsEvent event) {
        DLog.d(TAG, "received subscription information");
        createIabHelper();
    }

    @Subscribe
    public void onQueryInventoryFinished(PurchasingInfoEvent event) {
        String monthlySubscriptionId =
                SubscriptionsUtils.INSTANCE.getMonthlySubscriptionId(getBaseContext());
        String yearlySubscriptionId =
                SubscriptionsUtils.INSTANCE.getYearlySubscriptionId(getBaseContext());

        ProductDetails monthlySub = purchasingHandler.getProductDetails(monthlySubscriptionId);
        ProductDetails yearlySub = purchasingHandler.getProductDetails(yearlySubscriptionId);

        if (monthlySub != null && monthlySub.getSubscriptionOfferDetails() != null
                && yearlySub != null && yearlySub.getSubscriptionOfferDetails() != null) {

            mMonthlyCost = monthlySub.getSubscriptionOfferDetails().get(0).getPricingPhases()
                    .getPricingPhaseList().get(0).getFormattedPrice();

            for (int i = 0; i < yearlySub.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().size(); i++) {
                if (yearlySub.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(i).getPriceAmountMicros() != 0) {
                    mYearlyCost = yearlySub.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(i).getFormattedPrice();
                }
            }

            EventBus.getDefault().postSticky(new PricingLoadedEvent(mMonthlyCost, mYearlyCost));

            runOnUiThread(() -> {
                PurchasingFragment fragment = getPurchasingFragment();
                if (fragment != null) {
                    fragment.setUpCosts(mMonthlyCost, mYearlyCost);
                }
            });
            IAccount account = PIAFactory.getInstance().getAccount(getApplicationContext());
            PurchaseData data = account.temporaryPurchaseData();
            // If purchasing has failed, retry
            if (data != null) {
                switchToPurchasingProcess(true, false, false);
            }
        }
    }

    public PurchasingFragment getPurchasingFragment() {
        PurchasingFragment login = null;
        Fragment frag = getSupportFragmentManager().findFragmentById(R.id.container);
        if (frag instanceof PurchasingFragment) {
            login = (PurchasingFragment) frag;
        }
        return login;
    }

    public TrialFragment getGiftCardFragment() {
        TrialFragment giftCard = null;
        Fragment frag = getSupportFragmentManager().findFragmentById(R.id.container);
        if (frag instanceof TrialFragment) {
            giftCard = (TrialFragment) frag;
        }
        return giftCard;
    }

    public void refreshCurrencyTexts() {
        PurchasingFragment frag = getPurchasingFragment();
        if (frag != null)
            if (!TextUtils.isEmpty(mMonthlyCost))
                frag.setUpCosts(mMonthlyCost, mYearlyCost);
    }

    public static void setupTypeText(final Context context, TextView tv, String key) {
        if (key != null) {
            if (key.equals(SubscriptionsUtils.INSTANCE.getYearlySubscriptionId(context))) {
                tv.setText(String.format(context.getString(R.string.you_are_purchasing),
                        context.getString(R.string.yearly_only)));
            } else if (key.equals(SubscriptionsUtils.INSTANCE.getMonthlySubscriptionId(context))) {
                tv.setText(String.format(context.getString(R.string.you_are_purchasing),
                        context.getString(R.string.monthly_only)));
            }
        }
    }

    public static void setupToSPPText(final Context context, TextView tv) {
        String ppText = context.getString(R.string.pp_text);
        String tosText = context.getString(R.string.tos_text);

        String tosPPText = String.format(context.getString(R.string.tos_pos_text), tosText, ppText);
        String[] splitTos = tosPPText.split(tosText);
        String[] splitPP = tosPPText.split(ppText);
        int tosStart = splitTos[0].length();
        int ppStart = splitPP[0].length();
        SpannableStringBuilder spanTxt = new SpannableStringBuilder(tosPPText);
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                // tos selected
                DLog.d("PurchasingFragment", "tos");
                Intent i = new Intent(context, WebviewActivity.class);
                i.putExtra(WebviewActivity.EXTRA_URL, TERMS_OF_USE);
                context.startActivity(i);
            }
        }, tosStart, tosStart + tosText.length(), 0);
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                // pp selected
                DLog.d("PurchasingFragment", "pp");
                Intent i = new Intent(context, WebviewActivity.class);
                i.putExtra(WebviewActivity.EXTRA_URL, PRIVACY_POLICY);
                context.startActivity(i);
            }
        }, ppStart, ppStart + ppText.length(), 0);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setText(spanTxt, TextView.BufferType.SPANNABLE);
    }

    public void loginWithReceipt() {
        isSignUpFlow = false;
        purchasingHandler.getPurchase(true, EventBus.getDefault());
    }

    public void navigateToBuyVpnSite() {
        Intent i = new Intent(getApplicationContext(), WebviewActivity.class);
        i.putExtra(WebviewActivity.EXTRA_URL, getString(R.string.buyvpn_url_localized));
        startActivity(i);
    }
}
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

import android.animation.LayoutTransition;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.privateinternetaccess.android.BuildConfig;
import com.privateinternetaccess.android.PIAApplication;
import com.privateinternetaccess.android.R;
import com.privateinternetaccess.android.model.events.PricingLoadedEvent;
import com.privateinternetaccess.android.pia.PIAFactory;
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler;
import com.privateinternetaccess.android.pia.model.events.SubscriptionsEvent;
import com.privateinternetaccess.android.pia.utils.DLog;
import com.privateinternetaccess.android.ui.drawer.settings.DeveloperActivity;
import com.privateinternetaccess.android.ui.startup.StartupContainerFragment;
import com.privateinternetaccess.android.utils.SubscriptionsUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.privateinternetaccess.android.ui.connection.MainActivityHandler.FEATURE_HIDE_PAYMENT;

public class GetStartedFragment extends Fragment {

    @BindView(R.id.activity_login_purchasing_dev_button) View bDev;
    @BindView(R.id.activity_login_purchasing_button_layout) RelativeLayout lButtons;
    @BindView(R.id.activity_login_purchasing_TOS) TextView tvToS;
    @BindView(R.id.activity_login_purchasing_yearly_text) TextView tvYearly;

    @BindView(R.id.subscriptions_request_progress) ProgressBar pbSubscriptionsRequest;
    @BindView(R.id.activity_login_purchasing_buy_button) Button bSignup;
    @BindView(R.id.activity_login_purchasing_all_plans_button) Button bAllPlans;
    @BindView(R.id.activity_login_purchasing_free_trial_text) TextView tvFreeTrial;

    @BindView(R.id.guideline) Guideline glClosed;
    @BindView(R.id.guideline2) Guideline glOpen;

    public static final String TAG = "GetStartedFragment";

    private GestureDetectorCompat gestureListener;
    private int SWIPE_THRESHOLD = 50;
    private int SWIPE_VELOCITY_THRESHOLD = 100;

    private boolean isButtonLayoutOpen = false;
    private boolean pricesLoaded = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int layout = R.layout.fragment_get_started;
        if (PIAApplication.isAndroidTV(getContext())) {
            layout = R.layout.fragment_get_started_tv;
        }
        final View view = inflater.inflate(layout, container, false);
        ButterKnife.bind(this, view);

        // Open layout by default on Android TV
        if (PIAApplication.isAndroidTV(getContext())) {
            openLayout();
        }

        gestureListener = new GestureDetectorCompat(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
                float diffY = event2.getY() - event1.getY();

                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY < 0) {
                        openLayout();
                    }
                    else {
                        closeLayout();
                    }
                }
                return true;
            }
        });

        LoginPurchaseActivity.setupToSPPText(getActivity(), tvToS);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DLog.d(TAG, "Requesting subscriptions? " + SubscriptionsUtils.INSTANCE.isPlayStoreFlavour());
        if (SubscriptionsUtils.INSTANCE.isPlayStoreFlavour()) {
            requestSubscriptions();
        }
    }

    private void requestSubscriptions() {
        bSignup.setEnabled(false);
        pbSubscriptionsRequest.setVisibility(View.VISIBLE);

        DLog.d(TAG, "Requesting subscriptions");
        PIAFactory.getInstance()
                .getAccount(getContext())
                .availableSubscriptions((subscriptionsInformation, responseStatus) -> {
                            boolean successful = false;
                            switch (responseStatus) {
                                case SUCCEEDED:
                                    successful = true;
                                    break;
                                case AUTH_FAILED:
                                case THROTTLED:
                                case OP_FAILED:
                                    break;
                            }

                            DLog.d(TAG, "Requesting subscriptions response: " + responseStatus);
                            bSignup.setEnabled(true);
                            pbSubscriptionsRequest.setVisibility(View.GONE);

                            if (!successful) {
                                DLog.d(TAG, "availableSubscriptions unsuccessful " + responseStatus);
                                return null;
                            }

                            PiaPrefHandler.setAvailableSubscriptions(
                                    getContext(),
                                    subscriptionsInformation
                            );
                            EventBus.getDefault().postSticky(new SubscriptionsEvent());
                            return null;
                        }
                );
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        initView();
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    private void initView() {
        isButtonLayoutOpen = false;
        if(!PIAApplication.isRelease()){
            bDev.setVisibility(View.VISIBLE);
            bDev.setOnClickListener(v -> {
                Intent i = new Intent(getContext(), DeveloperActivity.class);
                startActivity(i);
            });
        } else {
            bDev.setVisibility(View.GONE);
        }

        if (PIAApplication.isAmazon()) {
            bAllPlans.setVisibility(View.GONE);
            bSignup.setVisibility(View.GONE);
            tvYearly.setVisibility(View.GONE);
            tvFreeTrial.setVisibility(View.GONE);
        }

        lButtons.setOnTouchListener((v, event) -> {
            gestureListener.onTouchEvent(event);
            return false;
        });
        lButtons.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        Fragment frag = new StartupContainerFragment();
        FragmentTransaction trans = getChildFragmentManager().beginTransaction();
        trans.replace(R.id.activity_login_purchasing_container, frag);
        trans.commit();
    }

    private void openLayout() {
        if (isButtonLayoutOpen)
            return;

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)lButtons.getLayoutParams();
        params.topToBottom = glOpen.getId();
        lButtons.requestLayout();

        isButtonLayoutOpen = true;
    }

    private void closeLayout() {
        if (!isButtonLayoutOpen)
            return;

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)lButtons.getLayoutParams();
        params.topToBottom = glClosed.getId();
        lButtons.requestLayout();

        isButtonLayoutOpen = false;
    }

    private void setUpCosts(String yearly){
        tvYearly.setText(getString(R.string.getstarted_trial_price, yearly));
    }

    @Subscribe(sticky = true)
    public void loadPricing(PricingLoadedEvent event) {
        pricesLoaded = !TextUtils.isEmpty(event.yearlyCost);
        setUpCosts(event.yearlyCost);
    }

    @OnClick(R.id.activity_login_purchasing_buy_button)
    public void buyPressed() {
        if (PiaPrefHandler.isFeatureActive(getContext(), FEATURE_HIDE_PAYMENT)) {
            showPaymentError();
            return;
        }

        if (pricesLoaded) {
            ((LoginPurchaseActivity) getActivity()).onSubscribeClicked(
                    SubscriptionsUtils.INSTANCE.getYearlySubscriptionId(getContext())
            );
        } else {
            if (!SubscriptionsUtils.INSTANCE.isPlayStoreFlavour()) {
                ((LoginPurchaseActivity) getActivity()).navigateToBuyVpnSite();
            } else {
                ((LoginPurchaseActivity) getActivity()).showConnectionError();
            }
        }
    }

    @OnClick(R.id.activity_login_purchasing_login_button)
    public void loginPressed() {
        if (getActivity() instanceof LoginPurchaseActivity) {
            ((LoginPurchaseActivity) getActivity()).switchToLogin();
        }
    }

    @OnClick(R.id.activity_login_purchasing_all_plans_button)
    public void allPlansPressed() {
        if (PiaPrefHandler.isFeatureActive(getContext(), FEATURE_HIDE_PAYMENT)) {
            showPaymentError();
            return;
        }

        if (pricesLoaded) {
            ((LoginPurchaseActivity) getActivity()).switchToPurchasing();
        } else {
            if (!SubscriptionsUtils.INSTANCE.isPlayStoreFlavour()) {
                ((LoginPurchaseActivity) getActivity()).navigateToBuyVpnSite();
            } else {
                ((LoginPurchaseActivity) getActivity()).showConnectionError();
            }
        }
    }

    @OnClick(R.id.activity_login_purchasing_redeem_button)
    public void trialPressed() {
        if (getActivity() instanceof LoginPurchaseActivity) {
            ((LoginPurchaseActivity) getActivity()).switchToTrialAccount();
        }
    }

    private void showPaymentError() {
        AlertDialog.Builder ab = new AlertDialog.Builder(getContext());
        ab.setMessage(R.string.hide_payment);
        ab.setPositiveButton(R.string.ok, (dialogInterface, i) -> dialogInterface.dismiss());
        ab.setOnCancelListener(dialog -> onDestroy());
        ab.show();
    }

}

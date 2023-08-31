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

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;

import com.privateinternetaccess.account.model.response.SignUpInformation;
import com.privateinternetaccess.android.PIAApplication;
import com.privateinternetaccess.android.R;
import com.privateinternetaccess.android.pia.PIAFactory;
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler;
import com.privateinternetaccess.android.pia.interfaces.IAccount;
import com.privateinternetaccess.android.pia.model.AmazonPurchaseData;
import com.privateinternetaccess.android.pia.model.TrialData;
import com.privateinternetaccess.android.pia.model.enums.RequestResponseStatus;
import com.privateinternetaccess.android.pia.utils.AppUtilities;
import com.privateinternetaccess.android.pia.utils.DLog;
import com.privateinternetaccess.android.ui.superclasses.BaseActivity;
import com.privateinternetaccess.android.ui.views.PiaxEditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by hfrede on 8/24/17.
 */

public class PurchasingProcessFragment extends Fragment {

    private static final String TAG = "PurchaseProcess";

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public @BindView(R.id.fragment_purchase_process_progress_area)
    LinearLayout aProgress;
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public @BindView(R.id.fragment_purchase_process_success_area)
    View aSuccess;
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public @BindView(R.id.fragment_purchase_process_failure_area)
    View aFailure;
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public @BindView(R.id.fragment_purchase_process_email_area)
    LinearLayout aEmail;
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public @BindView(R.id.fragment_purchase_process_button)
    Button button;
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public @BindView(R.id.fragment_purchase_process_button_progress)
    View progress;
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public @BindView(R.id.fragment_success_redeemed_username)
    TextView tvUsername;
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public @BindView(R.id.fragment_success_redeemed_password)
    TextView tvPassword;
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public @BindView(R.id.fragment_purchase_process_failure_title)
    TextView tvFailureTitle;
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public @BindView(R.id.fragment_purchase_process_failure_text)
    TextView tvFailureMessage;
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public @BindView(R.id.fragment_purchasing_email)
    PiaxEditText etEmail;

    private boolean firePurchasing;
    private boolean isTrial;
    private boolean loggingIn = false;
    private boolean isLoginWithReceipt = false;
    private String temporaryPassword;
    private IAccount account;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_purchase_process, container, false);
        ButterKnife.bind(this, view);

        etEmail.etMain.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        account = PIAFactory.getInstance().getAccount(getContext());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initView();
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public void setAccount(IAccount account) {
        this.account = account;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public boolean hasToken() {
        String token = PIAFactory.getInstance().getAccount(getContext()).vpnToken();
        return !TextUtils.isEmpty(token);
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public boolean hasTempPassword() {
        return !TextUtils.isEmpty(temporaryPassword);
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public boolean hasEmail() {
        return PiaPrefHandler.hasSetEmail(getContext());
    }

    private void initView() {
        if (loggingIn) {
            return;
        }

        if (!hasToken()) {
            showProgress();
            if (firePurchasing) {
                firePurchasing = false;
                notifySubscriptionToBackend();
            }
        } else if (!hasEmail()) {
            onSubscriptionSuccess();
        } else if (hasTempPassword()) {
            onSuccess();
        } else {
            onFailure();
        }
    }

    private void showProgress() {
        aProgress.setVisibility(View.VISIBLE);
        aSuccess.setVisibility(View.GONE);
        aFailure.setVisibility(View.GONE);
        aEmail.setVisibility(View.GONE);
        button.setVisibility(View.GONE);
    }

    private void onSubscriptionSuccess() {
        aProgress.setVisibility(View.GONE);
        aSuccess.setVisibility(View.GONE);
        aFailure.setVisibility(View.GONE);
        aEmail.setVisibility(View.VISIBLE);
        button.setVisibility(View.GONE);
    }

    private void onSuccess() {
        String username = PiaPrefHandler.getLogin(getContext());

        tvUsername.setText(username);
        tvPassword.setText(temporaryPassword);

        button.setVisibility(View.VISIBLE);
        button.setText(R.string.get_started);

        button.setOnClickListener(v -> {
            ((BaseActivity) getActivity()).goToMainActivity();
            button.setVisibility(View.INVISIBLE);
            progress.setVisibility(View.VISIBLE);
            temporaryPassword = null;
        });

        loggingIn = true;

        aProgress.setVisibility(View.GONE);
        aSuccess.setVisibility(View.VISIBLE);
        aFailure.setVisibility(View.GONE);
        aEmail.setVisibility(View.GONE);
    }

    private void onFailure() {
        if (isTrial) {
            onTrialFailure(null);
        } else {
            onPurchaseFailure();
        }
    }

    private void onPurchaseFailure() {
        Context context = getContext();
        boolean connected = PIAApplication.isNetworkAvailable(context);
        if (!connected) {
            tvFailureTitle.setText(R.string.no_internet_title);
            tvFailureMessage.setText(R.string.no_internet_message);

            button.setText(R.string.try_again);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    aFailure.setVisibility(View.GONE);
                    aSuccess.setVisibility(View.GONE);
                    aProgress.setVisibility(View.VISIBLE);
                    button.setVisibility(View.INVISIBLE);
                    notifySubscriptionToBackend();
                }
            });
        } else {
            tvFailureTitle.setText(R.string.account_creation_failure);
            tvFailureMessage.setText(R.string.account_creation_failure_message_no_ticket_id);

            button.setText(R.string.go_to_login);
            button.setOnClickListener(view -> {
                if (getActivity() instanceof LoginPurchaseActivity) {
                    ((LoginPurchaseActivity) getActivity()).switchToStart();
                    PiaPrefHandler.clearAccountInformation(context);
                    PiaPrefHandler.clearPurchasingInfo(context);
                }
            });

        }

        button.setVisibility(View.VISIBLE);
        aProgress.setVisibility(View.GONE);
        aSuccess.setVisibility(View.GONE);
        aFailure.setVisibility(View.VISIBLE);
    }

    private void onTrialFailure(String code) {
        if (code != null) {
            int titleResId;
            int messageResId;
            switch (code) {
                case "bad_email":  // Invalid email address
                    titleResId = R.string.trial_failure_email_title;
                    messageResId = R.string.trial_failure_email_message;
                    break;
                case "redeemed":  // Pin code already redeemed
                    titleResId = R.string.trial_failure_redeemed_title;
                    messageResId = R.string.trial_failure_redeemed_message;
                    break;
                case "not_found":
                case "canceled":  // Pin code not found or pin code canceled
                    titleResId = R.string.trial_failure_invalid_title;
                    messageResId = R.string.trial_failure_invalid_message;
                    break;
                default:  // throttled
                    titleResId = R.string.trial_failure_throttled_title;
                    messageResId = R.string.trial_failure_throttled_message;
                    break;
            }

            tvFailureTitle.setText(titleResId);
            tvFailureMessage.setText(messageResId);
        }

        button.setText(R.string.try_again);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        button.setVisibility(View.VISIBLE);
        aProgress.setVisibility(View.GONE);
        aSuccess.setVisibility(View.GONE);
        aFailure.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.fragment_purchasing_email_submit)
    public void submitEmail() {
        Context context = getContext();
        String email = etEmail.getText();
        if (!AppUtilities.isValidEmail(email)) {
            etEmail.setError(getString(R.string.invalid_email_signup));
            return;
        }

        etEmail.setError(null);
        PiaPrefHandler.saveLoginEmail(context, email);

        account.updateEmail(
                email,
                false,
                (tempPassword, requestResponseStatus) -> {
                    if (requestResponseStatus != RequestResponseStatus.SUCCEEDED) {
                        DLog.d(TAG, "updateEmail unsuccessful " + requestResponseStatus);
                        onFailure();
                        return null;
                    }

                    PiaPrefHandler.setHasSetEmail(context, true);
                    initView();
                    return null;
                }
        );
        showProgress();
    }

    public void notifySubscriptionToBackend() {
        if (isTrial) {
            TrialData data = PiaPrefHandler.getTempTrialData(requireContext());
            String email = data.getEmail();
            String pin = data.getPin();
            account.createTrialAccount(email, pin, (username, tempPassword, message, responseStatus) -> {
                handleTrialAccountResponse(username, tempPassword, message, responseStatus);
                return null;
            });
        } else if (isLoginWithReceipt) {
            isLoginWithReceipt = false;
            if (PIAApplication.isAmazon()) {
                final AmazonPurchaseData data = PiaPrefHandler.getAmazonPurchaseData(requireContext());
                if (data != null) {
                    account.amazonLoginWithReceipt(data.getReceiptId(), data.getUserId(), (loginRequestResponseStatus) -> {
                        handleLoginWithReceipt(loginRequestResponseStatus);
                        return null;
                    });
                }
            } else {
                account.loginWithReceipt(
                        PiaPrefHandler.getPurchasingToken(requireContext()),
                        PiaPrefHandler.getPurchasingSku(requireContext()),
                        (loginRequestResponseStatus) -> {
                            handleLoginWithReceipt(loginRequestResponseStatus);
                            return null;
                        }
                );
            }
        } else {
            if (PIAApplication.isAmazon()) {
                final AmazonPurchaseData data = PiaPrefHandler.getAmazonPurchaseData(requireContext());
                account.amazonSignUp(data.getUserId(), data.getReceiptId(), (signUpInformation, responseStatus) -> {
                    handleAmazonSignUpResponse(signUpInformation, responseStatus);
                    return null;
                });
            } else {
                String orderId = PiaPrefHandler.getPurchasingOrderId(requireContext());
                String token = PiaPrefHandler.getPurchasingToken(requireContext());
                String sku = PiaPrefHandler.getPurchasingSku(requireContext());
                account.signUp(orderId, token, sku, (signUpInformation, responseStatus) -> {
                    handleSignUpResponse(signUpInformation, responseStatus);
                    return null;
                });
            }
        }
    }

    private void handleTrialAccountResponse(
            String username,
            String tempPassword,
            String message,
            RequestResponseStatus requestResponseStatus
    ) {
        Context context = getContext();
        boolean successful = false;
        switch (requestResponseStatus) {
            case SUCCEEDED:
                successful = true;
                break;
            case AUTH_FAILED:
            case THROTTLED:
            case OP_FAILED:
                break;
        }

        if (!successful) {
            DLog.d(TAG, "createTrialAccount unsuccessful " + requestResponseStatus);
            onTrialFailure(message);
            return;
        }

        temporaryPassword = tempPassword;
        PiaPrefHandler.setLogin(context, username);
        PiaPrefHandler.cleanTempTrialData(context);
        account.loginWithCredentials(
                username,
                tempPassword,
                (loginRequestResponseStatus) -> {
                    handleAuthenticationResponse(loginRequestResponseStatus);
                    return null;
                }
        );
    }

    private void handleSignUpResponse(
            SignUpInformation information,
            RequestResponseStatus signUpRequestResponseStatus
    ) {
        Context context = getContext();
        if (signUpRequestResponseStatus != RequestResponseStatus.SUCCEEDED) {
            DLog.d(TAG, "handleSignUpResponse unsuccessful " + signUpRequestResponseStatus);
            onFailure();
            return;
        }

        temporaryPassword = information.getPassword();
        PiaPrefHandler.setLogin(context, information.getUsername());
        account.loginWithReceipt(
                PiaPrefHandler.getPurchasingToken(context),
                PiaPrefHandler.getPurchasingSku(context),
                (loginRequestResponseStatus) -> {
                    handleAuthenticationResponse(loginRequestResponseStatus);
                    return null;
                }
        );
    }

    private void handleAmazonSignUpResponse(
            SignUpInformation information,
            RequestResponseStatus signUpRequestResponseStatus
    ) {
        Context context = getContext();
        if (signUpRequestResponseStatus != RequestResponseStatus.SUCCEEDED) {
            DLog.d(TAG, "handleAmazonSignUpResponse unsuccessful " + signUpRequestResponseStatus);
            onFailure();
            return;
        }

        temporaryPassword = information.getPassword();
        PiaPrefHandler.setLogin(context, information.getUsername());
        account.loginWithCredentials(information.getUsername(), information.getPassword(), (loginResponse) -> {
            handleAuthenticationResponse(loginResponse);
            return null;
        });
    }

    private void handleAuthenticationResponse(RequestResponseStatus requestResponseStatus) {
        if (requestResponseStatus != RequestResponseStatus.SUCCEEDED) {
            DLog.d(TAG, "handleAuthenticationResponse unsuccessful " + requestResponseStatus);
            onFailure();
            ((LoginPurchaseActivity) getActivity()).switchToLogin();
            return;
        }

        PiaPrefHandler.clearPurchasingInfo(requireContext());
        PiaPrefHandler.setUserIsLoggedIn(requireContext(), true);
        initView();
    }

    private void handleLoginWithReceipt(RequestResponseStatus requestResponseStatus) {
        if (requestResponseStatus != RequestResponseStatus.SUCCEEDED) {
            DLog.d(TAG, "handleLoginWithReceipt unsuccessful " + requestResponseStatus);
            onFailure();
            ((LoginPurchaseActivity) requireActivity()).switchToLogin();
            return;
        }

        PiaPrefHandler.clearPurchasingInfo(requireContext());
        PiaPrefHandler.setUserIsLoggedIn(requireContext(), true);
        ((LoginPurchaseActivity) requireActivity()).goToMainActivity();
    }

    public void setFirePurchasing(boolean firePurchasing) {
        this.firePurchasing = firePurchasing;
    }

    public void setTrial(boolean trial) {
        isTrial = trial;
    }

    public void setLoginWithReceipt(boolean loginWithReceipt) {
        isLoginWithReceipt = loginWithReceipt;
    }
}
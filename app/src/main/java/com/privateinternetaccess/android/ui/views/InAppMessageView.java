package com.privateinternetaccess.android.ui.views;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.privateinternetaccess.android.R;
import com.privateinternetaccess.android.model.events.DedicatedIPUpdatedEvent;
import com.privateinternetaccess.android.utils.InAppMessageManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class InAppMessageView extends FrameLayout {

    @BindView(R.id.view_inapp_text) TextView tvMessage;

    public InAppMessageView(Context context) {
        super(context);
        init(context);
    }

    public InAppMessageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public InAppMessageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        inflate(context, R.layout.view_inapp_message, this);
        ButterKnife.bind(this, this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);
        showMessage();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    @OnClick(R.id.view_inapp_close)
    public void onDismissClicked() {
        InAppMessageManager.dismissMessage(getContext());
        showMessage();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void dedicatedIPUpdatedEvent(DedicatedIPUpdatedEvent event) {
        showMessage();
    }

    // region private
    private void showMessage() {
        if (!InAppMessageManager.hasQueuedMessages()) {
            this.setVisibility(View.GONE);
            return;
        }

        this.setVisibility(View.VISIBLE);
        SpannableStringBuilder localizedMessage = InAppMessageManager.showMessage(getContext());
        tvMessage.setMovementMethod(LinkMovementMethod.getInstance());
        tvMessage.setText(localizedMessage, TextView.BufferType.SPANNABLE);
    }
    // endregion
}

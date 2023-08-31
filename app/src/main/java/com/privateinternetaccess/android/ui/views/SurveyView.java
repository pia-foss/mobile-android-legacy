package com.privateinternetaccess.android.ui.views;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.privateinternetaccess.android.PIAApplication;
import com.privateinternetaccess.android.R;
import com.privateinternetaccess.android.model.events.SurveyEvent;
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler;
import com.privateinternetaccess.android.ui.features.WebviewActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SurveyView extends FrameLayout {

    private static final int SUCCESSFUL_CONNECTIONS_FOR_SURVEY = 15;

    @BindView(R.id.survey_action)
    TextView action;

    public SurveyView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.view_survey, this);
        ButterKnife.bind(this, this);

        action.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent surveyIntent = new Intent(getContext(), WebviewActivity.class);
                surveyIntent.setData(Uri.parse("survey://"));
                getContext().startActivity(surveyIntent);
                PiaPrefHandler.dismissSurvey(getContext());
            }
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);
        setSurveyVisibility(null);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    @OnClick(R.id.survey_close)
    public void onDismissClicked() {
        PiaPrefHandler.dismissSurvey(getContext());
        setSurveyVisibility(null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void setSurveyVisibility(SurveyEvent event) {
        if (PiaPrefHandler.getSuccessfulConnections(getContext()) >= SUCCESSFUL_CONNECTIONS_FOR_SURVEY
                && !PIAApplication.isAndroidTV(getContext())) {
            this.setVisibility(View.VISIBLE);
        } else {
            this.setVisibility(GONE);
        }
    }
}

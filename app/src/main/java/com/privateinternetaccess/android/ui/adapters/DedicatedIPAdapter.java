package com.privateinternetaccess.android.ui.adapters;

import android.app.Dialog;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.privateinternetaccess.account.model.response.DedicatedIPInformationResponse.DedicatedIPInformation;
import com.privateinternetaccess.android.R;
import com.privateinternetaccess.android.pia.handlers.PIAServerHandler;
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler;
import com.privateinternetaccess.android.pia.utils.DLog;
import com.privateinternetaccess.android.ui.DialogFactory;
import com.privateinternetaccess.android.ui.drawer.DedicatedIPActivity;
import com.privateinternetaccess.core.model.PIAServer;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class DedicatedIPAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "DedicatedIPAdapter";

    private Context mContext;
    private List<PIAServer> ipList;

    private PIAServerHandler serverHandler;

    public DedicatedIPAdapter(Context context, List<PIAServer> list) {
        this.mContext = context;
        this.ipList = list;

        serverHandler = PIAServerHandler.getInstance(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_dedicated_ip, parent, false);
        return new DedicatedIpHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (ipList.isEmpty()) {
            DLog.w(TAG, "Server list is empty");
            return;
        }

        final PIAServer server = ipList.get(position);
        if (server == null) {
            DLog.e(TAG, "Invalid server object at position " + position);
            return;
        }

        DedicatedIpHolder ipHolder = (DedicatedIpHolder)holder;
        DedicatedIPInformation dedicatedIPInformation = null;
        for (DedicatedIPInformation dip : PiaPrefHandler.getDedicatedIps(mContext)) {
            if ((server.getKey().equals(dip.getId()) || server.getName().toLowerCase().equals(dip.getId()))) {
                dedicatedIPInformation = dip;
                break;
            }
        }
        DedicatedIPInformation finalDedicatedIPInformation = dedicatedIPInformation;
        ipHolder.tvCountry.setText(server.getName());
        ipHolder.tvIp.setText(server.getDedicatedIp());

        int flagId = serverHandler.getFlagResource(server.getIso());
        ipHolder.ivFlag.setImageResource(flagId);
        ipHolder.ivFlag.setOnClickListener(view -> {
            DLog.d(TAG, "DIP " + finalDedicatedIPInformation);
        });

        ipHolder.ivClose.setOnClickListener(view -> {
            DialogFactory factory = new DialogFactory(mContext);
            final Dialog dialog = factory.buildDialog();
            factory.setHeader(mContext.getString(R.string.dip_remove_header));
            factory.setMessage(mContext.getString(R.string.dip_remove_body, server.getName(), server.getDedicatedIp()));

            factory.setPositiveButton(mContext.getString(R.string.ok), view1 -> {
                if (mContext instanceof DedicatedIPActivity) {
                    ((DedicatedIPActivity)mContext).removeDip(finalDedicatedIPInformation);
                }
                dialog.dismiss();
            });

            factory.setNegativeButton(mContext.getString(R.string.cancel), view12 -> dialog.dismiss());

            dialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return ipList.size();
    }

    public class DedicatedIpHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.list_server_name) TextView tvCountry;
        @BindView(R.id.list_server_ping) TextView tvIp;

        @BindView(R.id.list_server_flag) ImageView ivFlag;

        @BindView(R.id.list_server_close_button) AppCompatImageView ivClose;

        public DedicatedIpHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            tvCountry.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        }
    }

}

package com.odoo.widget.chatter;

import android.content.Context;
import android.content.SyncResult;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.odoo.core.rpc.helper.ODomain;
import com.odoo.core.support.CBind;
import com.odoo.core.support.OUser;
import com.odoo.core.utils.ODateUtils;
import com.odoo.core.utils.StringUtils;
import com.odoo.followup.R;
import com.odoo.followup.addons.customers.models.ResPartner;
import com.odoo.followup.addons.mail.models.MailMessage;
import com.odoo.followup.orm.OModel;
import com.odoo.followup.orm.data.ListRow;
import com.odoo.followup.orm.sync.SyncAdapter;

public class ChatterView extends LinearLayout implements View.OnClickListener {

    private OUser user;
    private MailMessage mailMessage;
    private OModel model;
    private int server_id = -1;

    public ChatterView(Context context) {
        super(context);
        init();
    }

    public ChatterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChatterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ChatterView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mailMessage = new MailMessage(getContext());
        user = OUser.current(getContext());
        setBackgroundColor(Color.parseColor("#ebebeb"));
    }

    public void loadChatter(OModel model, int server_id) {
        this.model = model;
        this.server_id = server_id;
        if (model != null && server_id > 0) {
            post(new Runnable() {
                @Override
                public void run() {
                    initView();
                }
            });
        }
    }

    private void initView() {
        removeAllViews();
        setOrientation(VERTICAL);
        addView(inflate(R.layout.chatter_action_buttons));
        addView(inflate(R.layout.chatter_messages_view));

        // binding actions
        findViewById(R.id.newMessage).setOnClickListener(this);
        findViewById(R.id.logInternalNote).setOnClickListener(this);

        new LoadMessages(getContext()).execute();
    }

    private View inflate(int view_id) {
        return LayoutInflater.from(getContext()).inflate(view_id, this, false);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.newMessage:
                break;
            case R.id.logInternalNote:
                break;
        }
    }

    private class LoadMessages extends AsyncTask<Void, Void, Void> {

        private Context mContext;

        public LoadMessages(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            findViewById(R.id.orSeparator).setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            SyncAdapter adapter = new SyncAdapter(mContext, true, mailMessage);
            ODomain domain = new ODomain();
            domain.add("model", "=", model.getModelName());
            domain.add("res_id", "=", server_id);
            domain.add("id", "not in", mailMessage.getServerIds());
            adapter.withDomain(domain);
            adapter.onlySync();
            adapter.onPerformSync(user.getAccount(), new Bundle(), mailMessage.authority(), null, new SyncResult());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            findViewById(R.id.progressBar).setVisibility(View.GONE);
            findViewById(R.id.orSeparator).setVisibility(View.VISIBLE);
            bindMessages();
        }
    }

    private void bindMessages() {
        LinearLayout container = (LinearLayout) findViewById(R.id.messageContainer);
        container.removeAllViews();
        ResPartner partner = new ResPartner(getContext());
        for (ListRow message : mailMessage.select(null, "date DESC", "model = ? and res_id = ?", model.getModelName(), server_id + "")) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.chatter_message_item, container, false);
            CBind.setText(view.findViewById(R.id.messageAuthor), partner.getName(message.getInt("author_id")));
            CBind.setText(view.findViewById(R.id.messageDate), ODateUtils.parseDate(ODateUtils.convertToDefault(message.getString("date"), ODateUtils.DEFAULT_FORMAT),
                    ODateUtils.DEFAULT_FORMAT, "dd MMM, hh:mm a"));
            CBind.setText(view.findViewById(R.id.messageBody), StringUtils.htmlToString(message.getString("body")));
            container.addView(view);
        }
    }

}

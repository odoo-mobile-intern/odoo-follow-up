package com.odoo.widget.chatter;

import android.content.Context;
import android.content.SyncResult;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.odoo.core.rpc.helper.ODomain;
import com.odoo.core.support.CBind;
import com.odoo.core.support.OUser;
import com.odoo.core.utils.ODateUtils;
import com.odoo.followup.R;
import com.odoo.followup.addons.customers.models.ResPartner;
import com.odoo.followup.addons.mail.models.MailMessage;
import com.odoo.followup.orm.OModel;
import com.odoo.followup.orm.data.ListRow;
import com.odoo.followup.orm.sync.SyncAdapter;
import com.odoo.followup.utils.BitmapUtils;

import java.util.List;

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
            List<Integer> serverIds = mailMessage.getServerIds();
            if (!serverIds.isEmpty())
                domain.add("id", "not in", serverIds);
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
            ListRow partnerObj = partner.browse(message.getInt("author_id"));
            CBind.setText(view.findViewById(R.id.messageAuthor), partnerObj.getString("name"));
            if (!partnerObj.getString("image_medium").equals("false")) {
                ImageView avatar = (ImageView) view.findViewById(R.id.authorImage);
                avatar.setImageBitmap(BitmapUtils.getBitmapImage(getContext(), partnerObj.getString("image_medium"))    );
            }
            CBind.setText(view.findViewById(R.id.messageDate), ODateUtils.parseDate(ODateUtils.convertToDefault(message.getString("date"), ODateUtils.DEFAULT_FORMAT),
                    ODateUtils.DEFAULT_FORMAT, "dd MMM, hh:mm a"));
            CBind.setSpannableText(view.findViewById(R.id.messageBody), Html.fromHtml(message.getString("body")));
            container.addView(view);
        }
    }

}

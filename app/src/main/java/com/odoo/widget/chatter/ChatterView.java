package com.odoo.widget.chatter;

import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

import com.odoo.core.rpc.Odoo;
import com.odoo.core.rpc.helper.ODomain;
import com.odoo.core.rpc.helper.ORecordValues;
import com.odoo.core.rpc.helper.utils.gson.OdooResult;
import com.odoo.core.support.CBind;
import com.odoo.core.support.OUser;
import com.odoo.core.support.OdooActivity;
import com.odoo.core.support.document.DocumentManager;
import com.odoo.core.utils.ODateUtils;
import com.odoo.followup.R;
import com.odoo.followup.addons.customers.models.ResPartner;
import com.odoo.followup.addons.mail.models.MailMessage;
import com.odoo.followup.orm.OModel;
import com.odoo.followup.orm.data.ListRow;
import com.odoo.followup.orm.models.IrAttachment;
import com.odoo.followup.orm.sync.SyncAdapter;
import com.odoo.followup.utils.BitmapUtils;

import java.util.List;

public class ChatterView extends LinearLayout implements View.OnClickListener {

    private OUser user;
    private MailMessage mailMessage;
    private IrAttachment irAttachment;
    private OModel model;
    private int server_id = -1;
    private boolean allowAttachments = false;
    private int total_attachments = 0;
    private OdooActivity odooActivity;
    private boolean isUploading = false;

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
        irAttachment = new IrAttachment(getContext());
        user = OUser.current(getContext());
        setBackgroundColor(Color.parseColor("#ebebeb"));
    }

    public void allowAttachments(OdooActivity activity, boolean allow) {
        odooActivity = activity;
        allowAttachments = allow;
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

        // checking chatter view
        if (allowAttachments) {
            findViewById(R.id.attachmentViewContainer).setVisibility(View.VISIBLE);
            findViewById(R.id.addNewAttachment).setOnClickListener(newAttachmentClick);
            findViewById(R.id.viewAllAttachments).setOnClickListener(viewAllAttachments);
            CBind.setText(findViewById(R.id.attachmentLabel), getContext()
                    .getString(R.string.label_loading_attachments));
            new LoadAttachments(getContext()).execute();
        }
    }

    private void bindAttachments() {
        total_attachments = irAttachment.getRecordAttachmentsCount(server_id, model.getModelName());
        String msg = getContext().getString(R.string.label_no_attachments);
        if (total_attachments > 0)
            msg = getContext().getString(R.string.label_total_attachments, total_attachments);
        CBind.setText(findViewById(R.id.attachmentLabel), msg);
    }

    private View.OnClickListener newAttachmentClick = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!isUploading) {
                DocumentManager documentManager = DocumentManager.getInstance(odooActivity);
                documentManager.requestFile(new DocumentManager.OnFileSelectListener() {
                    @Override
                    public void onFileSelected(ListRow fileData) {
                        isUploading = true;
                        CBind.setText(findViewById(R.id.addNewAttachment), getContext()
                                .getString(R.string.label_uploading));
                        new CreateNewAttachment().execute(fileData);
                    }

                    @Override
                    public void onFileSelectCancel() {

                    }
                });
            } else {
                Toast.makeText(odooActivity, R.string.label_another_upload_in_progress,
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    private class CreateNewAttachment extends AsyncTask<ListRow, Void, Boolean> {

        private Context mContext;

        private CreateNewAttachment() {
            mContext = getContext();
        }

        @Override
        protected Boolean doInBackground(ListRow... files) {
            try {
                ListRow file = files[0];
                Odoo odoo = Odoo.createWithUser(mContext, irAttachment.getUser());
                ORecordValues values = new ORecordValues();
                values.put("name", file.getString("name"));
                values.put("datas_fname", file.getString("datas_fname"));
                values.put("datas", file.getString("datas"));
                values.put("company_id", irAttachment.getUser().getCompanyId());
                values.put("type", "binary");
                values.put("res_model", model.getModelName());
                values.put("res_id", server_id);
                values.put("public", false);
                OdooResult result = odoo.createRecord(irAttachment.getModelName(), values);
                Thread.sleep(1000);
                return result.containsKey("result");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            isUploading = false;
            CBind.setText(findViewById(R.id.addNewAttachment), getContext()
                    .getString(R.string.label_add));
            if (success) {
                Toast.makeText(mContext, R.string.label_file_attached, Toast.LENGTH_SHORT).show();
                new LoadAttachments(getContext()).execute();
            } else {
                Toast.makeText(mContext, R.string.label_file_not_attached, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private View.OnClickListener viewAllAttachments = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (total_attachments > 0) {
                Intent attachmentViewer = new Intent(getContext(), AttachmentViewer.class);
                attachmentViewer.putExtra(AttachmentViewer.KEY_RECORD_NAME, model.getName(model.selectRowId(server_id)));
                attachmentViewer.putExtra(AttachmentViewer.KEY_MODEL_NAME, model.getModelName());
                attachmentViewer.putExtra(AttachmentViewer.KEY_RECORD_ID, server_id);
                getContext().startActivity(attachmentViewer);
            } else {
                Toast.makeText(getContext(), R.string.toast_no_attachments_to_view, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private View inflate(int view_id) {
        return LayoutInflater.from(getContext()).inflate(view_id, this, false);
    }

    @Override
    public void onClick(View view) {
        Intent composer = new Intent(getContext(), MessageComposer.class);
        composer.putExtra(MessageComposer.KEY_MODEL, model.getModelName());
        composer.putExtra(MessageComposer.KEY_RES_ID, server_id);
        composer.putExtra(MessageComposer.KEY_RECORD_NAME, model.getName(model.selectRowId(server_id)));
        switch (view.getId()) {
            case R.id.newMessage:
                composer.putExtra(MessageComposer.KEY_MESSAGE_TYPE, MessageComposer.MESSAGE_TYPE_MAIL);
                getContext().startActivity(composer);
                break;
            case R.id.logInternalNote:
                composer.putExtra(MessageComposer.KEY_MESSAGE_TYPE, MessageComposer.MESSAGE_TYPE_NOTE);
                getContext().startActivity(composer);
                break;
        }
    }

    private class LoadAttachments extends AsyncTask<Void, Void, Void> {
        private Context mContext;

        public LoadAttachments(Context context) {
            mContext = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            SyncAdapter adapter = new SyncAdapter(mContext, true, irAttachment);
            ODomain domain = new ODomain();
            domain.add("res_model", "=", model.getModelName());
            domain.add("res_id", "=", server_id);
            domain.add("res_field", "=", false);
            domain.add("datas", "!=", false);
            List<Integer> serverIds = irAttachment.getServerIds();
            if (!serverIds.isEmpty())
                domain.add("id", "not in", serverIds);
            adapter.withDomain(domain);
            adapter.onPerformSync(user.getAccount(), new Bundle(), irAttachment.authority(), null, new SyncResult());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            bindAttachments();
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
                avatar.setImageBitmap(BitmapUtils.getBitmapImage(getContext(), partnerObj.getString("image_medium")));
            }
            CBind.setText(view.findViewById(R.id.messageDate), ODateUtils.parseDate(ODateUtils.convertToDefault(message.getString("date"), ODateUtils.DEFAULT_FORMAT),
                    ODateUtils.DEFAULT_FORMAT, "dd MMM, hh:mm a"));
            CBind.setSpannableText(view.findViewById(R.id.messageBody), Html.fromHtml(message.getString("body")));
            container.addView(view);
        }
    }

}

package com.odoo.widget.chatter;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.odoo.core.rpc.Odoo;
import com.odoo.core.rpc.handler.OdooVersionException;
import com.odoo.core.rpc.helper.OArguments;
import com.odoo.core.rpc.helper.utils.gson.OdooResult;
import com.odoo.core.support.CBind;
import com.odoo.core.support.OUser;
import com.odoo.core.support.OdooActivity;
import com.odoo.followup.R;

import java.util.ArrayList;
import java.util.HashMap;

public class MessageComposer extends OdooActivity implements View.OnClickListener {

    public static final String KEY_MESSAGE_TYPE = "message_type";
    public static final String MESSAGE_TYPE_NOTE = "note";
    public static final String MESSAGE_TYPE_MAIL = "mail";
    public static final String KEY_MODEL = "model_name";
    public static final String KEY_RES_ID = "res_id";
    public static final String KEY_RECORD_NAME = "record_name";
    private Odoo odoo;
    private EditText edtBody;
    private boolean isNote = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.chatter_message_composer_view);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        CBind.setText(findViewById(R.id.recordName), getArgs().getString(KEY_RECORD_NAME));

        findViewById(R.id.btnCancel).setOnClickListener(this);
        findViewById(R.id.btnSend).setOnClickListener(this);
        CBind.setText(findViewById(R.id.messageSubject), "Re: " + getArgs().getString(KEY_RECORD_NAME));
        edtBody = (EditText) findViewById(R.id.messageBody);

        isNote = getArgs().getString(KEY_MESSAGE_TYPE).equals(MESSAGE_TYPE_NOTE);
        if (isNote) {
            findViewById(R.id.messageSubject).setVisibility(View.GONE);
            CBind.setText(findViewById(R.id.btnSend), getString(R.string.label_log));
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSend:
                if (validate()) {
                    messagePost(edtBody.getText().toString().trim());
                }
                break;
            case R.id.btnCancel:
                finish();
                break;
        }
    }

    private boolean validate() {
        if (edtBody.getText().toString().trim().isEmpty()) {
            edtBody.setError(getString(R.string.error_put_some_input_for_message));
            edtBody.requestFocus();
            return false;
        }
        return true;
    }

    private void messagePost(final String body) {
        Button btnSend = (Button) findViewById(R.id.btnSend);
        findViewById(R.id.btnCancel).setEnabled(false);
        if (!isNote)
            btnSend.setText(R.string.label_sending);
        else
            btnSend.setText(R.string.label_logging);
        btnSend.setEnabled(false);
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    odoo = Odoo.createWithUser(MessageComposer.this, OUser.current(MessageComposer.this));
                    OArguments arguments = new OArguments();
                    arguments.add(getArgs().getInt(KEY_RES_ID));

                    HashMap<String, Object> kwargs = new HashMap<>();
                    kwargs.put("attachment_ids", new ArrayList<>());
                    kwargs.put("content_subtype", "html");

                    HashMap<String, Object> context = new HashMap<>();
                    context.put("default_model", getArgs().getString(KEY_MODEL));
                    context.put("default_res_id", getArgs().getInt(KEY_RES_ID));
                    kwargs.put("context", odoo.updateContext(context));

                    kwargs.put("message_type", "comment");
                    kwargs.put("partner_ids", new ArrayList<>());
                    kwargs.put("subtype", !isNote ? "mail.mt_comment" : "mail.mt_note");
                    kwargs.put("body", body);

                    OdooResult result = odoo.callMethod(getArgs().getString(KEY_MODEL), "message_post", arguments, kwargs);
                    return !result.containsKey("error");
                } catch (OdooVersionException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                if (result) {
                    Toast.makeText(MessageComposer.this, isNote ? R.string.toast_note_logged : R.string.toast_message_sent, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(MessageComposer.this, isNote ? R.string.toast_not_able_to_log_note : R.string.toast_not_able_to_send_message, Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }
}

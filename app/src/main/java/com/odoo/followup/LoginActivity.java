package com.odoo.followup;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.odoo.core.rpc.Odoo;
import com.odoo.core.rpc.handler.OdooVersionException;
import com.odoo.core.rpc.listeners.IDatabaseListListener;
import com.odoo.core.rpc.listeners.IOdooConnectionListener;
import com.odoo.core.rpc.listeners.IOdooLoginCallback;
import com.odoo.core.rpc.listeners.OdooError;
import com.odoo.core.support.OUser;
import com.odoo.followup.auth.Authenticator;

import java.util.List;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener,
        IOdooConnectionListener, IOdooLoginCallback {
    public static final String TAG = LoginActivity.class.getCanonicalName();
    private EditText editHost, editUsername, editPassword;
    private ProgressDialog progressDialog;
    private View mView;
    private Odoo odoo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
    }

    private void init() {
        editHost = (EditText) findViewById(R.id.edtHost);
        editUsername = (EditText) findViewById(R.id.edtUsername);
        editPassword = (EditText) findViewById(R.id.edtPassword);
        findViewById(R.id.btnLogin).setOnClickListener(this);
        if (BuildConfig.DEBUG) {
            editHost.setText("http://192.168.199.101:8069");
            editUsername.setText("demo");
            editPassword.setText("demo");
        }
    }

    @Override
    public void onClick(View view) {
        mView = view;
        if (isValid()) {
            try {
                progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Please Wait");
                progressDialog.setMessage("Logging in..");
                progressDialog.setCancelable(false);
                progressDialog.show();
                odoo = Odoo.createInstance(this, hostURL());
                odoo.setOnConnect(this);
            } catch (OdooVersionException e) {
                Log.w(TAG, e.getMessage());
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.error_title_odoo_version);
                builder.setMessage(R.string.msg_unsupported_odoo_version);
                builder.setPositiveButton(android.R.string.ok, null);
                builder.show();
            }
        }
    }

    private boolean isValid() {
        editHost.setError(null);
        if (editHost.getText().toString().trim().isEmpty()) {
            editHost.setError(getString(R.string.enter_url));
            editHost.requestFocus();
            return false;
        }
        editUsername.setError(null);
        if (editUsername.getText().toString().trim().isEmpty()) {
            editUsername.setError(getString(R.string.username_required));
            editUsername.requestFocus();
            return false;
        }
        editPassword.setError(null);
        if (editPassword.getText().toString().trim().isEmpty()) {
            editPassword.setError(getString(R.string.password_required));
            editPassword.requestFocus();
            return false;
        }
        return true;
    }

    private String hostURL() {
        String url = editHost.getText().toString().trim();
        if (url.contains("http://") || url.contains("https://"))
            return url;
        else
            return "http://" + url;
    }

    @Override
    public void onConnect(Odoo odoo) {
        odoo.getDatabaseList(new IDatabaseListListener() {
            @Override
            public void onDatabasesLoad(List<String> list) {
                if (list.size() > 1) {
                    progressDialog.hide();
                    // Multiple database choice
                    //FIXME: Show user dialog for choice database
                    /*
                        Replace code with Alert dialog.
                            - List databases in dialog
                            - user will select one of them
                            and then process with selected database.
                     */
                    LoginTo(list.get(0));
                } else {
                    // Single database
                    LoginTo(list.get(0));
                }
            }
        });
    }

    public void LoginTo(String database) {
        odoo.authenticate(editUsername.getText().toString().trim(),
                editPassword.getText().toString().trim(), database, this);
    }

    @Override
    public void onError(OdooError odooError) {
        Snackbar.make(mView, getString(R.string.invalid_url), Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onLoginSuccess(Odoo odoo, OUser oUser) {
        AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
        Account account = new Account(oUser.getAndroidName(), Authenticator.AUTH_TYPE);
        if (manager.addAccountExplicitly(account, "N/A", oUser.getAsBundle())) {
            redirectToHome();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.title_fail_account_create);
            builder.setMessage(R.string.msg_unable_to_create_account);
            builder.setPositiveButton(android.R.string.ok, null);
            builder.show();
        }
    }

    private void redirectToHome() {
        progressDialog.dismiss();
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    @Override
    public void onLoginFail(OdooError odooError) {
        progressDialog.dismiss();
        Snackbar.make(mView, getString(R.string.invalid_username_or_password),
                Snackbar.LENGTH_LONG).show();
    }
}
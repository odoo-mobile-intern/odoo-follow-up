package com.odoo.followup;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.odoo.followup.auth.Authenticator;
import com.odoo.followup.sync.SyncAdapter;

import java.util.List;

import odoo.Odoo;
import odoo.handler.OdooVersionException;
import odoo.helper.OUser;
import odoo.listeners.IDatabaseListListener;
import odoo.listeners.IOdooConnectionListener;
import odoo.listeners.IOdooLoginCallback;
import odoo.listeners.OdooError;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener,
        IOdooConnectionListener, IOdooLoginCallback {

    private EditText editHost, editUsername, editPassword;
    private View mView;
    private Odoo odoo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        init();
    }

    private void init() {
        editHost = (EditText) findViewById(R.id.edtHost);
        editUsername = (EditText) findViewById(R.id.edtUsername);
        editPassword = (EditText) findViewById(R.id.edtPassword);
        findViewById(R.id.btnLogin).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        mView = view;
        isValid();
        try {
            odoo = Odoo.createInstance(this, hostURL());
            odoo.setOnConnect(this);
        } catch (OdooVersionException e) {
            e.printStackTrace();
        }
    }

    private void isValid() {
        editHost.setError(null);
        if (editHost.getText().toString().trim().isEmpty()) {
            editHost.setError(getString(R.string.enter_url));
            editHost.requestFocus();
            return;
        }

        editUsername.setError(null);
        if (editUsername.getText().toString().trim().isEmpty()) {
            editUsername.setError(getString(R.string.username_required));
            editUsername.requestFocus();
            return;
        }

        editPassword.setError(null);
        if (editPassword.getText().toString().trim().isEmpty()) {
            editPassword.setError(getString(R.string.password_required));
            editPassword.requestFocus();
            return;
        }
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
                    LoginTo(list.get(0));
                } else {
                    LoginTo(list.get(0));
                }
            }
        });
    }

    public void LoginTo(String database) {
        odoo.authenticate(editUsername.getText().toString().trim(), editPassword.getText().toString()
                .trim(), database, this);
    }

    @Override
    public void onError(OdooError odooError) {
        Snackbar.make(mView, getString(R.string.invalid_url), Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onLoginSuccess(Odoo odoo, OUser oUser) {

        AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
        Account account = new Account(oUser.getAndroidName(), Authenticator.AUTH_TYPE);
        if (manager.addAccountExplicitly(account, oUser.getPassword(), oUser.getAsBundle())) {
            getContentResolver().setSyncAutomatically(account, SyncAdapter.AUTHORITY, true);
            redirectToHome();
        }
    }

    private void redirectToHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    @Override
    public void onLoginFail(OdooError odooError) {
        Snackbar.make(mView, getString(R.string.invalid_username_or_password),
                Snackbar.LENGTH_LONG).show();
    }

}
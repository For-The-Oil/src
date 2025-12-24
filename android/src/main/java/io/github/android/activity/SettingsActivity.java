package io.github.android.activity;

import android.os.Bundle;

import io.github.android.manager.ClientManager;

public class SettingsActivity extends BaseActivity{

    private ClientManager clientManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.clientManager = ClientManager.getInstance();
        this.clientManager.setCurrentContext(this);
    }


    private void disconnect(){
        finish();
    }


}

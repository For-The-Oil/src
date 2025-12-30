package io.github.android.activity;

import android.os.Bundle;
import android.view.View;

import io.github.android.manager.ClientManager;
import io.github.fortheoil.R;

public class SettingsActivity extends BaseActivity{

    private ClientManager clientManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.clientManager = ClientManager.getInstance();
        this.clientManager.setCurrentContext(this);
        setContentView(R.layout.settings_activity_main);
    }


    public void goBack(View view){
        finish();
    }


    public void setLang(View view){

    }

    public void setMusic(){

    }

    public void setVfx(){
        
    }



}

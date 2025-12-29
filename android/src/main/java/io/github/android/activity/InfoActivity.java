package io.github.android.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import io.github.android.manager.ClientManager;
import io.github.core.config.ClientDefaultConfig;
import io.github.fortheoil.R;

public class InfoActivity extends BaseActivity {

    private ClientManager clientManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.clientManager = ClientManager.getInstance();
        this.clientManager.setCurrentContext(this);

        setContentView(R.layout.info_activity_main);

        // Views
        TextView tvAppName = findViewById(R.id.tv_app_name);
        TextView tvVersion = findViewById(R.id.tv_version);
        TextView tvDescription = findViewById(R.id.tv_description);
        TextView tvDeveloper = findViewById(R.id.tv_developer);
        Button btnBack = findViewById(R.id.btn_back);

        // Data
        tvAppName.setText(ClientDefaultConfig.APP_NAME);
        tvVersion.setText("Version " + ClientDefaultConfig.VERSION_NAME);
        tvDescription.setText(ClientDefaultConfig.DESCRIPTION);
        tvDeveloper.setText("Développé par " + ClientDefaultConfig.DEVELOPER_NAME);

        // Action
        btnBack.setOnClickListener(v -> finish());
    }
}

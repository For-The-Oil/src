package io.github.android.gui.fragment.launcher;

import static io.github.android.config.ClientDefaultConfig.SERVER_PREFS;
import static io.github.android.utils.OtherUtils.getIntOrDefault;

import androidx.fragment.app.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;

import io.github.core.config.ServerDefaultConfig;
import io.github.android.manager.ClientManager;
import io.github.android.utils.PrefsUtils;
import io.github.fortheoil.R;

public class ServerFragment extends Fragment {

    private EditText ipField;
    private EditText portField;
    private TextView serverMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.main_activity_server, container, false);
        serverMessage = root.findViewById(R.id.serverMessage);
        ipField = root.findViewById(R.id.serverIpField);
        portField = root.findViewById(R.id.serverPortField);
        initServerDefaults();
        return root;
    }

    public void showError(String message) {
        if (serverMessage != null) {
            serverMessage.setText(message);
            serverMessage.setVisibility(View.VISIBLE);
        }
    }

    public void hideError(){
        if (serverMessage != null) {
            serverMessage.setText("");
            serverMessage.setVisibility(View.GONE);
        }
    }


    private void initServerDefaults() {
        Context context = requireContext();
        ClientManager myClientManager = ClientManager.getInstance();

        // Charger les prefs sauvegardées
        HashMap<String, String> saved = PrefsUtils.loadPrefs(SERVER_PREFS, context);

        String ip = saved.getOrDefault("server_ip", ServerDefaultConfig.SERVER_HOST);
        int port = getIntOrDefault(saved, "server_port", ServerDefaultConfig.SERVER_PORT);

        // Mettre à jour le client
        myClientManager.setIP(ip);
        myClientManager.setPort(port);

        // Remplir les champs UI
        ipField.setText(ip);
        portField.setText(String.valueOf(port));
    }


}

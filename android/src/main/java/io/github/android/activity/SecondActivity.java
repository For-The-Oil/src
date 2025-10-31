package io.github.android.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import io.github.android.gui.adapter.MainAdapter;
import io.github.android.listeners.AuthClientListener;
import io.github.android.manager.ClientManager;
import io.github.android.manager.SessionManager;
import io.github.android.utils.UiUtils;
import io.github.fortheoil.R;

public class SecondActivity extends AppCompatActivity {

    private LinearLayout dotsLayout;
    private MainAdapter adapter;
    private ClientManager clientManager; // déclaration manquante

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_activity);

        this.clientManager = ClientManager.getInstance();
        //this.clientManager.getKryoManager().addListener(new AuthClientListener());
        this.clientManager.setCurrentContext(this);

        setupViewPager();

        //initClientConfig(this);
    }


















    public void disconnect(View view){
        SessionManager.getInstance().clearSession();
        clientManager.getKryoManager().getClient().close();
        finish();
    }

























    private void setupViewPager() {
        ViewPager2 viewPager = findViewById(R.id.secondViewPager);
        dotsLayout = findViewById(R.id.dotsLayout);

        this.adapter = new MainAdapter(this);
        viewPager.setAdapter(adapter);

        int pageCount = adapter.getItemCount();

        viewPager.setCurrentItem(1, false);

        // Affiche les dots dès le départ
        viewPager.post(() -> {
            UiUtils.addBottomDots(SecondActivity.this, dotsLayout, viewPager.getCurrentItem(), pageCount);
        });

        // Mets à jour les dots quand on change de page
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                UiUtils.addBottomDots(SecondActivity.this, dotsLayout, position, pageCount);
            }
        });
    }
}

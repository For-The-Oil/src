package io.github.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import io.github.android.gui.adapter.MainAdapter;
import io.github.android.listeners.ClientListener;
import io.github.android.manager.ClientManager;
import io.github.android.manager.SessionManager;
import io.github.android.utils.UiUtils;
import io.github.fortheoil.R;

public class HomeActivity extends AppCompatActivity {

    private LinearLayout dotsLayout;
    private MainAdapter adapter;
    private ClientManager clientManager; // déclaration manquante

    private String gameMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_activity);

        this.clientManager = ClientManager.getInstance();
        //this.clientManager.getKryoManager().addListener(new ClientListener());
        this.clientManager.setCurrentContext(this);

        //ClientListener.getInstance(this, null).setCurrentActivity(this);

        setupViewPager();
    }











    public void play(View view){

    }










    public void disconnect(View view) {
        SessionManager.getInstance().clearSession();
        clientManager.getKryoManager().getClient().close();

        Intent intent = new Intent(this, SplashActivity.class);
        intent.putExtra("forceLogin", true);
        startActivity(intent);
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
            UiUtils.addBottomDots(HomeActivity.this, dotsLayout, viewPager.getCurrentItem(), pageCount);
        });

        // Mets à jour les dots quand on change de page
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                UiUtils.addBottomDots(HomeActivity.this, dotsLayout, position, pageCount);
            }
        });
    }










    public void setGameMode(String mode){
        this.gameMode = mode;
    }

    public String getGameMode(){
        return this.gameMode;
    }



}

package io.github.android;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;


import io.github.android.gui.MyPagerAdapter;
import io.github.fortheoil.R;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.widget.ImageView;
import android.widget.LinearLayout;

public class AndroidLauncher extends AppCompatActivity  {

    private int[] layouts;
    private LinearLayout dotsLayout;
    private ImageView[] dots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        dotsLayout = findViewById(R.id.dotsLayout);

        layouts = new int[]{R.layout.main_activity_login, R.layout.main_activity_register};

        MyPagerAdapter adapter = new MyPagerAdapter(this, layouts);
        viewPager.setAdapter(adapter);

        // Ajouter les points
        addBottomDots(0);

        // Changement de page
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                addBottomDots(position);
            }
        });
    }

    private void addBottomDots(int currentPage) {
        dots = new ImageView[layouts.length];
        dotsLayout.removeAllViews();

        for (int i = 0; i < layouts.length; i++) {
            dots[i] = new ImageView(this);
            dots[i].setImageResource(i == currentPage ? android.R.drawable.presence_online : android.R.drawable.presence_invisible);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(30, 30);
            params.setMargins(10,0,10,0);
            dotsLayout.addView(dots[i], params);
        }
    }
}



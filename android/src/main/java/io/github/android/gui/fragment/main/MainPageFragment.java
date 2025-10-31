package io.github.android.gui.fragment.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import java.util.Arrays;
import java.util.List;

import io.github.android.activity.SecondActivity;
import io.github.android.gui.adapter.CarouselAdapter;
import io.github.android.gui.adapter.MainAdapter;
import io.github.android.utils.UiUtils;
import io.github.fortheoil.R;

public class MainPageFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Ici tu gonfles ton layout XML (par ex. second_activity_main.xml)
        return inflater.inflate(R.layout.second_activity_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Récupère les vues
        ViewPager2 carousel = view.findViewById(R.id.imageCarousel);
        Button btnPlay = view.findViewById(R.id.btnPlay);

        // Exemple de liste d’images (drawables)
        List<Integer> images = Arrays.asList(
            R.drawable.placeholder_0,
            R.drawable.placeholder_1,
            R.drawable.placeholder_2
        );

        // Adapter du carrousel
        CarouselAdapter adapter = new CarouselAdapter(images);
        carousel.setAdapter(adapter);
        carousel.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        carousel.setOffscreenPageLimit(3);

        carousel.getChildAt(0).setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);

            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.performClick(); // important pour l’accessibilité
            }

            return false; // laisse le ViewPager2 gérer le swipe
        });


        // Transformer pour effet zoom
        carousel.setPageTransformer((page, position) -> {
            float scale = 0.85f + (1 - Math.abs(position)) * 0.15f;
            page.setScaleY(scale);
            page.setScaleX(scale);
            page.setAlpha(0.5f + (1 - Math.abs(position)) * 0.5f);
        });

        // Action du bouton Play
        btnPlay.setOnClickListener(v -> {
            int current = carousel.getCurrentItem();
            // TODO : switch sur current pour lancer le bon mode
        });
    }
}

package io.github.android.gui.fragment.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import java.util.Arrays;
import java.util.List;

import io.github.android.activity.HomeActivity;
import io.github.android.gui.adapter.CarouselAdapter;
import io.github.android.manager.MatchMakingManager;
import io.github.fortheoil.R;
import io.github.shared.data.EnumsTypes.GameModeType;

public class MainPageFragment extends Fragment {

    private HomeActivity activity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.second_activity_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        activity = (HomeActivity) getActivity();
        if (activity == null) return;

        ViewPager2 carousel = view.findViewById(R.id.imageCarousel);
        Button btnPlay = view.findViewById(R.id.btnPlay);

        // Liste des images du carrousel
        List<Integer> images = Arrays.asList(
            R.drawable.placeholder_0,
            R.drawable.placeholder_1,
            R.drawable.placeholder_2
        );

        // Adapter du carrousel
        CarouselAdapter adapter = new CarouselAdapter(images);
        carousel.setAdapter(adapter);
        carousel.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        carousel.setOffscreenPageLimit(images.size());

        // Empêche le parent d’intercepter le touch
        if (carousel.getChildCount() > 0) {
            carousel.getChildAt(0).setOnTouchListener((v, event) -> {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                if (event.getAction() == MotionEvent.ACTION_UP) v.performClick();
                return false;
            });
        }

        // Transformer pour effet zoom
        carousel.setPageTransformer((page, position) -> {
            float scale = 0.85f + (1 - Math.abs(position)) * 0.15f;
            page.setScaleX(scale);
            page.setScaleY(scale);
            page.setAlpha(0.5f + (1 - Math.abs(position)) * 0.5f);
        });

        // Changement du gameMode lors du swipe
        carousel.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0: activity.setGameMode("MODE_0"); break;
                    case 1: activity.setGameMode("MODE_1"); break;
                    case 2: activity.setGameMode("MODE_2"); break;
                    default: activity.setGameMode("DEFAULT_MODE");
                }
            }
        });

        // Action du bouton Play
        btnPlay.setOnClickListener(v -> {
            String currentMode = activity.getGameMode();
            // TODO : lancer l’activité ou la logique correspondant au mode

            MatchMakingManager myMatchManager = MatchMakingManager.getInstance();

            switch (currentMode) {
                case "MODE_0":
                    myMatchManager.askMatchmaking(GameModeType.ALPHA_TEST);
                    break;
                case "MODE_1":
                    myMatchManager.askMatchmaking(GameModeType.CLASSIC);
                    break;
                case "MODE_2":
                    myMatchManager.askMatchmaking(GameModeType.CLASSIC);
                    break;
                default:
                    myMatchManager.askMatchmaking(GameModeType.CLASSIC);
            }
        });


    }



}

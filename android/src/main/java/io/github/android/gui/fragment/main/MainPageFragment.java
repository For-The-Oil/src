package io.github.android.gui.fragment.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.github.android.activity.HomeActivity;
import io.github.android.gui.adapter.CarouselAdapter;
import io.github.android.manager.MatchMakingManager;
import io.github.fortheoil.R;
import io.github.android.manager.SessionManager;
import io.github.shared.data.EnumsTypes.GameModeType;
import io.github.shared.data.gameobject.Deck;

public class MainPageFragment extends Fragment {

    private HomeActivity activity;
    private Button btnPlay, btnDeck;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.second_activity_main, container, false);
    }

    @Override
    public void onViewCreated(View view,Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        activity = (HomeActivity) getActivity();
        if (activity == null) return;

        btnDeck = view.findViewById(R.id.btnDeck);
        btnPlay = view.findViewById(R.id.btnPlay);
        ViewPager2 carousel = view.findViewById(R.id.imageCarousel);
        Button btnPlay = view.findViewById(R.id.btnPlay);

        setupCarousel(carousel);
        setupPlayButton();
        updateDeckButton(); // initial update du bouton Deck
    }

    private void setupCarousel(ViewPager2 carousel) {
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

        // Zoom & alpha effect
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
    }

    private void setupPlayButton() {
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
            }
        });
    }

    /**
     * Méthode publique à appeler quand le deck courant a changé côté client
     * Elle met à jour le texte du bouton Deck
     */
    public void updateDeckButton() {
        Deck currentDeck = SessionManager.getInstance().getCurrentDeck();
        String deckName = "Deck"; // fallback si aucun deck

        if (currentDeck != null) {
            Map<String, Deck> decks = SessionManager.getInstance().getDecks();
            for (Map.Entry<String, Deck> entry : decks.entrySet()) {
                if (entry.getValue() == currentDeck) {
                    deckName = entry.getKey();
                    break;
                }
            }
        }

        if (btnDeck != null) {
            btnDeck.setText(deckName);
        }
    }



}

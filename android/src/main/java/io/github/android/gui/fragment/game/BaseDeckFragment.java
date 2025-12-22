package io.github.android.gui.fragment.game;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayout;

import java.util.List;

import io.github.android.activity.GameActivity;
import io.github.android.gui.adapter.BuildingAdapter;
import io.github.android.utils.UiUtils;
import io.github.core.client_engine.manager.SessionManager;
import io.github.core.data.ClientGame;
import io.github.fortheoil.R;
import io.github.shared.data.enums_types.DeckCardCategory;
import io.github.shared.data.enums_types.EntityType;
import io.github.shared.data.gameobject.Deck;
import io.github.shared.data.network.Player;
import io.github.shared.shared_engine.Utility;

public abstract class BaseDeckFragment extends Fragment {

    protected RecyclerView recycler;
    protected BuildingAdapter adapter;
    protected abstract DeckCardCategory getCategory();
    protected static final String TAG = "BaseDeckFragment";
    private static final int COLUMN = 3;
    FlexboxLayout topButtonBar;
    EntityType selectedBuilding;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        topButtonBar = view.findViewById(R.id.topButtonBar);
        topButtonBar.setVisibility(View.GONE); // caché par défaut

        ImageButton btnBuild = view.findViewById(R.id.btnBuildBuilding);
        ImageButton btnRotate = view.findViewById(R.id.btnRotateBuilding);
        ImageButton btnCancel = view.findViewById(R.id.btnCancel);

        GameActivity cont = (GameActivity) view.getContext();
        LibGdxFragment frag = cont.getLibGdxFragment();

        btnCancel.setOnClickListener(v -> {
            topButtonBar.setVisibility(View.GONE);
            selectedBuilding = null;
            frag.getRenderer().unpinBuilding();
        });

        btnRotate.setOnClickListener(v -> {
            //if (selectedBuilding != null) rotateBuilding(selectedBuilding);
        });

        btnBuild.setOnClickListener(v -> {
//            if (selectedBuilding != null) {
//                buildBuilding(selectedBuilding);
//                topButtonBar.setVisibility(View.GONE);
//                selectedBuilding = null;
//            }
        });

        recycler = view.findViewById(R.id.recyclerSection1);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setLayoutManager(new GridLayoutManager(getContext(), COLUMN));

        //On affiche le menu top si on a déjà un building de sélectionné
        if(frag.getRenderer().isPinnedBuilding()){
            topButtonBar.setVisibility(View.VISIBLE);
        }

        loadCards();
    }

    private void loadCards() {
        Player current = Utility.findPlayerByUuid(
            ClientGame.getInstance().getPlayersList(),
            SessionManager.getInstance().getUuidClient()
        );

        if (current == null) {
            Log.d(TAG, "Aucun joueur trouvé !");
            return;
        }

        Deck deck = current.getCurrentDeck();
        if (deck == null) {
            Log.d(TAG, "Le joueur n'a pas de deck !");
            return;
        }

        List<EntityType> cards = deck.getCardsByCategory(deck, getCategory());
        Log.d(TAG, "Nombre de cartes " + getCategory() + " : " + cards.size());

        adapter = new BuildingAdapter(cards);
        recycler.setAdapter(adapter);
    }

}

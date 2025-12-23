package io.github.android.gui.fragment.game;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.badlogic.gdx.math.Vector2;
import com.google.android.flexbox.FlexboxLayout;
import java.util.List;
import io.github.android.activity.GameActivity;
import io.github.android.gui.adapter.BuildingAdapter;
import io.github.android.manager.ClientManager;
import io.github.android.utils.OtherUtils;
import io.github.core.client_engine.factory.KryoMessagePackager;
import io.github.core.client_engine.factory.RequestFactory;
import io.github.core.client_engine.manager.SessionManager;
import io.github.core.data.ClientGame;
import io.github.core.game_engine.system.GraphicsSyncSystem;
import io.github.fortheoil.R;
import io.github.shared.data.enums_types.DeckCardCategory;
import io.github.shared.data.enums_types.Direction;
import io.github.shared.data.enums_types.EntityType;
import io.github.shared.data.gameobject.Deck;
import io.github.shared.data.network.KryoMessage;
import io.github.shared.data.network.Player;
import io.github.shared.shared_engine.Utility;
import io.github.shared.shared_engine.manager.ShapeManager;

public abstract class BaseDeckFragment extends Fragment {

    protected RecyclerView recycler;
    protected BuildingAdapter adapter;
    protected abstract DeckCardCategory getCategory();
    protected static final String TAG = "BaseDeckFragment";
    private static final int COLUMN = 3;
    private FlexboxLayout topButtonBar;
    private EntityType selectedBuilding;
    private Direction direction = Direction.NORTH;
    LibGdxFragment frag;


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        topButtonBar = view.findViewById(R.id.topButtonBar);
        topButtonBar.setVisibility(View.GONE); // caché par défaut

        ImageButton btnBuild = view.findViewById(R.id.btnBuildBuilding);
        ImageButton btnRotate = view.findViewById(R.id.btnRotateBuilding);
        ImageButton btnCancel = view.findViewById(R.id.btnCancel);

        GameActivity cont = (GameActivity) view.getContext();
        frag = cont.getLibGdxFragment();

        btnCancel.setOnClickListener(this::cancel);
        btnRotate.setOnClickListener(this::rotate);
        btnBuild.setOnClickListener(this::build);

        recycler = view.findViewById(R.id.recyclerSection1);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setLayoutManager(new GridLayoutManager(getContext(), COLUMN));

        //On affiche le menu top si on a déjà un building de sélectionné
        if(frag.getRenderer().isPinnedBuilding()){
            topButtonBar.setVisibility(View.VISIBLE);
        }

        loadCards();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (frag != null && frag.getRenderer() != null) {
            frag.getRenderer().unpinBuilding();
        }
        selectedBuilding = null;
        direction = Direction.NORTH;
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
        Log.d(TAG, "Joueur actuel : "+ current);

        // Passer le joueur courant à l'adapter
        adapter = new BuildingAdapter(cards, current, this::onBuildingSelected);
        recycler.setAdapter(adapter);
    }




    private void build(View view) {

        Player current = Utility.findPlayerByUuid(
            ClientGame.getInstance().getPlayersList(),
            SessionManager.getInstance().getUuidClient()
        );

        GraphicsSyncSystem gfx = ClientGame.getInstance().getWorld().getSystem(GraphicsSyncSystem.class);
        int netFrom = gfx.getFrom(selectedBuilding);

        if (selectedBuilding.getFrom() != null && netFrom < 0) {
            cancel(view);
            Toast.makeText(requireActivity(), "Erreur, technologie indisponible", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!OtherUtils.canAfford(current.getResources(), selectedBuilding.getCost())) {
            cancel(view);
            Toast.makeText(requireActivity(), "Erreur, ressources insuffisantes", Toast.LENGTH_SHORT).show();
            return;
        }

        Vector2 v = frag.getRenderer().getPinnedShapePos(selectedBuilding.getShapeType().getShape(), direction);

        if (!ShapeManager.canOverlayShape(
            ClientGame.getInstance().getMap(), selectedBuilding.getShapeType().getShape(),
            Utility.worldToCell(v.x), Utility.worldToCell(v.y),
            0, 0,
            selectedBuilding.getShapeType().getShape().getWidth(), selectedBuilding.getShapeType().getShape().getHeight(),
            selectedBuilding.getShapeType().getCanBePlacedOn())
            ){

            cancel(view);
            Toast.makeText(requireActivity(), "Erreur, implaçable", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            KryoMessage message = KryoMessagePackager.packBuildRequest(
                RequestFactory.createBuildRequest(selectedBuilding, netFrom, v.x, v.y, direction),
                SessionManager.getInstance().getToken()
            );

            ClientManager.getInstance().getKryoManager().send(message);


            new Handler(Looper.getMainLooper()).post(() -> {
                cancel(view);
            });

        }).start();

    }

    private void rotate(View view){
        direction = direction.rotateClockwise();
        frag.getRenderer().RotatePinBuilding(direction);
    }

    private void cancel(View view){
        topButtonBar.setVisibility(View.GONE);
        selectedBuilding = null;
        frag.getRenderer().unpinBuilding();
    }

    private void onBuildingSelected(EntityType entity) {
        selectedBuilding = entity;
        direction = Direction.NORTH;
        topButtonBar.setVisibility(View.VISIBLE);
        frag.getRenderer().pinBuildingToScreenCenter(entity);
        Log.d(TAG, "Building sélectionné : " + entity.name());
    }


}

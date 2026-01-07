package io.github.android.gui.fragment.game;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.artemis.ComponentMapper;
import com.google.android.flexbox.FlexboxLayout;
import java.util.ArrayList;
import java.util.List;
import io.github.android.gui.adapter.QuickProductionAdapter;
import io.github.android.manager.ClientManager;
import io.github.core.client_engine.factory.KryoMessagePackager;
import io.github.core.client_engine.factory.RequestFactory;
import io.github.core.client_engine.manager.SessionManager;
import io.github.core.data.ClientGame;
import io.github.core.game_engine.system.GraphicsSyncSystem;
import io.github.fortheoil.R;
import io.github.shared.data.component.NetComponent;
import io.github.shared.data.enums_types.EntityType;
import io.github.shared.data.network.KryoMessage;
import io.github.shared.data.network.Player;
import io.github.shared.shared_engine.Utility;

public class UnitsFragment extends Fragment implements BottomFragment.SelectionAware {

    private static final String TAG = "UnitsFragment";

    private RecyclerView recyclerAvailable;
    private QuickProductionAdapter quickAdapter;

    private FlexboxLayout topButtonBar;
    private ImageButton btnDelete, btnMove, btnAttack;

    private final List<EntityType> producibleUnits = new ArrayList<>();
    private int targetedEntityNetId = -1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_units, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // 1. Initialisation des vues
        topButtonBar = view.findViewById(R.id.topButtonBar);
        btnDelete = view.findViewById(R.id.btnDeleteUnits);
        btnMove = view.findViewById(R.id.btnMoveUnits);
        btnAttack = view.findViewById(R.id.btnAttackUnits);
        ImageButton btnCancel = view.findViewById(R.id.btnCancel);

        // 2. Listeners
        btnCancel.setOnClickListener(v -> hideActionMenu());
        btnDelete.setOnClickListener(v -> deleteTargetedEntity());

        // 3. État initial (caché par défaut)
        topButtonBar.setVisibility(View.GONE);

        setupRecycler(view);
        refreshAvailableProduction();

        // 4. RÉTABLIR L'ÉTAT si on a déjà un ID sélectionné
        if (targetedEntityNetId != -1) {
            showActionMenu();
        }
    }

    private void setupRecycler(View view) {
        recyclerAvailable = view.findViewById(R.id.recyclerAvailableUnits);
        recyclerAvailable.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        Player me = Utility.findPlayerByUuid(ClientGame.getInstance().getPlayersList(), SessionManager.getInstance().getUuidClient());
        quickAdapter = new QuickProductionAdapter(producibleUnits, me, this::sendProductionRequest);
        recyclerAvailable.setAdapter(quickAdapter);
    }

    // --- Logique de Production ---

    private void refreshAvailableProduction() {
        GraphicsSyncSystem gfx = ClientGame.getInstance().getWorld().getSystem(GraphicsSyncSystem.class);
        if (gfx == null) return;

        List<Integer> owned = new ArrayList<>();
        owned.addAll(gfx.getEntityBuildingIndustry());
        owned.addAll(gfx.getEntityBuildingMilitary());

        List<EntityType> ownedTypes = new ArrayList<>();
        ComponentMapper<NetComponent> mNet = ClientGame.getInstance().getWorld().getMapper(NetComponent.class);

        for (int id : owned) {
            if (mNet.has(id)) ownedTypes.add(mNet.get(id).entityType);
        }

        List<EntityType> newList = new ArrayList<>();
        for (EntityType et : EntityType.values()) {
            if (et.getType() == EntityType.Type.Unit && ownedTypes.contains(et.getFrom())) {
                newList.add(et);
            }
        }

        if (!newList.equals(producibleUnits)) {
            producibleUnits.clear();
            producibleUnits.addAll(newList);
            if (quickAdapter != null) quickAdapter.notifyDataSetChanged();
        }
    }

    private void sendProductionRequest(EntityType unitType) {
        GraphicsSyncSystem gfx = ClientGame.getInstance().getWorld().getSystem(GraphicsSyncSystem.class);
        int netFrom = (gfx != null) ? gfx.getFrom(unitType) : -1;

        if (netFrom == -1) {
            Toast.makeText(getContext(), "Bâtiment de production introuvable !", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            KryoMessage message = KryoMessagePackager.packSummonRequest(
                RequestFactory.createSummonRequest(unitType, netFrom, 1),
                SessionManager.getInstance().getToken()
            );
            ClientManager.getInstance().getKryoManager().send(message);
            new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(getContext(), "Production : " + unitType.name(), Toast.LENGTH_SHORT).show());
        }).start();
    }

    // --- Actions sur l'entité ciblée (NetID) ---

    private void deleteTargetedEntity() {
        if (targetedEntityNetId == -1) return;

        btnDelete.setEnabled(false);
        new Thread(() -> {
            ArrayList<Integer> toKill = new ArrayList<>();
            toKill.add(targetedEntityNetId);
            KryoMessage msg = KryoMessagePackager.packDestroyRequest(
                RequestFactory.createdDestroyRequest(toKill),
                SessionManager.getInstance().getToken()
            );
            ClientManager.getInstance().getKryoManager().send(msg);

            new Handler(Looper.getMainLooper()).post(() -> {
                btnDelete.setEnabled(true);
                hideActionMenu();
                // Note: Le rafraîchissement de la liste des unités existantes
                // sera géré par l'autre adapter/fragment.
            });
        }).start();
    }

    @Override
    public void onEntitySelected(int netId) {
        this.targetedEntityNetId = netId;
        if (topButtonBar == null) return;
        showActionMenu();
    }

    private void showActionMenu() {
        if (topButtonBar != null) {
            topButtonBar.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);
            btnMove.setVisibility(View.VISIBLE);
            btnAttack.setVisibility(View.VISIBLE);
        }
    }

    private void hideActionMenu() {
        topButtonBar.setVisibility(View.GONE);
        targetedEntityNetId = -1;
    }

    public void updateDynamicUI() {
        refreshAvailableProduction();
    }
}

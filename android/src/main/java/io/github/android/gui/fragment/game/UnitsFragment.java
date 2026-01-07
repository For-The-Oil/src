package io.github.android.gui.fragment.game;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.artemis.ComponentMapper;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.List;

import io.github.android.activity.GameActivity;
import io.github.android.gui.GameRenderer;
import io.github.android.gui.adapter.ExistingEntityAdapter;
import io.github.android.gui.adapter.QuickProductionAdapter;
import io.github.android.manager.ClientManager;
import io.github.android.utils.CameraGestureController;
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
import io.github.shared.data.requests.game.AttackGroupRequest;
import io.github.shared.data.requests.game.MoveGroupRequest;
import io.github.shared.shared_engine.Utility;

public class UnitsFragment extends Fragment implements BottomFragment.SelectionAware {

    private RecyclerView recyclerAvailable;
    private QuickProductionAdapter quickAdapter;

    private FlexboxLayout topButtonBar;
    private ImageButton btnDelete, btnMove, btnAttack;

    private final List<EntityType> producibleUnits = new ArrayList<>();

    // Listes pour le système de sélection
    private ExistingEntityAdapter unitsListAdapter;
    private ExistingEntityAdapter selectedUnitsAdapter;
    private final List<Integer> unitsListIds = new ArrayList<>();
    private final List<Integer> selectedUnitsIds = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_units, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        topButtonBar = view.findViewById(R.id.topButtonBar);
        btnDelete = view.findViewById(R.id.btnDeleteUnits);
        btnMove = view.findViewById(R.id.btnMoveUnits);
        btnAttack = view.findViewById(R.id.btnAttackUnits);
        ImageButton btnCancel = view.findViewById(R.id.btnCancel);

        btnCancel.setOnClickListener(v -> {
            deselectAll();
        });

        btnDelete.setOnClickListener(v -> deleteSelectedEntities());

        btnMove.setOnClickListener(v -> {
            moveOrder();
        });

        btnAttack.setOnClickListener(v -> {
            attackOrder();
        });

        topButtonBar.setVisibility(View.GONE);

        setupRecyclers(view);
        refreshAvailableProduction();
        refreshExistingUnits();
    }

    private void moveOrder() {
        if (selectedUnitsIds.isEmpty()) return;

        GraphicsSyncSystem gfx = ClientGame.getInstance().getWorld().getSystem(GraphicsSyncSystem.class);
        if (gfx == null) return;

        // 1. On prépare les NetIDs sur le thread UI (pas de soucis ici)
        ArrayList<Integer> groupNetIds = new ArrayList<>();
        for (int id : selectedUnitsIds) {
            int netId = gfx.getNetIdFromEntity(id);
            if (netId != -1) groupNetIds.add(netId);
        }

        GameActivity activity = (GameActivity) getActivity();
        if (activity == null || activity.getLibGdxFragment() == null) return;

        // 2. On "saute" sur le Thread LibGDX pour calculer les coordonnées
        Gdx.app.postRunnable(() -> {
            GameRenderer renderer = activity.getLibGdxFragment().getRenderer();
            float cx = Gdx.graphics.getWidth() * 0.5f;
            float cy = Gdx.graphics.getHeight() * 0.5f;
            Ray ray = renderer.getCamera().getPickRay(cx, cy);

            Vector3 pinnedPos = new Vector3();
            boolean hit = Intersector.intersectRayPlane(ray, new Plane(new Vector3(0,1,0), 0f), pinnedPos);

            // Fallback si le ray ne touche pas le plan (caméra trop horizontale)
            if(!hit){
                float t = -ray.origin.y / ray.direction.y;
                pinnedPos.set(ray.origin).mulAdd(ray.direction, t);
            }

            // --- CORRECTION : Alignement sur la grille ---
            // On transforme le World (933.6) en Cell (ex: 9) puis on revient en World propre (900.0)
            float alignedX = Utility.cellToWorld(Utility.worldToCell(pinnedPos.x));
            float alignedZ = Utility.cellToWorld(Utility.worldToCell(pinnedPos.z));

            Log.d("GameOrder", "World X: " + pinnedPos.x + " | Aligned: " + alignedX);
            Log.d("GameOrder", "World Z: " + pinnedPos.z + " | Aligned: " + alignedZ);

            new Thread(() -> {
                // On envoie les coordonnées ALIGNÉES
                MoveGroupRequest moveReq = RequestFactory.createMoveGroupRequest(
                    groupNetIds,
                    alignedX,
                    alignedZ,
                    false
                );

                KryoMessage message = KryoMessagePackager.packMoveGroupRequest(moveReq, SessionManager.getInstance().getToken());
                ClientManager.getInstance().getKryoManager().send(message);

            }).start();
        });
    }

    private void attackOrder() {
        if (selectedUnitsIds.isEmpty()) return;

        GraphicsSyncSystem gfx = ClientGame.getInstance().getWorld().getSystem(GraphicsSyncSystem.class);
        if (gfx == null) return;

        // 1. Préparer les NetIDs des attaquants
        ArrayList<Integer> groupNetIds = new ArrayList<>();
        for (int id : selectedUnitsIds) {
            int netId = gfx.getNetIdFromEntity(id);
            if (netId != -1) groupNetIds.add(netId);
        }

        GameActivity activity = (GameActivity) getActivity();
        if (activity == null || activity.getLibGdxFragment() == null) return;

        // 2. Sauter sur le thread LibGDX pour "picker" l'entité au centre
        Gdx.app.postRunnable(() -> {
            GameRenderer renderer = activity.getLibGdxFragment().getRenderer();

            // Coordonnées du centre de l'écran
            int cx = Gdx.graphics.getWidth() / 2;
            int cy = Gdx.graphics.getHeight() / 2;

            // On utilise ta méthode pickAllScenes que tu as déjà dans GameRenderer
            List<net.mgsx.gltf.scene3d.scene.Scene> hitScenes = renderer.pickAllScenes(cx, cy);

            int targetNetId = -1;

            // On cherche la première scène qui appartient à une entité avec un NetID
            for (net.mgsx.gltf.scene3d.scene.Scene scene : hitScenes) {
                int netId = gfx.getEntityNetID(scene);
                if (netId != -1) {
                    // Optionnel : vérifier ici si c'est un ennemi (si tu as un TeamComponent)
                    targetNetId = netId;
                    break; // On a trouvé notre cible la plus proche au centre
                }
            }

            final int finalTargetId = targetNetId;

            // 3. Envoyer la requête (sur un thread réseau)
            new Thread(() -> {
                AttackGroupRequest attackReq = RequestFactory.createAttackGroupRequest(groupNetIds, finalTargetId);
                KryoMessage message = KryoMessagePackager.packAttackGroupRequest(attackReq, SessionManager.getInstance().getToken());
                ClientManager.getInstance().getKryoManager().send(message);

                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    if (finalTargetId != -1) {
                        Toast.makeText(getContext(), "Attaque de la cible " + finalTargetId, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Aucune cible au centre de l'écran", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        });
    }

    private void setupRecyclers(View view) {
        // 1. Production
        recyclerAvailable = view.findViewById(R.id.recyclerAvailableUnits);
        recyclerAvailable.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        Player me = Utility.findPlayerByUuid(ClientGame.getInstance().getPlayersList(), SessionManager.getInstance().getUuidClient());
        quickAdapter = new QuickProductionAdapter(producibleUnits, me, this::sendProductionRequest);
        recyclerAvailable.setAdapter(quickAdapter);

        // 2. Unités sélectionnées (Section 1)
        RecyclerView recyclerSelected = view.findViewById(R.id.recyclerSection1);
        recyclerSelected.setLayoutManager(new GridLayoutManager(getContext(), 4)); // <--- ICI
        selectedUnitsAdapter = new ExistingEntityAdapter(selectedUnitsIds, this::onUnitDeselected);
        recyclerSelected.setAdapter(selectedUnitsAdapter);

        // 3. Toutes les unités (Grille de 4)
        RecyclerView recyclerTotal = view.findViewById(R.id.recyclerSection4);
        recyclerTotal.setLayoutManager(new GridLayoutManager(getContext(), 4)); // <--- ICI
        unitsListAdapter = new ExistingEntityAdapter(unitsListIds, this::onUnitSelectedFromList);
        recyclerTotal.setAdapter(unitsListAdapter);
    }

    // --- Logique de Sélection ---

    private void onUnitSelectedFromList(int entityId) {
        if (!selectedUnitsIds.contains(entityId)) {
            selectedUnitsIds.add(entityId);
            unitsListIds.remove(Integer.valueOf(entityId));
            syncUI();
        }
    }

    private void onUnitDeselected(int entityId) {
        selectedUnitsIds.remove(Integer.valueOf(entityId));
        // On refresh pour qu'elle revienne dans la liste globale
        refreshExistingUnits();
    }

    private void deselectAll() {
        if (selectedUnitsIds.isEmpty()) return;
        // On remet tout dans la liste globale
        unitsListIds.addAll(selectedUnitsIds);
        // On vide la sélection
        selectedUnitsIds.clear();
        syncUI();
        refreshExistingUnits();
    }

    private void syncUI() {
        selectedUnitsAdapter.notifyDataSetChanged();
        unitsListAdapter.notifyDataSetChanged();
        topButtonBar.setVisibility(selectedUnitsIds.isEmpty() ? View.GONE : View.VISIBLE);
    }

    // --- Rafraîchissement des données ---

    private void refreshExistingUnits() {
        GraphicsSyncSystem gfx = ClientGame.getInstance().getWorld().getSystem(GraphicsSyncSystem.class);
        if (gfx == null) return;

        ArrayList<Integer> allUnits = gfx.getEntityUnit();

        // Nettoyage de la liste globale (on ne garde que ce qui n'est pas sélectionné)
        unitsListIds.clear();
        for (Integer id : allUnits) {
            if (!selectedUnitsIds.contains(id)) {
                unitsListIds.add(id);
            }
        }

        // Sécurité : retirer les unités sélectionnées qui seraient mortes
        selectedUnitsIds.removeIf(id -> !allUnits.contains(id));

        syncUI();
    }

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

    // --- Actions Réseau ---

    private void deleteSelectedEntities() {
        if (selectedUnitsIds.isEmpty()) return;

        GraphicsSyncSystem gfx = ClientGame.getInstance().getWorld().getSystem(GraphicsSyncSystem.class);
        if (gfx == null) return;

        btnDelete.setEnabled(false);
        ArrayList<Integer> netIds = new ArrayList<>();
        for (int id : selectedUnitsIds) {
            int netId = gfx.getNetIdFromEntity(id);
            if (netId != -1) netIds.add(netId);
        }

        new Thread(() -> {
            KryoMessage msg = KryoMessagePackager.packDestroyRequest(
                RequestFactory.createdDestroyRequest(netIds),
                SessionManager.getInstance().getToken()
            );
            ClientManager.getInstance().getKryoManager().send(msg);

            new Handler(Looper.getMainLooper()).post(() -> {
                btnDelete.setEnabled(true);
                selectedUnitsIds.clear();
                refreshExistingUnits();
            });
        }).start();
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

    @Override
    public void onEntitySelected(int netId) {
        GraphicsSyncSystem gfx = ClientGame.getInstance().getWorld().getSystem(GraphicsSyncSystem.class);
        if (gfx == null) return;
        int entityId = gfx.getEntityFromNetId(netId);
        if (entityId != -1) onUnitSelectedFromList(entityId);
    }

    /**
     * Met à jour l'intégralité du fragment (Production + Unités + Sélection).
     * Appelée par l'activité lors de la réception de snapshots ou de changements d'état.
     */
    public void updateUI() {
        GraphicsSyncSystem gfx = ClientGame.getInstance().getWorld().getSystem(GraphicsSyncSystem.class);
        if (gfx == null) return;

        // 1. Rafraîchir les unités produisibles (Bâtiments possédés)
        updateProducibleUnitsList(gfx);

        // 2. Rafraîchir les unités vivantes sur la map
        ArrayList<Integer> allUnitsOnMap = gfx.getEntityUnit();

        // 3. Sécurité : Nettoyer la sélection (retirer les unités mortes)
        selectedUnitsIds.removeIf(id -> !allUnitsOnMap.contains(id));

        // 4. Mettre à jour la liste globale (vivantes ET non sélectionnées)
        unitsListIds.clear();
        for (Integer id : allUnitsOnMap) {
            if (!selectedUnitsIds.contains(id)) {
                unitsListIds.add(id);
            }
        }

        // 5. Appliquer les changements visuels sur le thread UI
        triggerVisualRefresh();
    }

    /**
     * Calcule la liste des unités produisibles selon les bâtiments actuels.
     */
    private void updateProducibleUnitsList(GraphicsSyncSystem gfx) {
        List<Integer> owned = new ArrayList<>();
        owned.addAll(gfx.getEntityBuildingIndustry());
        owned.addAll(gfx.getEntityBuildingMilitary());

        ComponentMapper<NetComponent> mNet = ClientGame.getInstance().getWorld().getMapper(NetComponent.class);
        List<EntityType> ownedTypes = new ArrayList<>();
        for (int id : owned) {
            if (mNet.has(id)) ownedTypes.add(mNet.get(id).entityType);
        }

        List<EntityType> newList = new ArrayList<>();
        for (EntityType et : EntityType.values()) {
            if (et.getType() == EntityType.Type.Unit && ownedTypes.contains(et.getFrom())) {
                newList.add(et);
            }
        }

        // On ne met à jour la liste que si elle a changé pour éviter des lags de l'adapter
        if (!newList.equals(producibleUnits)) {
            producibleUnits.clear();
            producibleUnits.addAll(newList);
        }
    }

    /**
     * Notifie tous les adapters et gère la visibilité des menus sur le Main Thread.
     */
    private void triggerVisualRefresh() {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (quickAdapter != null) quickAdapter.notifyDataSetChanged();
            if (unitsListAdapter != null) unitsListAdapter.notifyDataSetChanged();
            if (selectedUnitsAdapter != null) selectedUnitsAdapter.notifyDataSetChanged();

            if (topButtonBar != null) {
                topButtonBar.setVisibility(selectedUnitsIds.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });
    }


}

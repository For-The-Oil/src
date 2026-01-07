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

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.badlogic.gdx.math.Vector2;
import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.List;
import io.github.android.activity.GameActivity;
import io.github.android.gui.adapter.BuildingAdapter;
import io.github.android.gui.adapter.ExistingBuildingAdapter;
import io.github.android.manager.ClientManager;
import io.github.android.utils.OtherUtils;
import io.github.core.client_engine.factory.KryoMessagePackager;
import io.github.core.client_engine.factory.RequestFactory;
import io.github.core.client_engine.manager.SessionManager;
import io.github.core.data.ClientGame;
import io.github.core.game_engine.system.GraphicsSyncSystem;
import io.github.fortheoil.R;
import io.github.shared.data.component.NetComponent;
import io.github.shared.data.enums_types.DeckCardCategory;
import io.github.shared.data.enums_types.Direction;
import io.github.shared.data.enums_types.EntityType;
import io.github.shared.data.gameobject.Deck;
import io.github.shared.data.network.KryoMessage;
import io.github.shared.data.network.Player;
import io.github.shared.shared_engine.Utility;
import io.github.shared.shared_engine.manager.EcsManager;
import io.github.shared.shared_engine.manager.ShapeManager;

/**
 * Classe de base abstraite pour les fragments gérant le deck de construction.
 * <p>
 * Cette classe fournit la logique commune pour :
 * <ul>
 * <li>Afficher les cartes de construction via un {@link RecyclerView}.</li>
 * <li>Gérer le placement de nouveaux bâtiments (Pinning, Rotation, Validation).</li>
 * <li>Gérer la sélection et la suppression de bâtiments existants sur la carte.</li>
 * <li>Communiquer avec le serveur via Kryonet pour les requêtes de construction/destruction.</li>
 * </ul>
 */
public abstract class BaseDeckFragment extends Fragment implements BottomFragment.SelectionAware {

    /** Vue de défilement pour les cartes du deck. */
    protected RecyclerView recycler;

    /** Adaptateur pour l'affichage des cartes de bâtiments disponibles. */
    protected BuildingAdapter adapter;

    /** Identifiant de log pour le débogage. */
    protected static final String TAG = "BaseDeckFragment";

    /** Nombre de colonnes dans la grille du deck. */
    private static final int COLUMN = 3;

    /** Barre d'outils supérieure contenant les actions (Build, Rotate, Delete, Cancel). */
    private FlexboxLayout topButtonBar;

    /** Type d'entité actuellement sélectionné pour une nouvelle construction. */
    private EntityType selectedBuilding;

    /** Direction actuelle pour l'orientation du bâtiment à placer. */
    private Direction direction = Direction.NORTH;

    /** Adaptateur pour l'affichage des bâtiments déjà existants (utilisé par les sous-classes). */
    protected ExistingBuildingAdapter existingAdapter;

    /** Référence vers le fragment LibGDX pour interagir avec le moteur de rendu. */
    LibGdxFragment frag;

    // Boutons d'action de l'interface
    protected ImageButton btnDelete;
    protected ImageButton btnBuild;
    protected ImageButton btnRotate;

    /** ID réseau de l'entité ciblée sur la carte (pour suppression ou modification). */
    protected int targetedEntityNetId = -1;

    /** ID temporaire stocké si une sélection survient avant que la vue ne soit prête. */
    protected int pendingSelectedNetId = -1;

    /**
     * Définit la catégorie de cartes (Bâtiments, Unités, etc.) que ce fragment doit afficher.
     * @return La catégorie {@link DeckCardCategory} correspondante.
     */
    protected abstract DeckCardCategory getCategory();

    /**
     * Initialise l'interface utilisateur, configure les listeners et charge le deck.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        topButtonBar = view.findViewById(R.id.topButtonBar);
        topButtonBar.setVisibility(View.GONE);

        btnBuild = view.findViewById(R.id.btnBuildBuilding);
        btnRotate = view.findViewById(R.id.btnRotateBuilding);
        ImageButton btnCancel = view.findViewById(R.id.btnCancel);
        btnDelete = view.findViewById(R.id.btnDelete);

        // Récupération du contexte de jeu pour accéder à LibGDX
        GameActivity cont = (GameActivity) view.getContext();
        frag = cont.getLibGdxFragment();

        btnCancel.setOnClickListener(this::cancel);
        btnRotate.setOnClickListener(this::rotate);
        btnBuild.setOnClickListener(this::build);

        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> {
                deleteTargetedEntity();
            });
            btnDelete.setVisibility(View.GONE);
            //btnDelete.setVisibility(View.GONE);
        }

        recycler = view.findViewById(R.id.recyclerSection1);
        recycler.setLayoutManager(new GridLayoutManager(getContext(), COLUMN));

        // Traitement d'une sélection en attente si nécessaire
        if (pendingSelectedNetId != -1) {
            targetedEntityNetId = pendingSelectedNetId;
            applyExistingSelection(pendingSelectedNetId);
            pendingSelectedNetId = -1;
        }

        loadCards();
    }

    /**
     * Gère la sélection d'un bâtiment déjà présent sur le terrain.
     * @param netId L'identifiant réseau unique de l'entité sélectionnée.
     */
    protected void onExistingBuildingSelected(int netId) {
        targetedEntityNetId = netId;

        // Si la vue n'est pas encore créée, on met l'ID en attente
        if (topButtonBar == null) {
            pendingSelectedNetId = netId;
            return;
        }

        applyExistingSelection(netId);
    }

    /**
     * Met à jour l'interface utilisateur pour afficher les options de suppression.
     * @param netId L'entité cible.
     */
    private void applyExistingSelection(int netId) {
        // 1. On cache d'abord les boutons de construction
        resetButtonVisibility();

        // 2. On affiche la barre et le bouton de suppression uniquement
        topButtonBar.setVisibility(View.VISIBLE);
        if (btnDelete != null) btnDelete.setVisibility(View.VISIBLE);

        // 3. On annule toute construction "fantôme" en cours
        selectedBuilding = null;
        if (frag != null && frag.getRenderer() != null) {
            frag.getRenderer().unpinBuilding();
        }
    }

    /**
     * Envoie une requête de destruction au serveur pour l'entité ciblée.
     * L'opération s'exécute dans un thread séparé pour ne pas bloquer l'UI.
     */
    private void deleteTargetedEntity() {
        if (targetedEntityNetId == -1) {
            Log.w(TAG, "Suppression annulée : targetedEntityNetId est à -1");
            return;
        }

        Log.d(TAG, "1. Début de la suppression pour le NetID: " + targetedEntityNetId);
        btnDelete.setEnabled(false);

        new Thread(() -> {
            try {
                ArrayList<Integer> entitiesToDestroy = new ArrayList<>();
                entitiesToDestroy.add(targetedEntityNetId);

                Log.d(TAG, "2. Préparation du paquet Kryo pour " + entitiesToDestroy.size() + " entité(s)");

                KryoMessage message = KryoMessagePackager.packDestroyRequest(
                    RequestFactory.createdDestroyRequest(entitiesToDestroy),
                    SessionManager.getInstance().getToken()
                );

                Log.d(TAG, "3. Envoi de la requête au serveur...");
                ClientManager.getInstance().getKryoManager().send(message);
                Log.d(TAG, "4. Requête envoyée avec succès au socket.");

                // Retour sur le thread principal pour nettoyer l'UI
                new Handler(Looper.getMainLooper()).post(() -> {
                    Log.d(TAG, "5. Retour sur le thread UI - Nettoyage de l'interface");
                    btnDelete.setEnabled(true);

                    // On garde une trace de l'ID supprimé avant de reset
                    int deletedId = targetedEntityNetId;

                    cancel(null); // Ferme le menu et reset targetedEntityNetId à -1
                    refreshExistingList();

                    Log.i(TAG, "6. Suppression terminée localement pour l'entité: " + deletedId);
                });

            } catch (Exception e) {
                Log.e(TAG, "ERREUR lors de la suppression: " + e.getMessage(), e);
                new Handler(Looper.getMainLooper()).post(() -> btnDelete.setEnabled(true));
            }
        }).start();
    }

    /**
     * Méthode à surcharger pour rafraîchir les listes locales après une suppression.
     */
    protected void refreshExistingList() {}

    /**
     * Nettoie les ressources et détache les prévisualisations lors de la fermeture du fragment.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (frag != null && frag.getRenderer() != null) {
            frag.getRenderer().unpinBuilding();
        }
        selectedBuilding = null;
        direction = Direction.NORTH;
    }

    /**
     * Charge les cartes du joueur actuel correspondant à la catégorie du fragment.
     */
    private void loadCards() {
        Player current = Utility.findPlayerByUuid(
            ClientGame.getInstance().getPlayersList(),
            SessionManager.getInstance().getUuidClient()
        );

        if (current == null || current.getCurrentDeck() == null) {
            Log.d(TAG, "Joueur ou Deck introuvable !");
            return;
        }

        List<EntityType> cards = current.getCurrentDeck().getCardsByCategory(current.getCurrentDeck(), getCategory());

        adapter = new BuildingAdapter(cards, current, this::onBuildingSelected);
        recycler.setAdapter(adapter);
    }

    /**
     * Logique de validation et d'envoi de la requête de construction.
     * Vérifie les prérequis technologiques, les ressources et les collisions sur la carte.
     * * @param view La vue ayant déclenché l'action.
     */
    private void build(View view) {
        Player current = Utility.findPlayerByUuid(
            ClientGame.getInstance().getPlayersList(),
            SessionManager.getInstance().getUuidClient()
        );

        GraphicsSyncSystem gfx = ClientGame.getInstance().getWorld().getSystem(GraphicsSyncSystem.class);
        int netFrom = gfx.getFrom(selectedBuilding);

        // 1. Vérification Technologie
        if (selectedBuilding.getFrom() != null && netFrom < 0) {
            cancel(view);
            Toast.makeText(requireActivity(), "Erreur, technologie indisponible", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Vérification Ressources
        if (!OtherUtils.canAfford(current.getResources(), selectedBuilding.getCost())) {
            cancel(view);
            Toast.makeText(requireActivity(), "Erreur, ressources insuffisantes", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Vérification Emplacement (Collisions/Terrain)
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

        // 4. Envoi au serveur
        new Thread(() -> {
            KryoMessage message = KryoMessagePackager.packBuildRequest(
                RequestFactory.createBuildRequest(selectedBuilding, netFrom, v.x, v.y, direction),
                SessionManager.getInstance().getToken()
            );
            ClientManager.getInstance().getKryoManager().send(message);
            new Handler(Looper.getMainLooper()).post(() -> cancel(view));
        }).start();
    }

    /**
     * Fait pivoter le bâtiment sélectionné dans le sens horaire.
     */
    private void rotate(View view){
        direction = direction.rotateClockwise();
        frag.getRenderer().RotatePinBuilding(direction);
    }

    /**
     * Annule l'action en cours, réinitialise l'UI et retire les prévisualisations.
     */
    protected void cancel(View view) {
        topButtonBar.setVisibility(View.GONE);
        resetButtonVisibility();

        targetedEntityNetId = -1;
        selectedBuilding = null;

        if (frag != null && frag.getRenderer() != null) {
            frag.getRenderer().unpinBuilding();
        }

        if (existingAdapter != null) {
            existingAdapter.setSelectedNetId(-1);
        }
    }

    /**
     * Callback lorsqu'une carte est cliquée dans le deck.
     * @param entity Le type de bâtiment à construire.
     */
    private void onBuildingSelected(EntityType entity) {
        selectedBuilding = entity;
        direction = Direction.NORTH;

        // 1. On cache TOUT d'abord pour éviter les résidus du mode sélection
        resetButtonVisibility();

        // 2. On affiche la barre et les boutons de construction uniquement
        topButtonBar.setVisibility(View.VISIBLE);
        if (btnBuild != null) btnBuild.setVisibility(View.VISIBLE);
        if (btnRotate != null) btnRotate.setVisibility(View.VISIBLE);

        // 3. On s'assure que le mode sélection est annulé (nettoyage de la liste)
        targetedEntityNetId = -1;
        if (existingAdapter != null) {
            existingAdapter.setSelectedNetId(-1);
        }

        frag.getRenderer().pinBuildingToScreenCenter(entity);
        Log.d(TAG, "Mode Construction activé pour : " + entity.name());
    }

    private void resetButtonVisibility() {
        if (btnDelete != null) btnDelete.setVisibility(View.GONE);
        if (btnBuild != null) btnBuild.setVisibility(View.GONE);
        if (btnRotate != null) btnRotate.setVisibility(View.GONE);
    }

    @Override
    public void onEntitySelected(int netId) {
        this.targetedEntityNetId = netId;

        // 1. Affiche les boutons Build/Delete en haut
        onExistingBuildingSelected(netId);

        // 2. Met à jour la surbrillance de la liste en bas
        if (existingAdapter != null) {
            // On passe directement le netId à l'adapter modifié
            existingAdapter.setSelectedNetId(netId);

            // Optionnel : Scroll automatique vers l'élément sélectionné
            int pos = existingAdapter.getPositionOfNetId(netId);
            if (pos != -1 && recycler != null) { // Assure-toi d'avoir la réf du recycler
                recycler.scrollToPosition(pos);
            }
        } else {
            // Stocke l'ID si le fragment n'est pas encore prêt (onViewCreated s'en chargera)
            this.pendingSelectedNetId = netId;
        }
    }

    public void updateDynamicUI() {
        if (existingAdapter != null) {
            existingAdapter.notifyDataSetChanged();
        }
    }

}

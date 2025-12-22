package io.github.android.gui.adapter;

import static io.github.core.game_engine.factory.InstanceFactoryScene.getShapeScenes;
import static io.github.core.game_engine.factory.InstanceFactoryScene.pinShapeScenes;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayout;

import net.mgsx.gltf.scene3d.scene.Scene;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.android.activity.GameActivity;
import io.github.android.gui.GameRenderer;
import io.github.android.gui.fragment.game.LibGdxFragment;
import io.github.android.utils.UiUtils;
import io.github.core.data.ClientGame;
import io.github.core.client_engine.manager.SessionManager;
import io.github.core.game_engine.factory.InstanceFactoryScene;
import io.github.fortheoil.R;
import io.github.shared.data.enums_types.EntityType;
import io.github.shared.data.gameobject.Shape;
import io.github.shared.data.network.Player;
import io.github.shared.data.enums_types.ResourcesType;

public class BuildingAdapter extends RecyclerView.Adapter<BuildingAdapter.BuildingViewHolder> {

    private final List<EntityType> cards;
    private final Player currentPlayer;
    private static final String TAG = "BuildingAdapter";

    public BuildingAdapter(List<EntityType> cards) {
        this.cards = cards;
        // Récupère le joueur courant
        currentPlayer = ClientGame.getInstance().getPlayersList().stream()
            .filter(p -> p.getUuid().equals(SessionManager.getInstance().getUuidClient()))
            .findFirst().orElse(null);
    }

    @NonNull
    @Override
    public BuildingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_building_card, parent, false);

        ImageButton imgCard = view.findViewById(R.id.imgCard);
        TextView txtCost = view.findViewById(R.id.txtCost);

        return new BuildingViewHolder(view, imgCard, txtCost);
    }


    @Override
    public void onBindViewHolder(@NonNull BuildingViewHolder holder, int position) {
        EntityType card = cards.get(position);
        int drawableId = UiUtils.mapEntityTypeToDrawable(card);
        holder.imageButton.setImageResource(drawableId);
        holder.imageButton.setTag(card.name());

        // Affichage du coût
        if (card.getCost() != null && !card.getCost().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            boolean affordable = true;

            for (Map.Entry<ResourcesType, Integer> entry : card.getCost().entrySet()) {
                sb.append(entry.getKey().name()).append(": ").append(entry.getValue()).append(" ");
                if (currentPlayer != null) {
                    int playerRes = currentPlayer.getResources().getOrDefault(entry.getKey(), 0);
                    if (playerRes < entry.getValue()) affordable = false;
                }
            }
            holder.costText.setText(sb.toString());

            // Griser si pas assez de ressources
            if (!affordable) {
                holder.imageButton.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                //holder.imageButton.setEnabled(false);
            } else {
                holder.imageButton.clearColorFilter();
                holder.imageButton.setEnabled(true);
            }
        } else {
            holder.costText.setText("");
        }

        // Click listener
        holder.imageButton.setOnClickListener(v -> {
            Log.d(TAG, "Carte cliquée : " + card.name());
            String cardName = holder.imageButton.toString();
            EntityType entity;
            try {
                entity = EntityType.valueOf(card.name());
            } catch (IllegalArgumentException e) {
                // le nom ne correspond à aucun EntityType
                entity = null;
            }

            if (entity!=null){
                Log.d(TAG, "Carte chargé : " + card.name());
                GameActivity context = (GameActivity) v.getContext();
                LibGdxFragment fragment = context.getLibGdxFragment();
                fragment.getRenderer().pinBuildingToScreenCenter(entity);
                showBuildingButton(v);
            }

        });
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }


    public void showBuildingButton(View v){
        Context context = v.getContext();
        if (context instanceof GameActivity) {
            GameActivity activity = (GameActivity) context;
            FlexboxLayout topButtonBar = activity.findViewById(R.id.topButtonBar);
            topButtonBar.setVisibility(View.VISIBLE);
        }
    }


    static class BuildingViewHolder extends RecyclerView.ViewHolder {
        ImageButton imageButton;
        TextView costText;

        BuildingViewHolder(@NonNull View itemView, ImageButton button, TextView costText) {
            super(itemView);
            this.imageButton = button;
            this.costText = costText;
        }
    }

}

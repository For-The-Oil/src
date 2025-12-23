package io.github.android.gui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

import io.github.android.utils.OtherUtils;
import io.github.android.utils.UiUtils;
import io.github.core.data.ClientGame;
import io.github.core.game_engine.system.GraphicsSyncSystem;
import io.github.fortheoil.R;
import io.github.shared.data.enums_types.EntityType;
import io.github.shared.data.enums_types.ResourcesType;
import io.github.shared.data.network.Player;

public class BuildingAdapter extends RecyclerView.Adapter<BuildingAdapter.BuildingViewHolder> {

    public interface OnBuildingClick {
        void onClick(EntityType entity);
    }

    private final List<EntityType> cards;
    private final OnBuildingClick callback;
    private final Player currentPlayer;


    public BuildingAdapter(List<EntityType> cards, Player currentPlayer, OnBuildingClick callback) {
        this.cards = cards;
        this.callback = callback;
        this.currentPlayer = currentPlayer;
    }

    @NonNull
    @Override
    public BuildingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_building_card, parent, false);
        return new BuildingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BuildingViewHolder holder, int position) {
        EntityType card = cards.get(position);

        holder.img.setImageResource(UiUtils.mapEntityTypeToDrawable(card));




        //TODO : Ajouter un cadenas si pas la tech
        GraphicsSyncSystem gfx = ClientGame.getInstance().getWorld().getSystem(GraphicsSyncSystem.class);
        int netFrom = gfx.getFrom(card);



        // Affichage du coût
        if (card.getCost() != null && !card.getCost().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<ResourcesType, Integer> entry : card.getCost().entrySet()) {
                sb.append(entry.getKey().name()).append(": ").append(entry.getValue()).append(" ");
            }
            holder.cost.setText(sb.toString().trim());

            // Griser si pas assez de ressources et désactiver le clic
            if (!OtherUtils.canAfford(currentPlayer.getResources(),card.getCost())) {
                holder.img.setColorFilter(Color.GRAY, android.graphics.PorterDuff.Mode.MULTIPLY);
                holder.img.setEnabled(false);
            } else {
                holder.img.clearColorFilter();
                holder.img.setEnabled(true);
            }
        } else {
            holder.cost.setText("");
            holder.img.clearColorFilter();
            holder.img.setEnabled(true);
        }

        holder.img.setOnClickListener(v -> {
            if (callback != null && holder.img.isEnabled()) {
                callback.onClick(card);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    static class BuildingViewHolder extends RecyclerView.ViewHolder {
        ImageButton img;
        TextView cost;

        BuildingViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgCard);
            cost = itemView.findViewById(R.id.txtCost);
        }
    }
}


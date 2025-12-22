package io.github.android.gui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import io.github.android.utils.UiUtils;
import io.github.fortheoil.R;
import io.github.shared.data.enums_types.EntityType;

public class BuildingAdapter extends RecyclerView.Adapter<BuildingAdapter.BuildingViewHolder> {

    public interface OnBuildingClick {
        void onClick(EntityType entity);
    }

    private final List<EntityType> cards;
    private final OnBuildingClick callback;

    public BuildingAdapter(List<EntityType> cards, OnBuildingClick callback) {
        this.cards = cards;
        this.callback = callback;
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

        holder.img.setOnClickListener(v -> {
            if (callback != null) {
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

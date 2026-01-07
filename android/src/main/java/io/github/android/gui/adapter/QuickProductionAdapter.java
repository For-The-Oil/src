package io.github.android.gui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.github.android.utils.UiUtils;
import io.github.fortheoil.R;
import io.github.shared.data.enums_types.EntityType;
import io.github.shared.data.network.Player;

public class QuickProductionAdapter extends RecyclerView.Adapter<QuickProductionAdapter.ViewHolder> {

    private final List<EntityType> units;
    private final Player player;
    private final OnUnitSelectedListener listener;

    public interface OnUnitSelectedListener {
        void onSelected(EntityType type);
    }

    public QuickProductionAdapter(List<EntityType> units, Player player, OnUnitSelectedListener listener) {
        this.units = units;
        this.player = player;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quick_unit, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EntityType unit = units.get(position);

        // Icône via ton UiUtils
        holder.icon.setImageResource(UiUtils.mapEntityTypeToDrawable(unit));
        holder.name.setText(unit.name());

        // Gestion des coûts
        holder.costContainer.removeAllViews();
        unit.getCost().forEach((resource, amount) -> {
            TextView tv = new TextView(holder.itemView.getContext());
            tv.setText(String.valueOf(amount));
            tv.setTextSize(9);
            tv.setPadding(4, 0, 4, 0);

            // Couleur rouge si pas assez de thunes
            int playerAmount = player.getResources().getOrDefault(resource, 0);
            tv.setTextColor(playerAmount >= amount ? Color.GREEN : Color.RED);

            holder.costContainer.addView(tv);
        });

        holder.itemView.setOnClickListener(v -> listener.onSelected(unit));
    }

    @Override
    public int getItemCount() { return units.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;
        LinearLayout costContainer;

        ViewHolder(View v) {
            super(v);
            icon = v.findViewById(R.id.unitIcon);
            name = v.findViewById(R.id.unitName);
            costContainer = v.findViewById(R.id.costContainer);
        }
    }
}

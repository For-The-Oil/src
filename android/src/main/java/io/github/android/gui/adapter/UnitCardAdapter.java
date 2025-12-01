package io.github.android.gui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

import io.github.android.utils.UiUtils;
import io.github.fortheoil.R;
import io.github.shared.data.enumsTypes.EntityType;
import io.github.shared.data.enumsTypes.ResourcesType;

public class UnitCardAdapter extends RecyclerView.Adapter<UnitCardAdapter.UnitViewHolder> {

    private final List<EntityType> unitList;

    public UnitCardAdapter(List<EntityType> unitList) {
        this.unitList = unitList;
    }

    @NonNull
    @Override
    public UnitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.unit_card, parent, false);
        return new UnitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UnitViewHolder holder, int position) {
        EntityType unit = unitList.get(position);

        // Nom
        holder.unitName.setText(unit.name());

        // Image
        holder.unitImage.setImageResource(UiUtils.mapEntityTypeToDrawable(unit));

        // Type
        holder.unitType.setText(unit.getType() != null ? unit.getType().name() : "Unknown");

        // Spawn From
        if (unit.getFrom() != null) {
            holder.unitSpawnFrom.setText(unit.getFrom().name());
            holder.unitSpawnFrom.setVisibility(View.VISIBLE);
        } else {
            holder.unitSpawnFrom.setVisibility(View.GONE);
        }

        // Health
        holder.unitHealth.setText(String.valueOf((int) unit.getMaxHealth()));

        // Armor
        holder.unitArmor.setText(String.valueOf(unit.getArmor()));

        // Speed
        holder.unitSpeed.setText(String.valueOf(unit.getBase_speed()));

        // Cost
        StringBuilder costStr = new StringBuilder();
        if (unit.getCost() != null && !unit.getCost().isEmpty()) {
            for (Map.Entry<ResourcesType, Integer> entry : unit.getCost().entrySet()) {
                costStr.append(entry.getKey().name())
                    .append(" ")
                    .append(entry.getValue())
                    .append(", ");
            }
            costStr.setLength(costStr.length() - 2); // enlever la dernière virgule
        } else {
            costStr.append("None");
        }
        holder.unitCost.setText(costStr.toString());
    }

    @Override
    public int getItemCount() {
        return unitList.size();
    }

    static class UnitViewHolder extends RecyclerView.ViewHolder {
        ImageView unitImage;
        TextView unitName;
        TextView unitType;
        TextView unitSpawnFrom;
        TextView unitHealth;
        TextView unitArmor;
        TextView unitSpeed;
        TextView unitCost;

        public UnitViewHolder(@NonNull View itemView) {
            super(itemView);
            unitImage = itemView.findViewById(R.id.unitImage);
            unitName = itemView.findViewById(R.id.unitName);
            unitType = itemView.findViewById(R.id.unitType);
            unitSpawnFrom = itemView.findViewById(R.id.unitSpawnFrom);
            unitHealth = itemView.findViewById(R.id.unitHealth);
            unitArmor = itemView.findViewById(R.id.unitArmor);
            unitSpeed = itemView.findViewById(R.id.unitSpeed);
            unitCost = itemView.findViewById(R.id.unitCost);
        }
    }
}

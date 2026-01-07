package io.github.android.gui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.artemis.ComponentMapper;

import java.util.List;

import io.github.android.utils.UiUtils;
import io.github.core.data.ClientGame;
import io.github.fortheoil.R;
import io.github.shared.data.component.LifeComponent;
import io.github.shared.data.component.NetComponent;
import io.github.shared.data.enums_types.EntityType;

public class ExistingEntityAdapter extends RecyclerView.Adapter<ExistingEntityAdapter.ViewHolder> {

    private final List<Integer> entityIds;
    private final OnEntityClickListener listener;

    public interface OnEntityClickListener {
        void onEntityClick(int entityId);
    }

    public ExistingEntityAdapter(List<Integer> entityIds, OnEntityClickListener listener) {
        this.entityIds = entityIds;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Utilise le layout de item_existing_building (ou crée item_existing_entity)
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_existing_entity, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int id = entityIds.get(position);

        ComponentMapper<NetComponent> mNet = ClientGame.getInstance().getWorld().getMapper(NetComponent.class);
        ComponentMapper<LifeComponent> mHealth = ClientGame.getInstance().getWorld().getMapper(LifeComponent.class);

        if (mNet.has(id)) {
            EntityType type = mNet.get(id).entityType;
            holder.txtName.setText(type.name());
            int iconRes = UiUtils.mapEntityTypeToDrawable(type);
            holder.imgIcon.setImageResource(iconRes);
        }

        if (mHealth.has(id)) {
            LifeComponent hc = mHealth.get(id);
            holder.progressHealth.setMax((int) hc.maxHealth);
            holder.progressHealth.setProgress((int) hc.health);

            // --- Logique de couleur dynamique ---
            float ratio = hc.health / hc.maxHealth;
            int color;

            if (ratio > 0.6f) {
                color = 0xFF4CAF50; // Vert (Material Green 500)
            } else if (ratio > 0.25f) {
                color = 0xFFFFC107; // Orange/Jaune (Material Amber 500)
            } else {
                color = 0xFFF44336; // Rouge (Material Red 500)
            }

            // On applique la couleur à la partie "progrès" de la barre
            holder.progressHealth.setProgressTintList(android.content.res.ColorStateList.valueOf(color));
        }

        holder.itemView.setOnClickListener(v -> listener.onEntityClick(id));
    }

    @Override
    public int getItemCount() {
        return entityIds.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName;
        ImageView imgIcon;
        ProgressBar progressHealth;

        ViewHolder(View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtBuildingName);
            imgIcon = itemView.findViewById(R.id.imgBuildingIcon);
            progressHealth = itemView.findViewById(R.id.progressHealth);
        }
    }
}

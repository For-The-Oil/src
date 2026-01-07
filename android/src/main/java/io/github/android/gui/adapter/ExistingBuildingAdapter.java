package io.github.android.gui.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.artemis.ComponentMapper;
import com.artemis.World;

import java.util.List;

import io.github.core.data.ClientGame;
import io.github.fortheoil.R;
import io.github.shared.data.component.LifeComponent;
import io.github.shared.data.component.NetComponent;

/**
 * Adaptateur pour la liste des bâtiments existants.
 * Utilise le NetID pour gérer la surbrillance, ce qui facilite la synchronisation avec la carte.
 */
public class ExistingBuildingAdapter extends RecyclerView.Adapter<ExistingBuildingAdapter.ViewHolder> {

    private static final String TAG = "ExistingBuildingAdapter";

    private final List<Integer> entityIds; // IDs locaux Artemis
    private final OnEntitySelectedListener listener;
    private final World world;

    /** ID Réseau (persistant) actuellement sélectionné pour la surbrillance. */
    private int selectedNetId = -1;

    public interface OnEntitySelectedListener {
        /** @param entityId L'ID interne ECS de l'entité. */
        void onSelected(int entityId);
    }

    public ExistingBuildingAdapter(List<Integer> entityIds, OnEntitySelectedListener listener) {
        this.entityIds = entityIds;
        this.listener = listener;
        this.world = ClientGame.getInstance().getWorld();
        Log.d(TAG, "Adapter initialisé avec " + entityIds.size() + " entités.");
    }

    /**
     * Définit le bâtiment à mettre en surbrillance via son NetID.
     * @param netId L'ID réseau de l'entité.
     */
    public void setSelectedNetId(int netId) {
        this.selectedNetId = netId;
        // Rafraîchit toute la liste pour déplacer la surbrillance
        Log.d(TAG, "Mise à jour de la surbrillance pour NetID: " + netId);
        notifyDataSetChanged();
    }

    /**
     * Trouve la position d'une entité dans la liste à partir de son NetID.
     */
    public int getPositionOfNetId(int netId) {
        ComponentMapper<NetComponent> mNet = world.getMapper(NetComponent.class);
        for (int i = 0; i < entityIds.size(); i++) {
            int eId = entityIds.get(i);
            if (mNet.has(eId) && mNet.get(eId).netId == netId) {
                return i;
            }
        }
        return -1;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_existing_building, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int eId = entityIds.get(position);

        ComponentMapper<NetComponent> mNet = world.getMapper(NetComponent.class);
        ComponentMapper<LifeComponent> mLife = world.getMapper(LifeComponent.class);

        int currentNetId = -1;

        // --- DONNÉES RÉSEAU ET NOM ---
        if (mNet.has(eId)) {
            NetComponent net = mNet.get(eId);
            currentNetId = net.netId;
            holder.tvName.setText(net.entityType.name());
            holder.tvId.setText("#" + net.netId);
        }

        // --- GESTION DE LA SURBRILLANCE (Basée sur le NetID) ---
        if (currentNetId != -1 && currentNetId == selectedNetId) {
            Log.d(TAG, "Highlight activé pour NetID: " + currentNetId);
            holder.itemView.setBackgroundResource(R.drawable.bg_item_selected);
        } else {
            holder.itemView.setBackgroundResource(android.R.color.transparent);
        }

        // --- SANTÉ ---
        if (mLife.has(eId)) {
            LifeComponent life = mLife.get(eId);
            holder.pbHealth.setMax((int) life.maxHealth);
            holder.pbHealth.setProgress((int) life.health);
        }

        // --- CLIC ---
        holder.itemView.setOnClickListener(v -> {
            if (mNet.has(eId)) {
                int clickedNetId = mNet.get(eId).netId;
                Log.d(TAG, "Clic liste: NetID=" + clickedNetId);

                // Met à jour la surbrillance localement
                setSelectedNetId(clickedNetId);

                // Informe le fragment (qui appellera onEntitySelected)
                listener.onSelected(eId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return entityIds.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvId;
        ProgressBar pbHealth;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvId = itemView.findViewById(R.id.tvItemId);
            pbHealth = itemView.findViewById(R.id.pbItemHealth);
        }
    }

    public void setEntityIds(List<Integer> newIds) {
        this.entityIds.clear();
        this.entityIds.addAll(newIds);
        notifyDataSetChanged();
    }

}

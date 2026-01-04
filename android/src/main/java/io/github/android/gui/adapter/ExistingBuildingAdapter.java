package io.github.android.gui.adapter;

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

public class ExistingBuildingAdapter extends RecyclerView.Adapter<ExistingBuildingAdapter.ViewHolder> {

    private final List<Integer> entityIds;
    private final OnEntitySelectedListener listener;
    private final World world;

    public interface OnEntitySelectedListener {
        void onSelected(int entityId);
    }

    public ExistingBuildingAdapter(List<Integer> entityIds, OnEntitySelectedListener listener) {
        this.entityIds = entityIds;
        this.listener = listener;
        this.world = ClientGame.getInstance().getWorld();
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

        if (mNet.has(eId)) {
            NetComponent net = mNet.get(eId);
            holder.tvName.setText(net.entityType.name());
            holder.tvId.setText("#" + net.netId);
        }

        if (mLife.has(eId)) {
            LifeComponent life = mLife.get(eId);
            holder.pbHealth.setMax((int) life.maxHealth);
            holder.pbHealth.setProgress((int) life.health);
        }

        holder.itemView.setOnClickListener(v -> listener.onSelected(eId));
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
}

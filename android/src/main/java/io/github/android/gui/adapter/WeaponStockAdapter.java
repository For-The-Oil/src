package io.github.android.gui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.github.android.gui.WeaponStock;
import io.github.fortheoil.R;

public class WeaponStockAdapter extends RecyclerView.Adapter<WeaponStockAdapter.ViewHolder> {

        public interface OnWeaponClickListener {
            void onWeaponClick(WeaponStock weapon);
        }

        private final List<WeaponStock> weapons;
        private final OnWeaponClickListener listener;

    public WeaponStockAdapter(List<WeaponStock> weapons,
                               OnWeaponClickListener listener) {
            this.weapons = weapons;
            this.listener = listener;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView icon;
            TextView name;
            TextView count;

            ViewHolder(View view) {
                super(view);
                icon = view.findViewById(R.id.imgWeapon);
                name = view.findViewById(R.id.txtWeaponName);
                count = view.findViewById(R.id.txtWeaponCount);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weapon_stock, parent, false);
            return new ViewHolder(view);
        }

    @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            WeaponStock weapon = weapons.get(position);
            holder.icon.setImageResource(weapon.iconRes);
            holder.name.setText(weapon.name);
            holder.count.setText("x" + weapon.quantity);

            holder.itemView.setEnabled(weapon.quantity > 0);
            holder.itemView.setAlpha(weapon.quantity > 0 ? 1f : 0.4f);

            holder.itemView.setOnClickListener(v -> {
                if (listener != null && weapon.quantity > 0) {
                    listener.onWeaponClick(weapon);
                }
            });
        }

        @Override
        public int getItemCount() {
            return weapons.size();
        }
    }

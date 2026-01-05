package io.github.android.gui.fragment.game;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.artemis.ComponentMapper;
import com.artemis.World;

import io.github.core.data.ClientGame;
import io.github.fortheoil.R;
import io.github.shared.data.component.LifeComponent;
import io.github.shared.data.component.NetComponent;
import io.github.shared.data.component.ProprietyComponent;
import io.github.shared.shared_engine.manager.EcsManager;

public class SelectedEntityFragment extends Fragment {

    private int netID;
    private int entityId;

    private TextView tvName, tvId, tvHpValue, tvTeam, tvArmor;
    private ProgressBar pbHealth;

    public SelectedEntityFragment(int netID) {
        this.netID = netID;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Le layout doit avoir android:clickable="true" pour bloquer les clics sur la map
        View root = inflater.inflate(R.layout.fragment_select_building, container, false);

        // --- INITIALISATION DES VUES (Correction du NullPointerException) ---
        tvName = root.findViewById(R.id.tvEntityName);
        tvId = root.findViewById(R.id.tvEntityId);
        tvHpValue = root.findViewById(R.id.tvHpValue);
        pbHealth = root.findViewById(R.id.pbHealth);
        tvTeam = root.findViewById(R.id.tvTeamName);  // Assurez-vous que cet ID existe dans votre XML
        tvArmor = root.findViewById(R.id.tvArmorValue); // Assurez-vous que cet ID existe dans votre XML

        // Logique ECS
        World world = ClientGame.getInstance().getWorld();
        if (world != null) {
            ComponentMapper<NetComponent> netMapper = world.getMapper(NetComponent.class);
            entityId = EcsManager.getIdByNetId(world, netID, netMapper);

            if (entityId != -1) {
                setupUI(world, entityId);
            } else if (tvName != null) {
                tvName.setText("Unknown Signal");
            }
        }

        return root;
    }

    private void setupUI(World world, int eId) {
        ComponentMapper<NetComponent> mNet = world.getMapper(NetComponent.class);
        ComponentMapper<LifeComponent> mLife = world.getMapper(LifeComponent.class);
        ComponentMapper<ProprietyComponent> mProp = world.getMapper(ProprietyComponent.class);

        // 1. Affichage de l'équipe (Team)
        if (tvTeam != null) {
            if (mProp.has(eId)) {
                String teamName = mProp.get(eId).team;
                tvTeam.setText("Team: " + (teamName != null ? teamName : "Neutral"));

                // Couleur selon l'équipe
                if ("Ennemy".equalsIgnoreCase(teamName) || "Enemy".equalsIgnoreCase(teamName)) {
                    tvTeam.setTextColor(0xFFFF4444); // Rouge
                } else if ("Ally".equalsIgnoreCase(teamName)) {
                    tvTeam.setTextColor(0xFF44FF44); // Vert
                }
            } else {
                tvTeam.setText("No Team");
            }
        }

        // 2. Affichage des infos de base (Type et ID)
        if (mNet.has(eId)) {
            NetComponent netComp = mNet.get(eId);
            if (tvName != null) tvName.setText(netComp.entityType.name());
            if (tvId != null) tvId.setText("NetID: " + netID);
        }

        // 3. Affichage de la vie et de l'armure
        if (mLife.has(eId)) {
            LifeComponent life = mLife.get(eId);

            int currentHp = (int) life.health;
            int maxHp = (int) life.maxHealth;
            int armor = life.armor;

            if (tvHpValue != null) tvHpValue.setText(currentHp + " / " + maxHp);

            if (tvArmor != null) {
                tvArmor.setText("Armor: " + armor);
                tvArmor.setVisibility(View.VISIBLE);
            }

            if (pbHealth != null) {
                pbHealth.setMax(maxHp);
                pbHealth.setProgress(currentHp);
                pbHealth.setVisibility(View.VISIBLE);
            }
        } else {
            if (tvHpValue != null) tvHpValue.setText("N/A");
            if (tvArmor != null) tvArmor.setVisibility(View.GONE);
            if (pbHealth != null) pbHealth.setVisibility(View.INVISIBLE);
        }
    }
}

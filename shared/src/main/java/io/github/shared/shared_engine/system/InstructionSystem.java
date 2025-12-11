
package io.github.shared.shared_engine.system;

import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.Entity;

import java.util.HashMap;

import io.github.shared.data.IGame;
import io.github.shared.data.component.BuildingMapPositionComponent;
import io.github.shared.data.component.FreezeComponent;
import io.github.shared.data.component.LifeComponent;
import io.github.shared.data.component.MeleeAttackComponent;
import io.github.shared.data.component.MoveComponent;
import io.github.shared.data.component.NetComponent;
import io.github.shared.data.component.OnCreationComponent;
import io.github.shared.data.component.PositionComponent;
import io.github.shared.data.component.ProjectileAttackComponent;
import io.github.shared.data.component.ProjectileComponent;
import io.github.shared.data.component.ProprietyComponent;
import io.github.shared.data.component.RangedAttackComponent;
import io.github.shared.data.component.RessourceComponent;
import io.github.shared.data.enums_types.Direction;
import io.github.shared.data.enums_types.EntityType;
import io.github.shared.data.enums_types.WeaponType;
import io.github.shared.data.gameobject.Shape;
import io.github.shared.data.instructions.CreateInstruction;
import io.github.shared.data.instructions.DestroyInstruction;
import io.github.shared.data.instructions.EventsInstruction;
import io.github.shared.data.instructions.Instruction;
import io.github.shared.data.instructions.ResourcesInstruction;
import io.github.shared.data.instructions.UpdateEntityInstruction;
import io.github.shared.data.network.Player;
import io.github.shared.data.snapshot.EntitySnapshot;
import io.github.shared.shared_engine.Utility;
import io.github.shared.shared_engine.factory.EntityFactory;
import io.github.shared.shared_engine.manager.EcsManager;
import io.github.shared.shared_engine.manager.ShapeManager;

public class InstructionSystem extends BaseSystem {
    private ComponentMapper<NetComponent> netMapper;
    private ComponentMapper<PositionComponent> positionMapper;
    private ComponentMapper<ProprietyComponent> proprietyMapper;
    private ComponentMapper<BuildingMapPositionComponent> buildingMapPositionMapper;
    private ComponentMapper<RessourceComponent> resMapper;
    private ComponentMapper<OnCreationComponent> onCreateMapper;
    private ComponentMapper<MeleeAttackComponent> meleeAttackMapper;
    private ComponentMapper<RangedAttackComponent> rangedAttackMapper;
    private ComponentMapper<ProjectileAttackComponent> projectileAttackMapper;
    private ComponentMapper<LifeComponent> lifeMapper;
    private ComponentMapper<FreezeComponent> freezeMapper;

    private ComponentMapper<ProjectileComponent> projectileMapper;
    private ComponentMapper<MoveComponent> moveMapper;

    public InstructionSystem() {}
    @Override
    protected void processSystem() {
        // no-op
    }
    public void executeInstruction(Instruction instruction,IGame game) {
        String type = instruction.getClass().getSimpleName();
        try {
            switch (type) {
                case "CreateInstruction":
                    CreateInstruction ci = (CreateInstruction) instruction;

                    for (int i = 0; i < ci.getToSpawn().size(); i++) {
                        float x = ci.getPosX().get(i);
                        float y = ci.getPosY().get(i);
                        int netId = ci.getNetId().get(i);
                        int from = ci.getFrom().get(i);
                        Direction direction = ci.getDirections().get(i);
                        EntityType entityType = ci.getToSpawn().get(i);
                        Entity entity = world.createEntity();

                        Player player = Utility.findPlayerByUuid(game.getPlayersList(), ci.getPlayer().get(i));
                        if (player != null) {
                            Utility.subtractResourcesInPlace(player.getResources(), entityType.getCost());
                            ProprietyComponent prc = proprietyMapper.create(entity);
                            prc.set(player.getUuid(), Utility.findTeamByPlayer(player, game.getPlayerTeam()));
                        }

                        if (entityType.getType().equals(EntityType.Type.Building)) {
                            Shape overlay = new Shape(entityType.getShapeType().getShape(), netId);
                            ShapeManager.overlayShape(game.getMap(), ShapeManager.rotateShape(overlay, direction), (int) x, (int) y, 0, 0, overlay.getWidth(), overlay.getHeight());
                            game.setMapDirty(true);

                            BuildingMapPositionComponent bpc = buildingMapPositionMapper.create(entity);
                            bpc.set(Utility.worldToCell(x),Utility.worldToCell(y),direction);

                            PositionComponent posC = positionMapper.create(entity);
                            posC.set(x,y,0);
                            posC.horizontalRotation = direction.getAngleRadians();

                            if(entityType.getProduction()!= null){
                                RessourceComponent res = resMapper.create(entity);
                                res.set(new HashMap<>(entityType.getProduction()));
                            }
                        }

                        if(entityType.getType().equals(EntityType.Type.Building)||entityType.getType().equals(EntityType.Type.Unit)){
                            FreezeComponent fc= freezeMapper.create(entity);
                            fc.freeze_time = entityType.getFreeze_time();

                            LifeComponent lc = lifeMapper.create(entity);
                            lc.set(entityType.getMaxHealth(),entityType.getMaxHealth(),entityType.getArmor(),entityType.getPassiveHeal());

                            OnCreationComponent occ = onCreateMapper.create(entity);
                            occ.set(from, entityType.getCreate_time());

                            for(WeaponType weaponType : entityType.getWeaponType()){
                                if(weaponType.getType().equals(WeaponType.Type.Melee)){
                                    MeleeAttackComponent melee = meleeAttackMapper.get(entity);
                                    if(melee == null)melee = meleeAttackMapper.create(entity);
                                    melee.set(weaponType,weaponType.getDamage(),weaponType.getCooldown(), weaponType.getReach());
                                }
                                else if(weaponType.getType().equals(WeaponType.Type.Range)){
                                    RangedAttackComponent ranged = rangedAttackMapper.get(entity);
                                    if(ranged == null)ranged = rangedAttackMapper.create(entity);
                                    ranged.set(weaponType,weaponType.getDamage(),weaponType.getCooldown(), weaponType.getReach());
                                }
                                else if(weaponType.getType().equals(WeaponType.Type.ProjectileLauncher)){
                                    ProjectileAttackComponent projectile = projectileAttackMapper.get(entity);
                                    if(projectile == null)projectile = projectileAttackMapper.create(entity);
                                    projectile.set(weaponType,weaponType.getCooldown(), weaponType.getReach(),weaponType.getProjectileType());
                                }
                            }

                        }

                        if(entityType.getType().equals(EntityType.Type.Projectile)){
                            PositionComponent posFromC = EcsManager.getPositionByNetId(world, from,netMapper,positionMapper);

                            PositionComponent posC = positionMapper.create(entity);
                            posC.set(posFromC.x, posFromC.y,0);

                            ProjectileComponent pc = projectileMapper.create(entity);
                            pc.set(entityType,entityType.getDamage(),entityType.getAoe(),entityType.getMaxHeight(),posFromC.x,posFromC.y);

                            MoveComponent mc = moveMapper.create(entity);
                            mc.set(false,x,y,true);
                        }

                        NetComponent nc = netMapper.create(entity);
                        nc.set(netId, entityType);
                    }
                    break;

                case "UpdateEntityInstruction":
                    for (EntitySnapshot snapshot : ((UpdateEntityInstruction) (instruction)).getToUpdate()) {
                        EntityFactory.applySnapshotToEntity(world, snapshot);
                    }
                    break;
                case "DestroyInstruction":
                    for (int netId : ((DestroyInstruction) (instruction)).getToKill()) {
                        EntityFactory.destroyEntityByNetId(world, netId, game);
                    }
                    break;
                case "ResourcesInstruction":
                    ResourcesInstruction resourcesInstruction = (ResourcesInstruction) instruction;
                    for (Player player : game.getPlayersList()) {
                        if (player.getUuid().equals(resourcesInstruction.getPlayer())) {
                            player.setResources(resourcesInstruction.getRessources());
                        }
                    }
                    break;
                case "EventsInstruction":
                    game.setCurrentEvent(((EventsInstruction) (instruction)).getEventType());
                    break;
                case "FinalInstruction":
                    break;
                case "SpecialRequestsInstruction":
                    break;
                default:
                    throw new IllegalArgumentException("Instruction non pris en charge : " + type);
            }
        } catch (Exception e) {
            System.err.print("executeInstruction err " + e);
        }
    }
}

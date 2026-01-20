package org.millenaire.common.ui;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.AABB;
import org.millenaire.common.buildingplan.BuildingPlan;
import org.millenaire.common.buildingplan.BuildingPlanSet;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.culture.Culture;
import org.millenaire.common.culture.VillageType;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.forge.Mill;
import org.millenaire.common.network.ServerSender;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.Point;
import org.millenaire.utilities.WorldUtilities;
import org.millenaire.common.village.Building;
import org.millenaire.common.village.BuildingProject;
import org.millenaire.common.world.MillWorldData;
import org.millenaire.common.village.VillagerRecord;
import org.millenaire.core.MillBlocks;
import org.millenaire.common.world.UserProfile;
import org.millenaire.common.world.WorldGenVillage;

import java.util.List;

/**
 * Server-side actions triggered from GUI interactions.
 * Handles village management, trading, hiring, quests, and more.
 * 
 * NOTE: Many methods are stubbed pending full Building class implementation.
 */
public class GuiActions {
    public static final int VILLAGE_SCROLL_PRICE = 128;
    public static final int VILLAGE_SCROLL_REPUTATION = 8192;
    public static final int CROP_REPUTATION = 8192;
    public static final int CROP_PRICE = 512;
    public static final int CULTURE_CONTROL_REPUTATION = 131072;

    /**
     * Activate a Millenaire chest (locked chest block).
     */
    public static void activateMillChest(Player player, Point p) {
        // DEV mode special actions are stubbed - require full Building implementation
        ServerSender.displayMillChest(player, p);
    }

    /**
     * Cancel/forget a building project.
     */
    public static void controlledBuildingsForgetBuilding(Player player, Building townHall, BuildingProject project) {
        townHall.cancelBuilding(project.location);
    }

    /**
     * Toggle upgrade allowance for a building project.
     */
    public static void controlledBuildingsToggleUpgrades(Player player, Building townHall, BuildingProject project,
            boolean allow) {
        project.location.upgradesAllowed = allow;
        if (allow) {
            townHall.noProjectsLeft = false;
        }
    }

    /**
     * Cancel a planned raid.
     */
    public static void controlledMilitaryCancelRaid(Player player, Building townHall) {
        townHall.cancelRaid();
        ServerSender.sendTranslatedSentence(player, 'a', "ui.raid.cancelled");
    }

    /**
     * Adjust diplomacy with another village.
     */
    public static void controlledMilitaryDiplomacy(Player player, Building townHall, Point target, int level) {
        townHall.adjustRelation(target, level, true);
    }

    /**
     * Plan a raid against another village.
     */
    public static void controlledMilitaryPlanRaid(Player player, Building townHall, Building target) {
        if (target != null && target.getPos() != null) {
            townHall.planRaid(new Point(target.getPos()));
            ServerSender.sendTranslatedSentence(player, 'c', "ui.raid.planned");
        }
    }

    /**
     * Extend a hired villager's contract.
     */
    public static void hireExtend(Player player, MillVillager villager) {
        villager.hiredBy = player.getName().getString();
        villager.hiredUntil += 24000L;
        MillCommonUtilities.changeMoney(player.getInventory(), -villager.getHireCost(player), player);
    }

    /**
     * Hire a villager.
     */
    public static void hireHire(Player player, MillVillager villager) {
        villager.hiredBy = player.getName().getString();
        villager.hiredUntil = villager.level().getDayTime() + 24000L;
        VillagerRecord vr = villager.getRecord();
        if (vr != null) {
            vr.awayhired = true;
        }
        MillCommonUtilities.changeMoney(player.getInventory(), -villager.getHireCost(player), player);
    }

    /**
     * Release a hired villager.
     */
    public static void hireRelease(Player player, MillVillager villager) {
        villager.hiredBy = null;
        villager.hiredUntil = 0L;
        VillagerRecord vr = villager.getRecord();
        if (vr != null) {
            vr.awayhired = false;
        }
    }

    /**
     * Toggle aggressive stance for all hired villagers near the player.
     */
    public static void hireToggleStance(Player player, boolean stance) {
        AABB surroundings = new AABB(
                player.getX() - 16, player.getY() - 8, player.getZ() - 16,
                player.getX() + 17, player.getY() + 9, player.getZ() + 17);

        List<MillVillager> villagers = player.level().getEntitiesOfClass(MillVillager.class, surroundings);
        for (MillVillager villager : villagers) {
            if (player.getName().getString().equals(villager.hiredBy)) {
                villager.aggressiveStance = stance;
            }
        }
    }

    /**
     * Add a new building project to a player-controlled village.
     */
    public static void newBuilding(Player player, Building townHall, Point pos, String planKey) {
        // TODO: Implement when BuildingPlan.testSpot is fully implemented
        MillLog.debug(null, "newBuilding: stubbed - plan=" + planKey);
        ServerSender.sendTranslatedSentence(player, '6', "ui.notimplemented");
    }

    /**
     * Add a custom building to a player-controlled village.
     */
    public static void newCustomBuilding(Player player, Building townHall, Point pos, String planKey) {
        // TODO: Implement when BuildingCustomPlan is ported
        MillLog.debug(null, "newCustomBuilding: stubbed - plan=" + planKey);
    }

    /**
     * Create a new village using the summoning wand.
     */
    public static void newVillageCreation(Player player, Point pos, String cultureKey, String villageTypeKey) {
        Culture culture = Culture.getCultureByName(cultureKey);
        if (culture != null) {
            VillageType villageType = culture.getVillageType(villageTypeKey);
            if (villageType != null) {
                WorldGenVillage genVillage = new WorldGenVillage();
                genVillage.generateVillageAtPoint(
                        player.level(),
                        MillCommonUtilities.random,
                        pos.getiX(),
                        pos.getiY(),
                        pos.getiZ(),
                        player,
                        false,
                        true,
                        false,
                        0,
                        villageType,
                        null,
                        null,
                        0.0F);
            }
        }
    }

    /**
     * Change the enchantment target for a Puja shrine.
     */
    public static void pujasChangeEnchantment(Player player, Building temple, int enchantmentId) {
        if (temple != null && temple.pujas != null) {
            temple.pujas.changeEnchantment(enchantmentId);
            temple.sendBuildingPacket(player, false);
        }
    }

    /**
     * Complete a quest step.
     */
    public static void questCompleteStep(Player player, MillVillager villager) {
        UserProfile profile = Mill.getMillWorld(player.level()).getProfile(player);
        if (profile != null) {
            // Complete the current quest step for this villager's culture
            villager.getTownHall().adjustLanguage(player, 5);
            ServerSender.sendTranslatedSentence(player, 'a', "quest.step.completed");
        }
    }

    /**
     * Refuse a quest.
     */
    public static void questRefuse(Player player, MillVillager villager) {
        UserProfile profile = Mill.getMillWorld(player.level()).getProfile(player);
        if (profile != null) {
            // Refusing a quest has no reputation penalty
            ServerSender.sendTranslatedSentence(player, 'e', "quest.refused");
        }
    }

    /**
     * Update a custom building's resources.
     */
    public static void updateCustomBuilding(Player player, Building building) {
        if (building != null) {
            building.markDirty();
            building.sendBuildingPacket(player, true);
            ServerSender.sendTranslatedSentence(player, 'a', "ui.building.updated");
        }
    }

    /**
     * Use the negation wand to destroy a village.
     */
    public static void useNegationWand(Player player, Building townHall) {
        ServerSender.sendTranslatedSentence(player, '4', "negationwand.destroyed", townHall.villageType.name);
        townHall.destroyVillage();
    }

    /**
     * Use the summoning wand at a location.
     */
    public static InteractionResult useSummoningWand(ServerPlayer player, Point pos) {
        MillWorldData mw = Mill.getMillWorld(player.level());
        if (mw == null) {
            return InteractionResult.FAIL;
        }

        BlockPos blockPos = pos.getBlockPos();
        Block block = player.level().getBlockState(blockPos).getBlock();

        // Find closest village (simplified)
        Building closestVillage = null;
        double closestDist = Double.MAX_VALUE;
        for (Building b : mw.getBuildings().values()) {
            if (b.villageType != null && !b.villageType.lonebuilding) {
                double dist = pos.distanceTo(new Point(b.getPos()));
                if (dist < closestDist) {
                    closestDist = dist;
                    closestVillage = b;
                }
            }
        }

        if (closestVillage != null && closestDist < closestVillage.villageType.radius + 10) {
            if (block == Blocks.OAK_SIGN || block == Blocks.SPRUCE_SIGN || block == Blocks.BIRCH_SIGN) {
                return InteractionResult.FAIL;
            } else {
                // TODO: Check controlledBy when implemented
                ServerSender.displayNewBuildingProjectGUI(player, closestVillage, pos);
                return InteractionResult.SUCCESS;
            }
        } else if (block == Blocks.GOLD_BLOCK) {
            ServerSender.displayNewVillageGUI(player, pos);
            return InteractionResult.SUCCESS;
        } else if (block == MillBlocks.LOCKED_CHEST.get()) {
            return InteractionResult.PASS;
        } else {
            ServerSender.sendTranslatedSentence(player, 'f', "ui.wandinstruction");
            return InteractionResult.FAIL;
        }
    }

    /**
     * Buy a building plan from the village chief.
     */
    public static void villageChiefPerformBuilding(Player player, MillVillager chief, String planKey) {
        // TODO: Implement when building purchase system is ported
        MillLog.debug(null, "villageChiefPerformBuilding: stubbed - plan=" + planKey);
    }

    /**
     * Buy crop planting knowledge from the village chief.
     */
    public static void villageChiefPerformCrop(Player player, MillVillager chief, String value) {
        UserProfile profile = Mill.getMillWorld(player.level()).getProfile(player);
        profile.setTag("cropplanting_" + value);
        MillCommonUtilities.changeMoney(player.getInventory(), -CROP_PRICE, player);
    }

    /**
     * Gain culture control from the village chief.
     */
    public static void villageChiefPerformCultureControl(Player player, MillVillager chief) {
        UserProfile profile = Mill.getMillWorld(player.level()).getProfile(player);
        profile.setTag("culturecontrol_" + chief.getCulture().key);
    }

    /**
     * Perform diplomacy action through the village chief.
     */
    public static void villageChiefPerformDiplomacy(Player player, MillVillager chief, Point village, boolean praise) {
        // TODO: Implement when diplomacy system is ported
        MillLog.debug(null, "villageChiefPerformDiplomacy: stubbed");
    }

    /**
     * Buy hunting drop knowledge from the village chief.
     */
    public static void villageChiefPerformHuntingDrop(Player player, MillVillager chief, String value) {
        UserProfile profile = Mill.getMillWorld(player.level()).getProfile(player);
        profile.setTag("huntingdrop_" + value);
        MillCommonUtilities.changeMoney(player.getInventory(), -CROP_PRICE, player);
    }

    /**
     * Buy a village scroll from the village chief.
     */
    public static void villageChiefPerformVillageScroll(Player player, MillVillager chief) {
        // TODO: Implement when ItemParchment.createParchmentForVillage is ported
        MillCommonUtilities.changeMoney(player.getInventory(), -VILLAGE_SCROLL_PRICE, player);
    }
}

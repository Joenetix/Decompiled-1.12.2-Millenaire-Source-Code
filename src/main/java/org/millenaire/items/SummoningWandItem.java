package org.millenaire.items;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.millenaire.core.VillageData;
import org.millenaire.common.culture.Culture;
import org.millenaire.common.culture.MillenaireCultures;
import org.millenaire.worldgen.VillageStarter;

/**
 * Summoning Wand item - allows player to spawn Millénaire villages.
 * 
 * Usage:
 * - Right-click: Opens culture selection
 * - Shift + Right-click: Cycles through cultures
 * - Left-click on ground: Spawns village of selected culture
 */
public class SummoningWandItem extends Item {

    // Store selected culture in NBT
    private static final String CULTURE_TAG = "SelectedCulture";

    public SummoningWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        ServerLevel serverLevel = (ServerLevel) level;

        // Shift + Right-click: Cycle through cultures
        if (player.isShiftKeyDown()) {
            cycleCulture(stack, player);
            return InteractionResultHolder.success(stack);
        }

        // Regular Right-click: Spawn village at targeted location
        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos targetPos = hitResult.getBlockPos().above();

            // Get selected culture
            String cultureId = getSelectedCulture(stack);
            Culture culture = MillenaireCultures.getCulture(cultureId);

            if (culture == null) {
                player.sendSystemMessage(Component.literal("§cNo culture selected! Shift + Right-click to select."));
                return InteractionResultHolder.fail(stack);
            }

            // Spawn the village
            player.sendSystemMessage(Component.literal("§6Spawning " + culture.getDisplayName() + " village..."));

            VillageData village = VillageStarter.spawnStarterVillage(serverLevel, targetPos, culture);

            if (village != null) {
                player.sendSystemMessage(Component.literal(
                        "§aSuccessfully spawned " + culture.getDisplayName() + " village!\n" +
                                "§7Village ID: §f" + village.getVillageId() + "\n" +
                                "§7Buildings: §f" + village.getBuildings().size()));

                // Consume item in survival mode
                if (!player.isCreative()) {
                    stack.shrink(1);
                }

                return InteractionResultHolder.consume(stack);
            } else {
                player.sendSystemMessage(Component.literal("§cFailed to spawn village!"));
                return InteractionResultHolder.fail(stack);
            }
        }

        return InteractionResultHolder.pass(stack);
    }

    /**
     * Cycle to the next culture
     */
    private void cycleCulture(ItemStack stack, Player player) {
        String currentCulture = getSelectedCulture(stack);
        String[] cultures = { "norman", "indian", "japanese", "mayan", "byzantines" };

        // Find current index
        int currentIndex = 0;
        for (int i = 0; i < cultures.length; i++) {
            if (cultures[i].equals(currentCulture)) {
                currentIndex = i;
                break;
            }
        }

        // Cycle to next
        int nextIndex = (currentIndex + 1) % cultures.length;
        String nextCulture = cultures[nextIndex];

        setSelectedCulture(stack, nextCulture);

        Culture culture = MillenaireCultures.getCulture(nextCulture);
        if (culture != null) {
            player.sendSystemMessage(Component.literal(
                    "§6Selected culture: §b" + culture.getDisplayName()));
        }
    }

    /**
     * Get the selected culture from the wand
     */
    private String getSelectedCulture(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(CULTURE_TAG)) {
            return stack.getTag().getString(CULTURE_TAG);
        }
        return "norman"; // Default to Norman
    }

    /**
     * Set the selected culture on the wand
     */
    private void setSelectedCulture(ItemStack stack, String cultureId) {
        stack.getOrCreateTag().putString(CULTURE_TAG, cultureId);
    }
}

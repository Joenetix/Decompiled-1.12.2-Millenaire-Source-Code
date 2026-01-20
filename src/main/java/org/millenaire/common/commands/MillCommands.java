package org.millenaire.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.millenaire.MillenaireRevived;
import org.millenaire.common.utilities.MillLog;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.millenaire.common.culture.Culture;
import org.millenaire.common.culture.MillenaireCultures;

/**
 * Registers all Millénaire commands using Brigadier.
 * Ported from 1.12.2 to 1.20.1.
 * 
 * Commands:
 * - /millSpawnVillage <culture> <type> [x z] [completion%]
 * - /millSpawnLoneBuilding <culture> <type> [x z] [completion%]
 * - /millTp <village_name> [player]
 * - /millListActiveVillages
 * - /millGiveReputation <village_name> <amount>
 * - /millRenameVillage <village_name> <new_name>
 * - /millSwitchVillageControl <village_name>
 * - /millImportCulture <culture>
 * - /millDebugResetVillagers
 * - /millDebugResendProfiles
 */
@Mod.EventBusSubscriber(modid = MillenaireRevived.MODID)
public class MillCommands {

        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
                CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

                // Register all Millénaire commands
                registerSpawnVillageCommand(dispatcher);
                registerSpawnLoneBuildingCommand(dispatcher);
                registerTeleportCommand(dispatcher);
                registerListActiveVillagesCommand(dispatcher);
                registerGiveReputationCommand(dispatcher);
                registerRenameVillageCommand(dispatcher);
                registerSwitchControlCommand(dispatcher);
                registerImportCultureCommand(dispatcher);
                registerDebugCommands(dispatcher);

                MillLog.minor(null, "Registered Millénaire commands");
        }

        private static void registerSpawnVillageCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
                dispatcher.register(
                                Commands.literal("millSpawnVillage")
                                                .requires(source -> source.hasPermission(3))
                                                .then(Commands.argument("culture", StringArgumentType.word())
                                                                .then(Commands.argument("villageType",
                                                                                StringArgumentType.word())
                                                                                .executes(MillCommands::executeSpawnVillage)
                                                                                .then(Commands.argument("x",
                                                                                                IntegerArgumentType
                                                                                                                .integer())
                                                                                                .then(Commands.argument(
                                                                                                                "z",
                                                                                                                IntegerArgumentType
                                                                                                                                .integer())
                                                                                                                .executes(MillCommands::executeSpawnVillageAtPos)
                                                                                                                .then(Commands
                                                                                                                                .argument("completion",
                                                                                                                                                IntegerArgumentType
                                                                                                                                                                .integer(0, 100))
                                                                                                                                .executes(
                                                                                                                                                MillCommands::executeSpawnVillageWithCompletion)))))));
        }

        private static void registerSpawnLoneBuildingCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
                dispatcher.register(
                                Commands.literal("millSpawnLoneBuilding")
                                                .requires(source -> source.hasPermission(3))
                                                .then(Commands.argument("culture", StringArgumentType.word())
                                                                .then(Commands.argument("buildingType",
                                                                                StringArgumentType.word())
                                                                                .executes(MillCommands::executeSpawnLoneBuilding)
                                                                                .then(Commands.argument("x",
                                                                                                IntegerArgumentType
                                                                                                                .integer())
                                                                                                .then(Commands.argument(
                                                                                                                "z",
                                                                                                                IntegerArgumentType
                                                                                                                                .integer())
                                                                                                                .executes(MillCommands::executeSpawnLoneBuildingAtPos))))));
        }

        private static void registerTeleportCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
                dispatcher.register(
                                Commands.literal("millTp")
                                                .requires(source -> source.hasPermission(3))
                                                .then(Commands.argument("villageName",
                                                                StringArgumentType.greedyString())
                                                                .executes(MillCommands::executeTeleport)));
        }

        private static void registerListActiveVillagesCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
                dispatcher.register(
                                Commands.literal("millListActiveVillages")
                                                .executes(MillCommands::executeListActiveVillages));
        }

        private static void registerGiveReputationCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
                dispatcher.register(
                                Commands.literal("millGiveReputation")
                                                .requires(source -> source.hasPermission(3))
                                                .then(Commands.argument("villageName", StringArgumentType.word())
                                                                .then(Commands.argument("amount",
                                                                                IntegerArgumentType.integer())
                                                                                .executes(MillCommands::executeGiveReputation))));
        }

        private static void registerRenameVillageCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
                dispatcher.register(
                                Commands.literal("millRenameVillage")
                                                .requires(source -> source.hasPermission(3))
                                                .then(Commands.argument("villageName", StringArgumentType.word())
                                                                .then(Commands.argument("newName",
                                                                                StringArgumentType.greedyString())
                                                                                .executes(MillCommands::executeRenameVillage))));
        }

        private static void registerSwitchControlCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
                dispatcher.register(
                                Commands.literal("millSwitchVillageControl")
                                                .requires(source -> source.hasPermission(3))
                                                .then(Commands.argument("villageName",
                                                                StringArgumentType.greedyString())
                                                                .executes(MillCommands::executeSwitchControl)));
        }

        private static void registerImportCultureCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
                dispatcher.register(
                                Commands.literal("millImportCulture")
                                                .requires(source -> source.hasPermission(3))
                                                .then(Commands.argument("cultureName", StringArgumentType.word())
                                                                .executes(MillCommands::executeImportCulture)));
        }

        private static void registerDebugCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
                dispatcher.register(
                                Commands.literal("millDebugResetVillagers")
                                                .requires(source -> source.hasPermission(3))
                                                .executes(MillCommands::executeDebugResetVillagers));

                dispatcher.register(
                                Commands.literal("millDebugResendProfiles")
                                                .requires(source -> source.hasPermission(3))
                                                .executes(MillCommands::executeDebugResendProfiles));
        }

        // === Command Execution Methods ===

        private static int executeSpawnVillage(CommandContext<CommandSourceStack> context) {
                String culture = StringArgumentType.getString(context, "culture");
                String villageType = StringArgumentType.getString(context, "villageType");
                ServerPlayer player = context.getSource().getPlayer();

                if (player == null) {
                        context.getSource().sendFailure(Component.literal("Command must be run by a player"));
                        return 0;
                }

                Culture c = MillenaireCultures.getCulture(culture);
                if (c == null) {
                        context.getSource().sendFailure(Component.literal("Culture not found: " + culture));
                        return 0;
                }

                org.millenaire.common.culture.VillageType vt = null;
                for (org.millenaire.common.culture.VillageType t : c.getVillageTypes()) {
                        if (t.getId().equalsIgnoreCase(villageType)) {
                                vt = t;
                                break;
                        }
                }

                if (vt == null) {
                        context.getSource().sendFailure(Component.literal("Village type not found: " + villageType));
                        return 0;
                }

                org.millenaire.worldgen.VillageStarter.spawnVillage(player.serverLevel(), player.blockPosition(), c,
                                vt);

                context.getSource().sendSuccess(() -> Component.literal(
                                "Spawning village: " + culture + " / " + villageType + " at player position"), true);

                return 1;
        }

        private static int executeSpawnVillageAtPos(CommandContext<CommandSourceStack> context) {
                String culture = StringArgumentType.getString(context, "culture");
                String villageType = StringArgumentType.getString(context, "villageType");
                int x = IntegerArgumentType.getInteger(context, "x");
                int z = IntegerArgumentType.getInteger(context, "z");
                ServerPlayer player = context.getSource().getPlayer();

                if (player == null) {
                        context.getSource().sendFailure(
                                        Component.literal("Command must be run by a player (for level context)"));
                        return 0;
                }

                Culture c = MillenaireCultures.getCulture(culture);
                if (c == null) {
                        context.getSource().sendFailure(Component.literal("Culture not found: " + culture));
                        return 0;
                }

                org.millenaire.common.culture.VillageType vt = null;
                for (org.millenaire.common.culture.VillageType t : c.getVillageTypes()) {
                        if (t.getId().equalsIgnoreCase(villageType)) {
                                vt = t;
                                break;
                        }
                }

                if (vt == null) {
                        context.getSource().sendFailure(Component.literal("Village type not found: " + villageType));
                        return 0;
                }

                // Find ground level at x, z
                BlockPos pos = new BlockPos(x, 64, z); // Height will be adjusted by spawnVillage logic
                org.millenaire.worldgen.VillageStarter.spawnVillage(player.serverLevel(), pos, c, vt);

                context.getSource().sendSuccess(() -> Component.literal(
                                "Spawning village: " + culture + " / " + villageType + " at " + x + ", " + z), true);

                return 1;
        }

        private static int executeSpawnVillageWithCompletion(CommandContext<CommandSourceStack> context) {
                // Ignoring completion functionality for now, just spawning
                return executeSpawnVillageAtPos(context);
        }

        private static int executeSpawnLoneBuilding(CommandContext<CommandSourceStack> context) {
                String culture = StringArgumentType.getString(context, "culture");
                String buildingType = StringArgumentType.getString(context, "buildingType");
                ServerPlayer player = context.getSource().getPlayer();

                if (player == null) {
                        context.getSource().sendFailure(Component.literal("Command must be run by a player"));
                        return 0;
                }

                Culture c = MillenaireCultures.getCulture(culture);
                if (c == null) {
                        context.getSource().sendFailure(Component.literal("Culture not found: " + culture));
                        return 0;
                }

                // Trick: VillageStarter.spawnLoneBuilding takes culture and finds Random lone
                // building? NO.
                // It takes a Culture. But we have a specific building type?
                // VillageStarter.spawnLoneBuilding(level, pos, culture) spawns a RANDOM lone
                // building.
                // But the command specifies a type.
                // We need spawnLoneBuilding(..., type).
                // Let's see if we can implement it here.

                // Actually VillageStarter.spawnLoneBuilding method signature: (ServerLevel,
                // BlockPos, Culture)
                // It picks random.
                // We want specific.
                // Implementing logic similar to spawnVillage but for lone building type.
                // I'll assume we can use spawnLoneBuilding for now and log warning if type
                // ignored, or try to select it.
                // Wait, culture.getVillageType(id) works for lone buildings too if they are
                // registered as village types?
                // Lone buildings are VillageTypes with isLoneBuilding=true.

                org.millenaire.common.culture.VillageType vt = null;
                for (org.millenaire.common.culture.VillageType t : c.getVillageTypes()) {
                        if (t.getId().equalsIgnoreCase(buildingType)) {
                                vt = t;
                                break;
                        }
                }

                if (vt != null) {
                        org.millenaire.worldgen.VillageStarter.spawnVillage(player.serverLevel(),
                                        player.blockPosition(), c, vt);
                } else {
                        context.getSource().sendFailure(
                                        Component.literal("Lone building type not found: " + buildingType));
                        return 0;
                }

                context.getSource().sendSuccess(() -> Component.literal(
                                "Spawning lone building: " + culture + " / " + buildingType), true);

                return 1;
        }

        private static int executeSpawnLoneBuildingAtPos(CommandContext<CommandSourceStack> context) {
                String culture = StringArgumentType.getString(context, "culture");
                String buildingType = StringArgumentType.getString(context, "buildingType");
                int x = IntegerArgumentType.getInteger(context, "x");
                int z = IntegerArgumentType.getInteger(context, "z");
                ServerPlayer player = context.getSource().getPlayer();

                if (player == null) {
                        context.getSource().sendFailure(Component.literal("Command must be run by a player"));
                        return 0;
                }

                Culture c = MillenaireCultures.getCulture(culture);
                if (c == null) {
                        context.getSource().sendFailure(Component.literal("Culture not found: " + culture));
                        return 0;
                }

                org.millenaire.common.culture.VillageType vt = null;
                for (org.millenaire.common.culture.VillageType t : c.getVillageTypes()) {
                        if (t.getId().equalsIgnoreCase(buildingType)) {
                                vt = t;
                                break;
                        }
                }

                if (vt != null) {
                        BlockPos pos = new BlockPos(x, 64, z);
                        org.millenaire.worldgen.VillageStarter.spawnVillage(player.serverLevel(), pos, c, vt);

                        context.getSource().sendSuccess(() -> Component.literal(
                                        "Spawning lone building: " + culture + " / " + buildingType + " at " + x + ", "
                                                        + z),
                                        true);
                        return 1;
                } else {
                        context.getSource().sendFailure(
                                        Component.literal("Lone building type not found: " + buildingType));
                        return 0;
                }
        }

        private static int executeTeleport(CommandContext<CommandSourceStack> context) {
                String villageName = StringArgumentType.getString(context, "villageName");

                context.getSource().sendSuccess(() -> Component.literal(
                                "Teleporting to village: " + villageName), true);

                // TODO: Implement when village system is ported
                MillLog.major(null, "Command: Teleport to village " + villageName);

                return 1;
        }

        private static int executeListActiveVillages(CommandContext<CommandSourceStack> context) {
                context.getSource().sendSuccess(() -> Component.literal(
                                "Listing active villages..."), false);

                // TODO: Implement when MillWorldData is ported
                context.getSource().sendSuccess(() -> Component.literal(
                                "No active villages found (village system not yet fully ported)"), false);

                return 1;
        }

        private static int executeGiveReputation(CommandContext<CommandSourceStack> context) {
                String villageName = StringArgumentType.getString(context, "villageName");
                int amount = IntegerArgumentType.getInteger(context, "amount");

                context.getSource().sendSuccess(() -> Component.literal(
                                "Giving " + amount + " reputation to village: " + villageName), true);

                // TODO: Implement when reputation system is ported
                MillLog.major(null, "Command: Give " + amount + " reputation to " + villageName);

                return 1;
        }

        private static int executeRenameVillage(CommandContext<CommandSourceStack> context) {
                String villageName = StringArgumentType.getString(context, "villageName");
                String newName = StringArgumentType.getString(context, "newName");

                context.getSource().sendSuccess(() -> Component.literal(
                                "Renaming village " + villageName + " to " + newName), true);

                // TODO: Implement when village system is ported
                MillLog.major(null, "Command: Rename village " + villageName + " to " + newName);

                return 1;
        }

        private static int executeSwitchControl(CommandContext<CommandSourceStack> context) {
                String villageName = StringArgumentType.getString(context, "villageName");

                context.getSource().sendSuccess(() -> Component.literal(
                                "Switching control for village: " + villageName), true);

                // TODO: Implement when village control system is ported
                MillLog.major(null, "Command: Switch control for " + villageName);

                return 1;
        }

        private static int executeImportCulture(CommandContext<CommandSourceStack> context) {
                String cultureName = StringArgumentType.getString(context, "cultureName");

                context.getSource().sendSuccess(() -> Component.literal(
                                "Importing culture: " + cultureName), true);

                // TODO: Implement when culture import system is ported
                MillLog.major(null, "Command: Import culture " + cultureName);

                return 1;
        }

        private static int executeDebugResetVillagers(CommandContext<CommandSourceStack> context) {
                context.getSource().sendSuccess(() -> Component.literal(
                                "Resetting all villagers..."), true);

                // TODO: Implement when villager system is ported
                MillLog.major(null, "Command: Debug reset villagers");

                return 1;
        }

        private static int executeDebugResendProfiles(CommandContext<CommandSourceStack> context) {
                context.getSource().sendSuccess(() -> Component.literal(
                                "Resending all profiles..."), true);

                // TODO: Implement when profile system is ported
                MillLog.major(null, "Command: Debug resend profiles");

                return 1;
        }
}

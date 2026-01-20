package org.millenaire.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.millenaire.common.culture.Culture;
import org.millenaire.common.culture.MillenaireCultures;
import org.millenaire.worldgen.MillenaireBuildingParser;
import org.millenaire.common.buildingplan.BuildingPlan;

import java.util.ArrayList;
import java.util.List;

/**
 * Commands for testing and managing Millénaire buildings and cultures.
 */
public class MillenaireCommands {

    // Suggestion provider for culture names
    private static final SuggestionProvider<CommandSourceStack> CULTURE_SUGGESTIONS = (context,
            builder) -> SharedSuggestionProvider.suggest(
                    MillenaireCultures.getAllCultures().keySet(),
                    builder);

    // Suggestion provider for building paths
    private static final SuggestionProvider<CommandSourceStack> BUILDING_SUGGESTIONS = (context, builder) -> {
        List<String> suggestions = new ArrayList<>();
        try {
            String cultureName = StringArgumentType.getString(context, "culture");
            Culture culture = MillenaireCultures.getCulture(cultureName);
            if (culture != null) {
                for (String path : culture.getBuildingPaths()) {
                    // Extract just the building name from the path
                    String[] parts = path.split("/");
                    if (parts.length >= 3) {
                        String buildingFile = parts[parts.length - 1];
                        String buildingName = buildingFile.replace("_A0.png", "");
                        suggestions.add(buildingName);
                    }
                }
            }
        } catch (Exception e) {
            // If culture not found, return empty suggestions
        }
        return SharedSuggestionProvider.suggest(suggestions, builder);
    };

    // Suggestion provider for village types
    private static final SuggestionProvider<CommandSourceStack> VILLAGE_TYPE_SUGGESTIONS = (context, builder) -> {
        List<String> suggestions = new ArrayList<>();
        try {
            String cultureName = StringArgumentType.getString(context, "culture");
            Culture culture = MillenaireCultures.getCulture(cultureName);
            if (culture != null) {
                for (org.millenaire.common.culture.VillageType type : culture.getVillageTypes()) {
                    suggestions.add(type.getId());
                }
            }
        } catch (Exception e) {
            // If culture not found, return empty suggestions
        }
        return SharedSuggestionProvider.suggest(suggestions, builder);
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("millenaire")
                        .requires(source -> source.hasPermission(2)) // Op level 2

                        // /millenaire testbuild <culture> <building>
                        .then(Commands.literal("testbuild")
                                .then(Commands.argument("culture", StringArgumentType.word())
                                        .suggests(CULTURE_SUGGESTIONS)
                                        .then(Commands.argument("building", StringArgumentType.word())
                                                .suggests(BUILDING_SUGGESTIONS)
                                                .executes(MillenaireCommands::testBuild))))

                        // /millenaire listbuildings [culture]
                        .then(Commands.literal("listbuildings")
                                .executes(MillenaireCommands::listAllBuildings)
                                .then(Commands.argument("culture", StringArgumentType.word())
                                        .suggests(CULTURE_SUGGESTIONS)
                                        .executes(MillenaireCommands::listCultureBuildings)))

                        // /millenaire listcultures
                        .then(Commands.literal("listcultures")
                                .executes(MillenaireCommands::listCultures))

                        // /millenaire spawnvillage <culture> [type]
                        .then(Commands.literal("spawnvillage")
                                .then(Commands.argument("culture", StringArgumentType.word())
                                        .suggests(CULTURE_SUGGESTIONS)
                                        .executes(MillenaireCommands::spawnVillage)
                                        .then(Commands.argument("type", StringArgumentType.word())
                                                .suggests(VILLAGE_TYPE_SUGGESTIONS)
                                                .executes(MillenaireCommands::spawnVillageWithType))))

                        // /millenaire spawnlone <culture>
                        .then(Commands.literal("spawnlone")
                                .then(Commands.argument("culture", StringArgumentType.word())
                                        .suggests(CULTURE_SUGGESTIONS)
                                        .executes(MillenaireCommands::spawnLoneBuilding))));
    }

    private static int testBuild(CommandContext<CommandSourceStack> context) {
        String cultureName = StringArgumentType.getString(context, "culture");
        String buildingName = StringArgumentType.getString(context, "building");

        CommandSourceStack source = context.getSource();

        try {
            ServerPlayer player = source.getPlayerOrException();
            ServerLevel level = player.serverLevel();
            BlockPos playerPos = player.blockPosition();

            // Get culture
            Culture culture = MillenaireCultures.getCulture(cultureName);
            if (culture == null) {
                source.sendFailure(Component.literal("Culture not found: " + cultureName));
                return 0;
            }

            // Find matching building path
            String foundPath = null;
            for (String path : culture.getBuildingPaths()) {
                if (path.contains("/" + buildingName + "_A0.png")) {
                    foundPath = path;
                    break;
                }
            }

            if (foundPath == null) {
                source.sendFailure(
                        Component.literal("Building not found: " + buildingName + " in culture " + cultureName));
                return 0;
            }

            final String buildingPath = foundPath;

            // Load and place building
            source.sendSuccess(() -> Component.literal("Loading building: " + buildingPath), true);

            BuildingPlan plan = MillenaireBuildingParser.loadPlan(buildingPath);
            MillenaireBuildingParser.placeBuilding(level, playerPos, plan);

            source.sendSuccess(() -> Component.literal(
                    "Placed " + buildingName + " from " + culture.getDisplayName() + " culture! " +
                            "Size: " + plan.width() + "x" + plan.length() + " (floors: " + plan.nbFloors() + ")"),
                    true);

            return 1;

        } catch (Exception e) {
            source.sendFailure(Component.literal("Error placing building: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }

    private static int listCultureBuildings(CommandContext<CommandSourceStack> context) {
        String cultureName = StringArgumentType.getString(context, "culture");
        CommandSourceStack source = context.getSource();

        Culture culture = MillenaireCultures.getCulture(cultureName);
        if (culture == null) {
            source.sendFailure(Component.literal("Culture not found: " + cultureName));
            return 0;
        }

        source.sendSuccess(() -> Component.literal(
                "§6Buildings in " + culture.getDisplayName() + " culture: " +
                        "§f(" + culture.getBuildingPaths().size() + " total)"),
                false);

        // Group by category
        culture.getBuildingPaths().forEach(path -> {
            String[] parts = path.split("/");
            if (parts.length >= 3) {
                String category = parts[parts.length - 2];
                String file = parts[parts.length - 1];
                String name = file.replace("_A0.png", "");
                source.sendSuccess(() -> Component.literal(
                        "  §3" + category + "§7/§b" + name), false);
            }
        });

        return 1;
    }

    private static int listAllBuildings(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        source.sendSuccess(() -> Component.literal("§6All Millénaire Cultures:"), false);

        MillenaireCultures.getAllCultures().forEach((id, culture) -> {
            source.sendSuccess(() -> Component.literal(
                    "  §3" + culture.getDisplayName() + "§7: §f" +
                            culture.getBuildingPaths().size() + " buildings"),
                    false);
        });

        return 1;
    }

    private static int listCultures(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        source.sendSuccess(() -> Component.literal("§6Available Cultures:"), false);

        MillenaireCultures.getAllCultures().forEach((id, culture) -> {
            int buildingCount = culture.getBuildingPaths().size();
            int villagerCount = culture.getVillagerTypes().size();
            int tradeCount = culture.getTradeGoods().size();

            source.sendSuccess(() -> Component.literal(
                    "  §3" + culture.getDisplayName() + "§7 (§b" + id + "§7)\n" +
                            "    Buildings: §f" + buildingCount +
                            "§7  Villagers: §f" + villagerCount +
                            "§7  Trade Goods: §f" + tradeCount),
                    false);
        });

        return 1;
    }

    private static int spawnVillageWithType(CommandContext<CommandSourceStack> context) {
        String cultureName = StringArgumentType.getString(context, "culture");
        String typeName = StringArgumentType.getString(context, "type");
        CommandSourceStack source = context.getSource();

        try {
            ServerPlayer player = source.getPlayerOrException();
            ServerLevel level = player.serverLevel();
            BlockPos playerPos = player.blockPosition();

            // Get culture
            Culture culture = MillenaireCultures.getCulture(cultureName);
            if (culture == null) {
                source.sendFailure(Component.literal("Culture not found: " + cultureName));
                return 0;
            }

            // Get Village Type
            org.millenaire.common.culture.VillageType villageType = null;
            for (org.millenaire.common.culture.VillageType type : culture.getVillageTypes()) {
                if (type.getId().equalsIgnoreCase(typeName)) {
                    villageType = type;
                    break;
                }
            }

            if (villageType == null) {
                source.sendFailure(Component.literal("Village type not found: " + typeName));
                return 0;
            }

            // Create final copy for lambda
            final org.millenaire.common.culture.VillageType finalVillageType = villageType;

            // Spawn village
            source.sendSuccess(
                    () -> Component
                            .literal("Spawning " + culture.getDisplayName() + " " + finalVillageType.getName() + "..."),
                    true);

            org.millenaire.core.VillageData village = org.millenaire.worldgen.VillageStarter.spawnVillage(
                    level, playerPos, culture, villageType);

            if (village != null) {
                source.sendSuccess(() -> Component.literal(
                        "§aSuccessfully spawned " + culture.getDisplayName() + " village!\n" +
                                "§7Village ID: §f" + village.getVillageId() + "\n" +
                                "§7Buildings placed: §f" + village.getBuildings().size()),
                        true);
                return 1;
            } else {
                source.sendFailure(Component.literal("Failed to spawn village"));
                return 0;
            }

        } catch (Exception e) {
            source.sendFailure(Component.literal("Error spawning village: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }

    private static int spawnVillage(CommandContext<CommandSourceStack> context) {
        String cultureName = StringArgumentType.getString(context, "culture");
        CommandSourceStack source = context.getSource();

        try {
            ServerPlayer player = source.getPlayerOrException();
            ServerLevel level = player.serverLevel();
            BlockPos playerPos = player.blockPosition();

            // Get culture
            Culture culture = MillenaireCultures.getCulture(cultureName);
            if (culture == null) {
                source.sendFailure(Component.literal("Culture not found: " + cultureName));
                return 0;
            }

            // Spawn starter village
            source.sendSuccess(() -> Component.literal("Spawning " + culture.getDisplayName() + " random village..."),
                    true);

            // Use the updated spawnStarterVillage which picks a random type
            org.millenaire.core.VillageData village = org.millenaire.worldgen.VillageStarter.spawnStarterVillage(
                    level, playerPos, culture);

            if (village != null) {
                source.sendSuccess(() -> Component.literal(
                        "§aSuccessfully spawned " + culture.getDisplayName() + " village!\n" +
                                "§7Village ID: §f" + village.getVillageId() + "\n" +
                                "§7Buildings placed: §f" + village.getBuildings().size()),
                        true);
                return 1;
            } else {
                source.sendFailure(Component
                        .literal("Failed to spawn village (check logs for errors, maybe no village types loaded)"));
                return 0;
            }

        } catch (Exception e) {
            source.sendFailure(Component.literal("Error spawning village: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }

    private static int spawnLoneBuilding(CommandContext<CommandSourceStack> context) {
        String cultureName = StringArgumentType.getString(context, "culture");
        CommandSourceStack source = context.getSource();

        try {
            ServerPlayer player = source.getPlayerOrException();
            ServerLevel level = player.serverLevel();
            BlockPos playerPos = player.blockPosition();

            // Get culture
            Culture culture = MillenaireCultures.getCulture(cultureName);
            if (culture == null) {
                source.sendFailure(Component.literal("Culture not found: " + cultureName));
                return 0;
            }

            // Spawn lone building
            source.sendSuccess(() -> Component.literal("Spawning " + culture.getDisplayName() + " lone building..."),
                    true);

            boolean success = org.millenaire.worldgen.VillageStarter.spawnLoneBuilding(level, playerPos, culture);

            if (success) {
                source.sendSuccess(() -> Component.literal(
                        "§aSuccessfully spawned " + culture.getDisplayName() + " lone building!"), true);
                return 1;
            } else {
                source.sendFailure(Component.literal("Failed to spawn lone building"));
                return 0;
            }

        } catch (Exception e) {
            source.sendFailure(Component.literal("Error spawning lone building: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }
}

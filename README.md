# Millénaire 1.12.2 - Decompiled Source Code

<<<<<<< HEAD
=======
[![Minecraft](https://img.shields.io/badge/Minecraft-1.12.2-green.svg)](https://minecraft.net)
[![Forge](https://img.shields.io/badge/Forge-14.23.5.2860-orange.svg)](https://files.minecraftforge.net/)
[![License](https://img.shields.io/badge/License-See%20Original-blue.svg)](http://millenaire.org)

>>>>>>> f5d0cad (Updates to MillVillager, Trade and Puja GUIs)
Fully decompiled and fixed source code for the **Millénaire** mod for Minecraft 1.12.2.

Millénaire is a village mod that adds procedurally generated NPC villages from various cultures (Norman, Indian, Japanese, Mayan, Byzantine, Inuit, and Seljuk) that grow and evolve over time.

## Features

- 🏘️ **7 Unique Cultures** - Norman, Indian, Japanese, Mayan, Byzantine, Inuit, and Seljuk villages
- 🔨 **Dynamic Building** - Villages construct and upgrade buildings over time
- 👥 **Living Villagers** - NPCs with jobs, relationships, and daily routines
- 💰 **Trading System** - Buy and sell goods with villagers
- 📜 **Quests** - Complete quests for reputation and rewards
- ⚔️ **Village Raids** - Defend villages from attacks

## Bug Fixes

<<<<<<< HEAD
Things that needed fixing after I originally decompiled:
=======
This version includes critical fixes not present in the original release:
>>>>>>> f5d0cad (Updates to MillVillager, Trade and Puja GUIs)

### Invisible Villagers Fix
- **Root Cause**: Multiple `MillWorldData` instances caused villager records to be registered in one instance but checked in another
- **Solution**: Changed `rebuildVillagerList` to check `villager.mw.getVillagerRecords()` instead of `this.villagerRecords`

### Additional Fixes
- Fixed resource loading errors for models and textures
- Added missing blockstate configurations
- Resolved entity registration issues

## Building

### Requirements
- Java Development Kit 8 (JDK 1.8)
- Gradle (wrapper included)

### Build Commands

```bash
# Set JAVA_HOME to JDK 8
set JAVA_HOME=C:\Program Files\Java\jdk-1.8

# Build the mod
gradlew build
```

The compiled JAR will be located in `build/libs/`.

## Installation

1. Install [Minecraft Forge](https://files.minecraftforge.net/) for 1.12.2
2. Download the mod JAR from [Releases](../../releases)
3. Place the JAR in your `.minecraft/mods` folder
4. Launch Minecraft with the Forge profile

## Project Structure

```
src/main/java/org/millenaire/
├── common/
│   ├── block/          # Custom blocks (thatch, timber, etc.)
│   ├── buildingplan/   # Building blueprints and construction
│   ├── config/         # Configuration handling
│   ├── entity/         # MillVillager and related entities
│   ├── forge/          # Forge integration (Mill.java main class)
│   ├── item/           # Custom items and tools
│   ├── network/        # Packet handling
│   ├── village/        # Village and Building logic
│   └── world/          # MillWorldData and world management
└── client/             # Client-side rendering
```

## Credits

- **Original Mod**: [Millénaire](http://millenaire.org) by Kinniken
- **Decompilation & Fixes**: Joenetix

## Disclaimer

This is a decompiled version of the original Millénaire mod for educational and preservation purposes. All rights to the original mod belong to its creator. Please refer to the [original mod's website](http://millenaire.org) for licensing information.

## Links

- [Original Millénaire Website](http://millenaire.org)
- [Minecraft Forge](https://files.minecraftforge.net/)
- [CurseForge Page](https://www.curseforge.com/minecraft/mc-mods/millenaire)
